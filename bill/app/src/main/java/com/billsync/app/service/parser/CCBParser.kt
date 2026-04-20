package com.billsync.app.service.parser

import com.billsync.app.data.entity.*

/**
 * 建设银行账单解析器
 *
 * 建设银行通知/短信格式示例：
 *
 * 【储蓄卡】
 * - "您尾号1234的储蓄卡01月15日支出人民币100.00元，余额1000.00元，对方户名：XX"
 * - "您尾号1234的账户01月15日收入人民币5000.00元，余额8000.00元"
 * - "您尾号1234的账户消费人民币50.00元，余额950.00元"
 *
 * 【信用卡-龙卡】
 * - "您尾号1234的信用卡01月15日消费人民币200.00元，商户：XX"
 * - "您尾号1234龙卡01月15日网上消费人民币100.00元"
 * - "信用卡尾号1234还款人民币3000.00元成功"
 *
 * 短信发送号码通常包含: 95533, 建设银行
 * App包名: com.chinamworld.bocmbci (中国建设银行)
 */
class CCBParser : BillParser {

    companion object {
        const val CCB_PACKAGE = "com.chinamworld.bocmbci"
        val CCB_SMS_SENDERS = listOf("95533", "106980095533", "建设银行")

        // 储蓄卡支出
        val DEBIT_EXPENSE = Regex("尾号(\\d+).*?(?:支出|消费|转出).*?人民币(\\d+\\.?\\d*)元")
        // 储蓄卡收入
        val DEBIT_INCOME = Regex("尾号(\\d+).*?(?:收入|转入|入账|存入).*?人民币(\\d+\\.?\\d*)元")
        // 信用卡消费
        val CREDIT_EXPENSE = Regex("(?:信用卡|龙卡)尾号(\\d+).*?(?:消费|交易|支出).*?人民币(\\d+\\.?\\d*)元")
        // 信用卡还款
        val CREDIT_REPAY = Regex("(?:信用卡|龙卡)尾号(\\d+).*?还款.*?人民币(\\d+\\.?\\d*)元")
        // 对方户名
        val COUNTERPARTY_PATTERN = Regex("(?:对方户名[：:]?|商户[：:]?)(.+?)(?:[，,。]|$)")
        // 通用金额
        val AMOUNT_PATTERN = Regex("人民币(\\d+\\.?\\d*)元")
    }

    override fun canHandleNotification(packageName: String): Boolean {
        return packageName == CCB_PACKAGE
    }

    override fun canHandleSms(sender: String): Boolean {
        return CCB_SMS_SENDERS.any { sender.contains(it) }
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
        val isCredit = content.contains("信用卡") || content.contains("龙卡")
        val counterparty = COUNTERPARTY_PATTERN.find(content)?.groupValues?.get(1)?.trim() ?: ""

        // 信用卡还款
        CREDIT_REPAY.find(content)?.let { match ->
            val cardNo = match.groupValues[1]
            val amount = match.groupValues[2].toDoubleOrNull() ?: return null
            return BillEntity(
                amount = -amount,
                type = BillType.TRANSFER,
                source = BillSource.CCB_CREDIT,
                category = BillCategory.TRANSFER,
                description = "建行信用卡还款(尾号$cardNo)",
                counterparty = "建设银行",
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
                source = BillSource.CCB_CREDIT,
                category = guessCategory(counterparty, content),
                description = "建行信用卡消费(尾号$cardNo)",
                counterparty = counterparty,
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
                source = BillSource.CCB_DEBIT,
                category = guessCategoryIncome(content),
                description = "建行储蓄卡收入(尾号$cardNo)",
                counterparty = counterparty,
                rawContent = "$rawTitle|$content"
            )
        }

        // 储蓄卡支出
        DEBIT_EXPENSE.find(content)?.let { match ->
            val cardNo = match.groupValues[1]
            val amount = match.groupValues[2].toDoubleOrNull() ?: return null
            return BillEntity(
                amount = -amount,
                type = BillType.EXPENSE,
                source = BillSource.CCB_DEBIT,
                category = guessCategory(counterparty, content),
                description = "建行储蓄卡支出(尾号$cardNo)",
                counterparty = counterparty,
                rawContent = "$rawTitle|$content"
            )
        }

        // 兜底
        AMOUNT_PATTERN.find(content)?.let { match ->
            val amount = match.groupValues[1].toDoubleOrNull() ?: return null
            val isIncome = content.containsAny("收入", "转入", "入账", "存入", "到账", "工资")
            val source = if (isCredit) BillSource.CCB_CREDIT else BillSource.CCB_DEBIT
            return BillEntity(
                amount = if (isIncome) amount else -amount,
                type = if (isIncome) BillType.INCOME else BillType.EXPENSE,
                source = source,
                category = BillCategory.OTHER,
                description = content.take(50),
                counterparty = counterparty,
                rawContent = "$rawTitle|$content"
            )
        }

        return null
    }

    private fun guessCategory(merchant: String, content: String): BillCategory {
        val text = "$merchant $content"
        return when {
            text.containsAny("餐", "饭", "食", "外卖", "饮") -> BillCategory.FOOD
            text.containsAny("打车", "出行", "公交", "加油", "停车") -> BillCategory.TRANSPORT
            text.containsAny("超市", "商城", "购物") -> BillCategory.SHOPPING
            text.containsAny("房租", "水电", "物业") -> BillCategory.HOUSING
            text.containsAny("医院", "药", "医疗") -> BillCategory.MEDICAL
            text.containsAny("话费", "充值") -> BillCategory.COMMUNICATION
            text.containsAny("还款") -> BillCategory.TRANSFER
            else -> BillCategory.OTHER
        }
    }

    private fun guessCategoryIncome(content: String): BillCategory {
        return when {
            content.containsAny("工资", "薪") -> BillCategory.SALARY
            content.containsAny("奖金") -> BillCategory.BONUS
            content.containsAny("退款", "退") -> BillCategory.REFUND
            content.containsAny("转账", "转入") -> BillCategory.TRANSFER
            else -> BillCategory.OTHER
        }
    }

    private fun String.containsAny(vararg keywords: String): Boolean {
        return keywords.any { this.contains(it, ignoreCase = true) }
    }
}
