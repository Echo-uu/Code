package com.billsync.app.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.billsync.app.data.database.AppDatabase
import com.billsync.app.data.repository.BillRepository
import com.billsync.app.service.parser.BillParserManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * 通知监听服务
 *
 * 监听系统通知栏，自动解析来自微信、支付宝、美团、银行等应用的支付通知
 *
 * 需要用户在系统设置中手动授权通知读取权限：
 * 设置 -> 通知访问权限 / 通知使用权 -> 账单同步 -> 开启
 */
class BillNotificationListenerService : NotificationListenerService() {

    companion object {
        private const val TAG = "BillNotificationService"

        // 需要监听的应用包名
        val MONITORED_PACKAGES = setOf(
            "com.tencent.mm",                    // 微信
            "com.eg.android.AlipayGphone",       // 支付宝
            "com.sankuai.meituan",               // 美团
            "com.sankuai.meituan.takeoutnew",    // 美团外卖
            "cmb.pb",                            // 招商银行
            "com.chinamworld.main",              // 掌上生活（招行信用卡）
            "com.chinamworld.bocmbci"            // 建设银行
        )
    }

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    private lateinit var repository: BillRepository

    override fun onCreate() {
        super.onCreate()
        val database = AppDatabase.getDatabase(applicationContext)
        repository = BillRepository(database.billDao())
        Log.i(TAG, "通知监听服务已启动")
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
        Log.i(TAG, "通知监听服务已停止")
    }

    /**
     * 当新通知到达时调用
     */
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        sbn ?: return

        val packageName = sbn.packageName ?: return

        // 只处理我们关注的应用通知
        if (packageName !in MONITORED_PACKAGES) return

        val notification = sbn.notification ?: return
        val extras = notification.extras ?: return

        val title = extras.getCharSequence("android.title")?.toString() ?: ""
        val content = extras.getCharSequence("android.text")?.toString() ?: ""

        if (content.isBlank()) return

        Log.d(TAG, "收到通知 [$packageName] 标题: $title, 内容: $content")

        // 使用解析器解析通知内容
        val bill = BillParserManager.parseNotification(title, content, packageName)

        if (bill != null) {
            serviceScope.launch {
                try {
                    val result = repository.insert(bill)
                    if (result > 0) {
                        Log.i(TAG, "成功记录账单: ${bill.description} ${bill.amount}元 [${bill.source.displayName}]")
                    } else {
                        Log.d(TAG, "账单已存在（去重）: ${bill.description}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "保存账单失败", e)
                }
            }
        } else {
            Log.d(TAG, "通知未能解析为账单: $content")
        }
    }

    /**
     * 当通知被移除时调用（通常不需要处理）
     */
    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        // 不需要处理通知移除
    }
}
