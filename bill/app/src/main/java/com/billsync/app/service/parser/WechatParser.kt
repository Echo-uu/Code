package com.billsync.app.service.parser

import com.billsync.app.data.entity.*

/**
 * 微信账单解析器
 *
 * 微信支付通知格式示例：
 * - 标题: "微信支付"
 * - 内容: "微信支付收款到账¥10.00" (收款)
 * - 内容: "你已成功向XXX付款¥25.50" (付款)
 * - 内容: "微信红包 你收到了¥5.00的红包" (红包收入)
 * - 内容: "XXX]¥15.00" (付款通知简化版)
 */
class WechatParser : BillParser {

    companion object {
        const val WECHAT_PACKAGE = "com.tencent.mm"

        // 收款正则
        val INCOME_PATTERN = Regex("收款到账[¥￥](\\d+\\.?\\d*)")
        // 红包收入
        val RED_PACKET_INCOME = Regex("收到.*?[¥￥](\\d+\\.?\\d*).*?红包")
        val RED_PACKET_INCOME2 = Regex("红包.*?[¥￥](\\d+\\.?\\d*)")
        // 付款正则
        val EXPENSE_PATTERN = Regex("向(.+?)付款[¥￥](\\d+\\.?\\d*)")
        // 简化付款通知 "商家名]¥金额"
        val EXPENSE_SIMPLE = Regex("(.+?)][¥￥](\\d+\\.?\\d*)")
        // 通用金额提取
        val AMOUNT_PATTERN = Regex("[¥￥](\\d+\\.?\\d*)")
    }

    override fun canHandleNotification(packageName: String): Boolean {
        return packageName == WECHAT_PACKAGE
    }

    override fun canHandleSms(sender: String): Boolean = false

    override fun parseNotification(title: String, content: String, packageName: String): BillEntity? {
        if (packageName != WECHAT_PACKAGE) return null

        // 尝试解析收款
        INCOME_PATTERN.find(content)?.let { match ->
            val amount = match.groupValues[1].toDoubleOrNull() ?: return null
            return BillEntity(
                amount = amount,
                type = BillType.INCOME,
                source = BillSource.WECHAT,
                category = BillCategory.OTHER,
                description = "微信收款",
                counterparty = "",
                rawContent = "$title|$content"
            )
        }

        // 尝试解析红包收入
        RED_PACKET_INCOME.find(content)?.let { match ->
            val amount = match.groupValues[1].toDoubleOrNull() ?: return null
            return BillEntity(
                amount = amount,
                type = BillType.INCOME,
                source = BillSource.WECHAT,
                category = BillCategory.RED_PACKET,
                description = "微信红包",
                counterparty = "",
                rawContent = "$title|$content"
            )
        }

        RED_PACKET_INCOME2.find(content)?.let { match ->
            val amount = match.groupValues[1].toDoubleOrNull() ?: return null
            return BillEntity(
                amount = amount,
                type = BillType.INCOME,
                source = BillSource.WECHAT,
                category = BillCategory.RED_PACKET,
                description = "微信红包",
                counterparty = "",
                rawContent = "$title|$content"
            )
        }

        // 尝试解析付款（带商家名）
        EXPENSE_PATTERN.find(content)?.let { match ->
            val counterparty = match.groupValues[1].trim()
            val amount = match.groupValues[2].toDoubleOrNull() ?: return null
            return BillEntity(
                amount = -amount,
                type = BillType.EXPENSE,
                source = BillSource.WECHAT,
                category = guessCategory(counterparty, content),
                description = "微信支付",
                counterparty = counterparty,
                rawContent = "$title|$content"
            )
        }

        // 尝试简化付款格式
        EXPENSE_SIMPLE.find(content)?.let { match ->
            val counterparty = match.groupValues[1].trim()
            val amount = match.groupValues[2].toDoubleOrNull() ?: return null
            return BillEntity(
                amount = -amount,
                type = BillType.EXPENSE,
                source = BillSource.WECHAT,
                category = guessCategory(counterparty, content),
                description = "微信支付",
                counterparty = counterparty,
                rawContent = "$title|$content"
            )
        }

        // 兜底：只要包含金额就尝试提取
        if (title.contains("微信支付") || title.contains("微信")) {
            AMOUNT_PATTERN.find(content)?.let { match ->
                val amount = match.groupValues[1].toDoubleOrNull() ?: return null
                val isIncome = content.contains("收") || content.contains("到账")
                return BillEntity(
                    amount = if (isIncome) amount else -amount,
                    type = if (isIncome) BillType.INCOME else BillType.EXPENSE,
                    source = BillSource.WECHAT,
                    category = BillCategory.OTHER,
                    description = content.take(50),
                    counterparty = "",
                    rawContent = "$title|$content"
                )
            }
        }

        return null
    }

    override fun parseSms(sender: String, content: String): BillEntity? = null

    /**
     * 根据商家名和内容猜测消费分类
     */
    private fun guessCategory(counterparty: String, content: String): BillCategory {
        val text = "$counterparty $content"
        return when {
            text.containsAny("餐", "饭", "食", "吃", "外卖", "美食", "饮", "咖啡", "奶茶", "面", "饺", "烧烤") -> BillCategory.FOOD
            text.containsAny("打车", "出行", "滴滴", "公交", "地铁", "出租", "高铁", "火车", "飞机", "加油") -> BillCategory.TRANSPORT
            text.containsAny("超市", "商城", "购物", "淘宝", "京东", "拼多多", "天猫", "便利店") -> BillCategory.SHOPPING
            text.containsAny("电影", "游戏", "KTV", "娱乐", "演出", "门票") -> BillCategory.ENTERTAINMENT
            text.containsAny("房租", "水电", "物业", "燃气", "暖气") -> BillCategory.HOUSING
            text.containsAny("医院", "药", "诊所", "医疗", "挂号") -> BillCategory.MEDICAL
            text.containsAny("学费", "培训", "课程", "书", "教育") -> BillCategory.EDUCATION
            text.containsAny("话费", "流量", "宽带", "移动", "联通", "电信") -> BillCategory.COMMUNICATION
            text.containsAny("衣", "服装", "鞋", "包") -> BillCategory.CLOTHING
            text.containsAny("美容", "理发", "护肤", "化妆") -> BillCategory.BEAUTY
            text.containsAny("红包") -> BillCategory.RED_PACKET
            else -> BillCategory.OTHER
        }
    }

    private fun String.containsAny(vararg keywords: String): Boolean {
        return keywords.any { this.contains(it) }
    }
}
