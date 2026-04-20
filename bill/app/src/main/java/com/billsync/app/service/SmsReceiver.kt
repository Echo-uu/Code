package com.billsync.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import com.billsync.app.data.database.AppDatabase
import com.billsync.app.data.repository.BillRepository
import com.billsync.app.service.parser.BillParserManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * 短信广播接收器
 *
 * 监听银行交易短信，自动解析为账单记录
 * 主要处理招商银行(95555)和建设银行(95533)的交易通知短信
 *
 * 需要 RECEIVE_SMS 和 READ_SMS 权限
 */
class SmsReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "SmsReceiver"

        // 需要监听的银行短信发送号码关键字
        val BANK_KEYWORDS = listOf(
            "95555",    // 招商银行
            "95533",    // 建设银行
            "招商银行",
            "建设银行"
        )
    }

    private val receiverJob = SupervisorJob()
    private val receiverScope = CoroutineScope(Dispatchers.IO + receiverJob)

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (messages.isNullOrEmpty()) return

        val database = AppDatabase.getDatabase(context.applicationContext)
        val repository = BillRepository(database.billDao())

        // 将同一发送者的多条短信片段合并
        val messageMap = mutableMapOf<String, StringBuilder>()
        for (message in messages) {
            val sender = message.originatingAddress ?: continue
            val body = message.messageBody ?: continue
            messageMap.getOrPut(sender) { StringBuilder() }.append(body)
        }

        for ((sender, bodyBuilder) in messageMap) {
            val body = bodyBuilder.toString()

            // 只处理银行相关短信
            val isBankSms = BANK_KEYWORDS.any { sender.contains(it) || body.contains(it) }
            if (!isBankSms) continue

            Log.d(TAG, "收到银行短信 [发送者: $sender] 内容: $body")

            // 使用解析器解析短信
            val bill = BillParserManager.parseSms(sender, body)

            if (bill != null) {
                receiverScope.launch {
                    try {
                        val result = repository.insert(bill)
                        if (result > 0) {
                            Log.i(TAG, "成功从短信记录账单: ${bill.description} ${bill.amount}元 [${bill.source.displayName}]")
                        } else {
                            Log.d(TAG, "短信账单已存在（去重）: ${bill.description}")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "保存短信账单失败", e)
                    }
                }
            } else {
                Log.d(TAG, "短信未能解析为账单: $body")
            }
        }
    }
}
