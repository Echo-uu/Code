package com.billsync.app.service.parser

import com.billsync.app.data.entity.*

/**
 * 招商银行账单解析器
 *
 * 招商银行通知/短信格式示例：
 *
 * 【储蓄卡-掌上生活/招商银行】
 * - "您账户*1234于01月15日消费人民币100.00元，余额1000.00元"
 * - "您尾号1234的储蓄账户01月15日转出人民币500.00元，余额2000.00"
 * - "您尾号1234的储蓄账户01月15日收入人民币8000.00元，余额10000.00"
 *
 * 【信用卡】
 * - "您的信用卡尾号1234于01月15日在XX商户消费人民币200.00元"
 * - "您尾号1234的信用卡01月15日网上交易人民币150.00元，商户：XX"
 * - "信用卡尾号1234还款人民币5000.00元成功"
 *
 * 短信发送号码通常包含: 95555, 招商银行
 * App包名: cmb.pb (招商银行), com.chinamworld.main (掌上生活)
 */
class CMBParser : BillParser {

    companion object {
        const val CMB_PACKAGE = "cmb.pb"
        const val CMB_LIFE_PACKAGE = "com.chinamworld.main"
        val CMB_SMS_SENDERS = listOf("95555", "106980095555", "招商银行")

        // 储蓄卡消费
        val DEBIT_EXPENSE = Regex("尾号(\\d+).*?(?:消费|转出|支出).*?人民币(\\d+\\.?\\d*)元")
        // 储蓄卡收入
        val DEBIT_INCOME = Regex("尾号(\\d+).*?(?:收入|转入|入账).*?人民币(\\d+\\.?\\d*)元")
        // 信用卡消费
        val CREDIT_EXPENSE = Regex("信用卡尾号(\\d+).*?(?:消费|交易).*?人民币(\\d+\\.?\\d*)元")
        // 信用卡还款
        val CREDIT_REPAY = Regex("信用卡尾号(\\d+).*?还款.*?人民币(\\d+\\.?\\d*)元")
        // 商户名提取
        val MERCHANT_PATTERN = Regex("(?:商户[：:]?|在)(.+?)(?:消费|交易|支付)")
        // 通用金额
        val AMOUNT_PATTERN = Regex("人民币(\\d+\\.?\\d*)元")
        // 余额
        val BALANCE_PATTERN = Regex("余额[¥￥]?(\\d+\\.?\\d*)元?")
    }

    override fun canHandleNotification(packageName: String): Boolean {
        return packageName == CMB_PACKAGE || packageName == CMB_LIFE_PACKAGE
    }

    override fun canHandleSms(sender: String): Boolean {
        return CMB_SMS_SENDERS.any { sender.contains(it) }
    }

    override fun parseNotification(title: String, content: String, packageName: String): BillEntity? {
        if (!canHandleNotification(packageName)) return null
        return parseContent(content, title)
    }

    override fun parseSms(sender: String, content: String): BillEntity? {
        if (!canHandleSms(sender)) return null
        return parseContent(content, "短信:$sender")
    }

    private fun parseContent(content: String, rawTitle: String): BillEntity? {
        val isCredit = content.contains("信用卡")
        val merchant = MERCHANT_PATTERN.find(content)?.groupValues?.get(1)?.trim() ?: ""

        // 信用卡还款
        CREDIT_REPAY.find(content)?.let { match ->
            val cardNo = match.groupValues[1]
            val amount = match.groupValues[2].toDoubleOrNull() ?: return null
            return BillEntity(
                amount = -amount,
                type = BillType.TRANSFER,
                source = BillSource.CMB_CREDIT,
                category = BillCategory.TRANSFER,
                description = "招行信用卡还款(尾号$cardNo)",
                counterparty = "招商银行",
                rawContent = "$rawTitle|$content"
            )
        }

        // 信用卡消费
        CREDIT_EXPENSE.find(content)?.let { match ->
            val cardNo = match.groupValues[1]
            val amount = match.groupValues[2].toDoubleOrNull() ?: return null
            return BillEntity(
                amount = -amount,
                type = BillType.EXPENSE,
                source = BillSource.CMB_CREDIT,
                category = guessCategory(merchant, content),
                description = "招行信用卡消费(尾号$cardNo)",
                counterparty = merchant,
                rawContent = "$rawTitle|$content"
            )
        }

        // 储蓄卡收入
        DEBIT_INCOME.find(content)?.let { match ->
            val cardNo = match.groupValues[1]
            val amount = match.groupValues[2].toDoubleOrNull() ?: return null
            return BillEntity(
                amount = amount,
                type = BillType.INCOME,
                source = BillSource.CMB_DEBIT,
                category = guessCategoryIncome(content),
                description = "招行储蓄卡收入(尾号$cardNo)",
                counterparty = merchant,
                rawContent = "$rawTitle|$content"
            )
        }

        // 储蓄卡消费/转出
        DEBIT_EXPENSE.find(content)?.let { match ->
            val cardNo = match.groupValues[1]
            val amount = match.groupValues[2].toDoubleOrNull() ?: return null
            return BillEntity(
                amount = -amount,
                type = BillType.EXPENSE,
                source = BillSource.CMB_DEBIT,
                category = guessCategory(merchant, content),
                description = "招行储蓄卡支出(尾号$cardNo)",
                counterparty = merchant,
                rawContent = "$rawTitle|$content"
            )
        }

        // 兜底
        AMOUNT_PATTERN.find(content)?.let { match ->
            val amount = match.groupValues[1].toDoubleOrNull() ?: return null
            val isIncome = content.containsAny("收入", "转入", "入账", "到账", "工资", "薪")
            val source = if (isCredit) BillSource.CMB_CREDIT else BillSource.CMB_DEBIT
            return BillEntity(
                amount = if (isIncome) amount else -amount,
                type = if (isIncome) BillType.INCOME else BillType.EXPENSE,
                source = source,
                category = BillCategory.OTHER,
                description = content.take(50),
                counterparty = merchant,
                rawContent = "$rawTitle|$content"
            )
        }

        return null
    }

    private fun guessCategory(merchant: String, content: String): BillCategory {
        val text = "$merchant $content"
        return when {
            text.containsAny("餐", "饭", "食", "外卖", "饮") -> BillCategory.FOOD
            text.containsAny("打车", "出行", "滴滴", "公交", "加油", "停车") -> BillCategory.TRANSPORT
            text.containsAny("超市", "商城", "购物", "淘宝", "京东") -> BillCategory.SHOPPING
            text.containsAny("房租", "水电", "物业") -> BillCategory.HOUSING
            text.containsAny("医院", "药", "医疗") -> BillCategory.MEDICAL
            text.containsAny("话费", "充值") -> BillCategory.COMMUNICATION
            text.containsAny("还款") -> BillCategory.TRANSFER
            else -> BillCategory.OTHER
        }
    }

    private fun guessCategoryIncome(content: String): BillCategory {
        return when {
            content.containsAny("工资", "薪", "salary") -> BillCategory.SALARY
            content.containsAny("奖金", "bonus") -> BillCategory.BONUS
            content.containsAny("退款", "退") -> BillCategory.REFUND
            content.containsAny("转账", "转入") -> BillCategory.TRANSFER
            else -> BillCategory.OTHER
        }
    }

    private fun String.containsAny(vararg keywords: String): Boolean {
        return keywords.any { this.contains(it, ignoreCase = true) }
    }
}
