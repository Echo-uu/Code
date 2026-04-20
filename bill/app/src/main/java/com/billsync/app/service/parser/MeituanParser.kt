package com.billsync.app.service.parser

import com.billsync.app.data.entity.*

/**
 * 美团账单解析器
 *
 * 美团/美团外卖通知格式示例：
 * - 包名: com.sankuai.meituan (美团)
 * - 包名: com.sankuai.meituan.takeoutnew (美团外卖)
 * - 标题: "美团" / "美团外卖"
 * - 内容: "订单已支付，实付¥25.50"
 * - 内容: "您的订单已完成支付，共计¥35.00"
 * - 内容: "支付成功！实付金额¥18.80"
 * - 内容: "退款成功，¥12.00已退回"
 */
class MeituanParser : BillParser {

    companion object {
        const val MEITUAN_PACKAGE = "com.sankuai.meituan"
        const val MEITUAN_TAKEOUT_PACKAGE = "com.sankuai.meituan.takeoutnew"

        // 支付金额
        val PAY_PATTERN1 = Regex("实付[¥￥]?(\\d+\\.?\\d*)元?")
        val PAY_PATTERN2 = Regex("共计[¥￥]?(\\d+\\.?\\d*)元?")
        val PAY_PATTERN3 = Regex("付金额[¥￥]?(\\d+\\.?\\d*)元?")
        val PAY_PATTERN4 = Regex("支付[¥￥](\\d+\\.?\\d*)")

        // 退款
        val REFUND_PATTERN = Regex("退款.*?[¥￥]?(\\d+\\.?\\d*)元?.*?退")
        val REFUND_PATTERN2 = Regex("退.*?[¥￥](\\d+\\.?\\d*)")

        // 通用金额
        val AMOUNT_PATTERN = Regex("[¥￥](\\d+\\.?\\d*)")
    }

    override fun canHandleNotification(packageName: String): Boolean {
        return packageName == MEITUAN_PACKAGE || packageName == MEITUAN_TAKEOUT_PACKAGE
    }

    override fun canHandleSms(sender: String): Boolean = false

    override fun parseNotification(title: String, content: String, packageName: String): BillEntity? {
        if (!canHandleNotification(packageName)) return null

        val isTakeout = packageName == MEITUAN_TAKEOUT_PACKAGE || content.contains("外卖")

        // 尝试解析退款
        if (content.contains("退款") || content.contains("退回")) {
            REFUND_PATTERN.find(content)?.let { match ->
                val amount = match.groupValues[1].toDoubleOrNull() ?: return null
                return createBill(amount, BillType.INCOME, BillCategory.REFUND, "美团退款", title, content)
            }
            REFUND_PATTERN2.find(content)?.let { match ->
                val amount = match.groupValues[1].toDoubleOrNull() ?: return null
                return createBill(amount, BillType.INCOME, BillCategory.REFUND, "美团退款", title, content)
            }
        }

        // 尝试解析支付（按优先级）
        val payPatterns = listOf(PAY_PATTERN1, PAY_PATTERN2, PAY_PATTERN3, PAY_PATTERN4)
        for (pattern in payPatterns) {
            pattern.find(content)?.let { match ->
                val amount = match.groupValues[1].toDoubleOrNull() ?: return@let
                val category = if (isTakeout) BillCategory.FOOD else guessCategory(content)
                val desc = if (isTakeout) "美团外卖" else "美团消费"
                return createBill(-amount, BillType.EXPENSE, category, desc, title, content)
            }
        }

        // 兜底：有金额就提取
        if (content.contains("支付") || content.contains("付款") || content.contains("订单")) {
            AMOUNT_PATTERN.find(content)?.let { match ->
                val amount = match.groupValues[1].toDoubleOrNull() ?: return null
                val category = if (isTakeout) BillCategory.FOOD else guessCategory(content)
                return createBill(-amount, BillType.EXPENSE, category, "美团消费", title, content)
            }
        }

        return null
    }

    override fun parseSms(sender: String, content: String): BillEntity? = null

    private fun createBill(
        amount: Double,
        type: BillType,
        category: BillCategory,
        description: String,
        title: String,
        content: String
    ): BillEntity {
        return BillEntity(
            amount = amount,
            type = type,
            source = BillSource.MEITUAN,
            category = category,
            description = description,
            counterparty = "美团",
            rawContent = "$title|$content"
        )
    }

    private fun guessCategory(content: String): BillCategory {
        return when {
            content.containsAny("外卖", "餐", "食", "饮") -> BillCategory.FOOD
            content.containsAny("酒店", "住宿", "民宿") -> BillCategory.HOUSING
            content.containsAny("打车", "骑车", "出行", "单车") -> BillCategory.TRANSPORT
            content.containsAny("电影", "演出", "门票", "娱乐") -> BillCategory.ENTERTAINMENT
            content.containsAny("美容", "美发", "丽人") -> BillCategory.BEAUTY
            else -> BillCategory.OTHER
        }
    }

    private fun String.containsAny(vararg keywords: String): Boolean {
        return keywords.any { this.contains(it) }
    }
}
