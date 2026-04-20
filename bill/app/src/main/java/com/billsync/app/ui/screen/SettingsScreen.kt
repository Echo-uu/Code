package com.billsync.app.ui.screen

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.billsync.app.service.BillNotificationListenerService
import com.billsync.app.service.CsvSource
import com.billsync.app.viewmodel.BillViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: BillViewModel) {
    val context = LocalContext.current
    val importResult by viewModel.importResult.collectAsState()
    val billCount by viewModel.billCount.collectAsState()

    // 权限状态
    var notificationPermissionGranted by remember {
        mutableStateOf(isNotificationListenerEnabled(context))
    }
    var smsPermissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_SMS) ==
                    PackageManager.PERMISSION_GRANTED
        )
    }

    // SMS 权限请求
    val smsPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        smsPermissionGranted = permissions[Manifest.permission.RECEIVE_SMS] == true
    }

    // CSV 文件选择
    var selectedCsvSource by remember { mutableStateOf<CsvSource?>(null) }
    val csvFilePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            selectedCsvSource?.let { source ->
                viewModel.importCsv(uri, source)
            }
        }
    }

    // 导入结果对话框
    importResult?.let { result ->
        AlertDialog(
            onDismissRequest = { viewModel.clearImportResult() },
            title = { Text("导入结果") },
            text = {
                Column {
                    Text("总计：${result.total} 条")
                    Text("成功：${result.success} 条")
                    Text("重复（已跳过）：${result.duplicate} 条")
                    if (result.error > 0) {
                        Text("失败：${result.error} 条")
                    }
                    result.errorMessage?.let {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("错误信息：$it", color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.clearImportResult() }) {
                    Text("确定")
                }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("设置") }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 数据概览
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "已记录账单",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$billCount 笔",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Icon(
                            Icons.Filled.Receipt,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                        )
                    }
                }
            }

            // 权限设置标题
            item {
                Text(
                    text = "权限设置",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // 通知读取权限
            item {
                SettingCard(
                    icon = Icons.Filled.Notifications,
                    title = "通知读取权限",
                    description = "监听微信、支付宝、美团、银行App的支付通知",
                    isEnabled = notificationPermissionGranted,
                    onClick = {
                        // 跳转到系统通知访问权限设置
                        context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                    },
                    actionText = if (notificationPermissionGranted) "已开启" else "去设置"
                )
            }

            // 短信权限
            item {
                SettingCard(
                    icon = Icons.Filled.Sms,
                    title = "短信读取权限",
                    description = "读取招商银行、建设银行交易短信通知",
                    isEnabled = smsPermissionGranted,
                    onClick = {
                        smsPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.RECEIVE_SMS,
                                Manifest.permission.READ_SMS
                            )
                        )
                    },
                    actionText = if (smsPermissionGranted) "已授权" else "去授权"
                )
            }

            // 手动导入标题
            item {
                Text(
                    text = "手动导入",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // 导入微信账单
            item {
                SettingCard(
                    icon = Icons.Filled.FileUpload,
                    title = "导入微信账单",
                    description = "从微信导出的CSV文件导入账单",
                    onClick = {
                        selectedCsvSource = CsvSource.WECHAT
                        csvFilePicker.launch(arrayOf("text/*", "application/csv", "*/*"))
                    },
                    actionText = "选择文件"
                )
            }

            // 导入支付宝账单
            item {
                SettingCard(
                    icon = Icons.Filled.FileUpload,
                    title = "导入支付宝账单",
                    description = "从支付宝导出的CSV文件导入账单",
                    onClick = {
                        selectedCsvSource = CsvSource.ALIPAY
                        csvFilePicker.launch(arrayOf("text/*", "application/csv", "*/*"))
                    },
                    actionText = "选择文件"
                )
            }

            // 同步状态
            item {
                Text(
                    text = "监听状态",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // 各平台同步状态
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SyncStatusItem("微信支付", notificationPermissionGranted, "通知监听")
                        SyncStatusItem("支付宝", notificationPermissionGranted, "通知监听")
                        SyncStatusItem("美团/美团外卖", notificationPermissionGranted, "通知监听")
                        SyncStatusItem("招商银行", notificationPermissionGranted && smsPermissionGranted, "通知+短信")
                        SyncStatusItem("建设银行", notificationPermissionGranted && smsPermissionGranted, "通知+短信")
                    }
                }
            }

            // 使用说明
            item {
                Text(
                    text = "使用说明",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        HelpItem("1️⃣", "开启「通知读取权限」后，App 会自动监听微信、支付宝等应用的支付通知")
                        HelpItem("2️⃣", "开启「短信权限」后，App 会自动读取银行交易短信（招商/建设银行）")
                        HelpItem("3️⃣", "你也可以从微信/支付宝手动导出CSV账单文件进行导入")
                        HelpItem("4️⃣", "所有数据仅存储在本地，不会上传至任何服务器")
                        HelpItem("⚠️", "请确保微信/支付宝/银行App的通知未被系统静默或关闭")
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    isEnabled: Boolean = false,
    onClick: () -> Unit,
    actionText: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                tint = if (isEnabled) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            TextButton(onClick = onClick) {
                Text(
                    text = actionText,
                    color = if (isEnabled) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Composable
private fun SyncStatusItem(name: String, isActive: Boolean, method: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                if (isActive) Icons.Filled.CheckCircle else Icons.Filled.Cancel,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = if (isActive) com.billsync.app.ui.theme.IncomeGreen
                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Text(
            text = method,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun HelpItem(emoji: String, text: String) {
    Row {
        Text(text = emoji)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 检查通知监听权限是否已开启
 */
private fun isNotificationListenerEnabled(context: Context): Boolean {
    val cn = ComponentName(context, BillNotificationListenerService::class.java)
    val flat = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
    return flat?.contains(cn.flattenToString()) == true
}
