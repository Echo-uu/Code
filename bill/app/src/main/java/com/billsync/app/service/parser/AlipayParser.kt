package com.billsync.app.service.parser

import com.billsync.app.data.entity.*

/**
 * 支付宝账单解析器
 *
 * 支付宝通知格式示例：
 * - 标题: "支付宝" / "支付助手"
 * - 内容: "成功付款¥12.50（商家名称）"
 * - 内容: "你已成功向XXX付款12.50元"
 * - 内容: "收到XXX的转账¥100.00"
 * - 内容: "XXX向你付款¥50.00"
 * - 内容: "账单提醒：你在XXX消费了¥88.00"
 */
class AlipayParser : BillParser {

    companion object {
        const val ALIPAY_PACKAGE = "com.eg.android.AlipayGphone"

        // 付款模式
        val EXPENSE_PATTERN1 = Regex("付款[¥￥](\\d+\\.?\\d*)[（(](.+?)[）)]")
        val EXPENSE_PATTERN2 = Regex("向(.+?)付款[¥￥]?(\\d+\\.?\\d*)元?")
        val EXPENSE_PATTERN3 = Regex("在(.+?)消费了?[¥￥]?(\\d+\\.?\\d*)元?")
        val EXPENSE_PATTERN4 = Regex("付款[¥￥](\\d+\\.?\\d*)")

        // 收款模式
        val INCOME_PATTERN1 = Regex("收到(.+?)的?转账[¥￥]?(\\d+\\.?\\d*)元?")
        val INCOME_PATTERN2 = Regex("(.+?)向你付款[¥￥]?(\\d+\\.?\\d*)元?")
        val INCOME_PATTERN3 = Regex("收款到账[¥￥](\\d+\\.?\\d*)")
        val INCOME_PATTERN4 = Regex("到账[¥￥](\\d+\\.?\\d*)")

        // 通用金额
        val AMOUNT_PATTERN = Regex("[¥￥](\\d+\\.?\\d*)")
    }

    override fun canHandleNotification(packageName: String): Boolean {
        return packageName == ALIPAY_PACKAGE
    }

    override fun canHandleSms(sender: String): Boolean = false

    override fun parseNotification(title: String, content: String, packageName: String): BillEntity? {
        if (packageName != ALIPAY_PACKAGE) return null

        // 尝试解析收款
        INCOME_PATTERN1.find(content)?.let { match ->
            val counterparty = match.groupValues[1].trim()
            val amount = match.groupValues[2].toDoubleOrNull() ?: return null
            return createBill(amount, BillType.INCOME, counterparty, "支付宝转账收入", title, content)
        }

        INCOME_PATTERN2.find(content)?.let { match ->
            val counterparty = match.groupValues[1].trim()
            val amount = match.groupValues[2].toDoubleOrNull() ?: return null
            return createBill(amount, BillType.INCOME, counterparty, "支付宝收款", title, content)
        }

        INCOME_PATTERN3.find(content)?.let { match ->
            val amount = match.groupValues[1].toDoubleOrNull() ?: return null
            return createBill(amount, BillType.INCOME, "", "支付宝收款到账", title, content)
        }

        INCOME_PATTERN4.find(content)?.let { match ->
            if (content.contains("收") || content.contains("到账")) {
                val amount = match.groupValues[1].toDoubleOrNull() ?: return null
                return createBill(amount, BillType.INCOME, "", "支付宝到账", title, content)
            }
        }

        // 尝试解析付款（带商家名）
        EXPENSE_PATTERN1.find(content)?.let { match ->
            val amount = match.groupValues[1].toDoubleOrNull() ?: return null
            val counterparty = match.groupValues[2].trim()
            return createBill(-amount, BillType.EXPENSE, counterparty, "支付宝支付", title, content)
        }

        EXPENSE_PATTERN2.find(content)?.let { match ->
            val counterparty = match.groupValues[1].trim()
            val amount = match.groupValues[2].toDoubleOrNull() ?: return null
            return createBill(-amount, BillType.EXPENSE, counterparty, "支付宝付款", title, content)
        }

        EXPENSE_PATTERN3.find(content)?.let { match ->
            val counterparty = match.groupValues[1].trim()
            val amount = match.groupValues[2].toDoubleOrNull() ?: return null
            return createBill(-amount, BillType.EXPENSE, counterparty, "支付宝消费", title, content)
        }

        EXPENSE_PATTERN4.find(content)?.let { match ->
            val amount = match.groupValues[1].toDoubleOrNull() ?: return null
            return createBill(-amount, BillType.EXPENSE, "", "支付宝支付", title, content)
        }

        // 兜底
        AMOUNT_PATTERN.find(content)?.let { match ->
            val amount = match.groupValues[1].toDoubleOrNull() ?: return null
            val isIncome = content.contains("收") || content.contains("到账") || content.contains("退")
            return createBill(
                if (isIncome) amount else -amount,
                if (isIncome) BillType.INCOME else BillType.EXPENSE,
                "", content.take(50), title, content
            )
        }

        return null
    }

    override fun parseSms(sender: String, content: String): BillEntity? = null

    private fun createBill(
        amount: Double,
        type: BillType,
        counterparty: String,
        description: String,
        title: String,
        content: String
    ): BillEntity {
        return BillEntity(
            amount = amount,
            type = type,
            source = BillSource.ALIPAY,
            category = guessCategory(counterparty, content),
            description = description,
            counterparty = counterparty,
            rawContent = "$title|$content"
        )
    }

    private fun guessCategory(counterparty: String, content: String): BillCategory {
        val text = "$counterparty $content"
        return when {
            text.containsAny("餐", "饭", "食", "外卖", "饿了么", "饮", "咖啡", "奶茶") -> BillCategory.FOOD
            text.containsAny("打车", "出行", "滴滴", "公交", "地铁", "高德", "花小猪") -> BillCategory.TRANSPORT
            text.containsAny("超市", "商城", "淘宝", "天猫", "购物") -> BillCategory.SHOPPING
            text.containsAny("电影", "游戏", "娱乐", "门票") -> BillCategory.ENTERTAINMENT
            text.containsAny("房租", "水电", "物业", "燃气") -> BillCategory.HOUSING
            text.containsAny("医院", "药", "医疗") -> BillCategory.MEDICAL
            text.containsAny("话费", "流量", "充值") -> BillCategory.COMMUNICATION
            text.containsAny("红包") -> BillCategory.RED_PACKET
            text.containsAny("退款", "退") -> BillCategory.REFUND
            text.containsAny("转账") -> BillCategory.TRANSFER
            else -> BillCategory.OTHER
        }
    }

    private fun String.containsAny(vararg keywords: String): Boolean {
        return keywords.any { this.contains(it) }
    }
}
