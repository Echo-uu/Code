package com.billsync.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.billsync.app.data.entity.BillEntity
import com.billsync.app.data.entity.BillSource
import com.billsync.app.data.entity.BillType
import com.billsync.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun BillCard(
    bill: BillEntity,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        onClick = { onClick?.invoke() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 分类图标
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(getCategoryColor(bill).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = bill.category.icon,
                    fontSize = 20.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 描述和来源
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = bill.description.ifBlank { bill.category.displayName },
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 来源标签
                    SourceTag(source = bill.source)
                    Spacer(modifier = Modifier.width(8.dp))
                    // 交易对方
                    if (bill.counterparty.isNotBlank()) {
                        Text(
                            text = bill.counterparty,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    // 时间
                    Text(
                        text = formatTime(bill.timestamp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 金额
            Text(
                text = formatAmount(bill),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = when (bill.type) {
                    BillType.EXPENSE -> ExpenseRed
                    BillType.INCOME -> IncomeGreen
                    BillType.TRANSFER -> TransferBlue
                }
            )
        }
    }
}

@Composable
fun SourceTag(source: BillSource) {
    val color = when (source) {
        BillSource.WECHAT -> WechatGreen
        BillSource.ALIPAY -> AlipayBlue
        BillSource.MEITUAN -> MeituanYellow
        BillSource.CMB_DEBIT, BillSource.CMB_CREDIT -> CMBRed
        BillSource.CCB_DEBIT, BillSource.CCB_CREDIT -> CCBBlue
        else -> CategoryOther
    }

    val label = when (source) {
        BillSource.WECHAT -> "微信"
        BillSource.ALIPAY -> "支付宝"
        BillSource.MEITUAN -> "美团"
        BillSource.CMB_DEBIT -> "招行储"
        BillSource.CMB_CREDIT -> "招行信"
        BillSource.CCB_DEBIT -> "建行储"
        BillSource.CCB_CREDIT -> "建行信"
        BillSource.MANUAL -> "手动"
        BillSource.CSV_IMPORT -> "导入"
    }

    Surface(
        shape = RoundedCornerShape(4.dp),
        color = color.copy(alpha = 0.12f),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun getCategoryColor(bill: BillEntity): androidx.compose.ui.graphics.Color {
    return when (bill.category) {
        com.billsync.app.data.entity.BillCategory.FOOD -> CategoryFood
        com.billsync.app.data.entity.BillCategory.TRANSPORT -> CategoryTransport
        com.billsync.app.data.entity.BillCategory.SHOPPING -> CategoryShopping
        com.billsync.app.data.entity.BillCategory.ENTERTAINMENT -> CategoryEntertainment
        com.billsync.app.data.entity.BillCategory.HOUSING -> CategoryHousing
        com.billsync.app.data.entity.BillCategory.MEDICAL -> CategoryMedical
        com.billsync.app.data.entity.BillCategory.EDUCATION -> CategoryEducation
        else -> CategoryOther
    }
}

private fun formatAmount(bill: BillEntity): String {
    val absAmount = kotlin.math.abs(bill.amount)
    val prefix = when (bill.type) {
        BillType.EXPENSE -> "-"
        BillType.INCOME -> "+"
        BillType.TRANSFER -> ""
    }
    return "$prefix¥${"%.2f".format(absAmount)}"
}

private val timeFormat = SimpleDateFormat("HH:mm", Locale.CHINA)
private val dateFormat = SimpleDateFormat("MM/dd", Locale.CHINA)

private fun formatTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = timestamp

    val today = Calendar.getInstance()
    return when {
        // 今天：显示时间
        calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) -> {
            timeFormat.format(Date(timestamp))
        }
        // 昨天
        diff < 2 * 24 * 60 * 60 * 1000L -> "昨天"
        // 今年内：显示月/日
        calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) -> {
            dateFormat.format(Date(timestamp))
        }
        // 其他：显示年/月/日
        else -> {
            SimpleDateFormat("yyyy/MM/dd", Locale.CHINA).format(Date(timestamp))
        }
    }
}
