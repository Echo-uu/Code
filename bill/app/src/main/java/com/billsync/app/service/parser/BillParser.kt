package com.billsync.app.service.parser

import com.billsync.app.data.entity.BillEntity

/**
 * 账单解析器接口
 * 所有平台的解析器都实现此接口
 */
interface BillParser {

    /**
     * 解析通知内容为账单实体
     * @param title 通知标题
     * @param content 通知内容
     * @param packageName 应用包名
     * @return 解析出的账单，解析失败返回 null
     */
    fun parseNotification(title: String, content: String, packageName: String): BillEntity?

    /**
     * 解析短信内容为账单实体
     * @param sender 发送者号码
     * @param content 短信内容
     * @return 解析出的账单，解析失败返回 null
     */
    fun parseSms(sender: String, content: String): BillEntity?

    /**
     * 判断是否可以处理该通知
     */
    fun canHandleNotification(packageName: String): Boolean

    /**
     * 判断是否可以处理该短信
     */
    fun canHandleSms(sender: String): Boolean
}

/**
 * 解析器管理器
 * 管理所有平台的解析器，根据通知/短信来源自动选择合适的解析器
 */
object BillParserManager {

    private val parsers: List<BillParser> = listOf(
        WechatParser(),
        AlipayParser(),
        MeituanParser(),
        CMBParser(),
        CCBParser()
    )

    /**
     * 解析通知
     */
    fun parseNotification(title: String, content: String, packageName: String): BillEntity? {
        for (parser in parsers) {
            if (parser.canHandleNotification(packageName)) {
                val bill = parser.parseNotification(title, content, packageName)
                if (bill != null) return bill
            }
        }
        return null
    }

    /**
     * 解析短信
     */
    fun parseSms(sender: String, content: String): BillEntity? {
        for (parser in parsers) {
            if (parser.canHandleSms(sender)) {
                val sms = parser.parseSms(sender, content)
                if (sms != null) return sms
            }
        }
        return null
    }
}
