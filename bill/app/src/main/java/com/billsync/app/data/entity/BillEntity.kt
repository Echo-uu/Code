package com.billsync.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 账单来源枚举
 */
enum class BillSource(val displayName: String) {
    WECHAT("微信"),
    ALIPAY("支付宝"),
    MEITUAN("美团"),
    CMB_DEBIT("招商银行储蓄卡"),
    CMB_CREDIT("招商银行信用卡"),
    CCB_DEBIT("建设银行储蓄卡"),
    CCB_CREDIT("建设银行信用卡"),
    MANUAL("手动录入"),
    CSV_IMPORT("CSV导入")
}

/**
 * 账单类型枚举
 */
enum class BillType(val displayName: String) {
    EXPENSE("支出"),
    INCOME("收入"),
    TRANSFER("转账")
}

/**
 * 账单分类枚举
 */
enum class BillCategory(val displayName: String, val icon: String) {
    FOOD("餐饮", "🍔"),
    TRANSPORT("交通", "🚗"),
    SHOPPING("购物", "🛒"),
    ENTERTAINMENT("娱乐", "🎮"),
    HOUSING("住房", "🏠"),
    MEDICAL("医疗", "🏥"),
    EDUCATION("教育", "📚"),
    COMMUNICATION("通讯", "📱"),
    CLOTHING("服饰", "👔"),
    BEAUTY("美容", "💄"),
    SOCIAL("社交", "🤝"),
    TRAVEL("旅行", "✈️"),
    SALARY("工资", "💰"),
    BONUS("奖金", "🎁"),
    RED_PACKET("红包", "🧧"),
    REFUND("退款", "↩️"),
    TRANSFER("转账", "🔄"),
    OTHER("其他", "📋")
}

/**
 * 账单实体类
 */
@Entity(tableName = "bills")
data class BillEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** 金额（单位：元，正数表示收入，负数表示支出） */
    val amount: Double,

    /** 账单类型 */
    val type: BillType,

    /** 账单来源 */
    val source: BillSource,

    /** 账单分类 */
    val category: BillCategory = BillCategory.OTHER,

    /** 交易描述/备注 */
    val description: String = "",

    /** 交易对方 */
    val counterparty: String = "",

    /** 交易时间（毫秒时间戳） */
    val timestamp: Long = System.currentTimeMillis(),

    /** 原始通知/短信内容（用于调试和去重） */
    val rawContent: String = "",

    /** 是否已确认（自动导入的账单可能需要用户确认） */
    val isConfirmed: Boolean = false,

    /** 创建时间 */
    val createdAt: Long = System.currentTimeMillis(),

    /** 唯一标识符（用于去重：source + timestamp + amount 的哈希） */
    val uniqueHash: String = ""
)
