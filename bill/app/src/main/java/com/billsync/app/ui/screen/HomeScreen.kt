package com.billsync.app.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.billsync.app.data.entity.BillEntity
import com.billsync.app.data.entity.BillType
import com.billsync.app.ui.components.BillCard
import com.billsync.app.ui.theme.*
import com.billsync.app.viewmodel.BillViewModel
import com.billsync.app.viewmodel.TimeRange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: BillViewModel,
    onAddBill: () -> Unit = {}
) {
    val bills by viewModel.allBills.collectAsState()
    val totalExpense by viewModel.totalExpense.collectAsState()
    val totalIncome by viewModel.totalIncome.collectAsState()
    val timeRange by viewModel.timeRange.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddBill,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "添加账单")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // 概览卡片
            item {
                OverviewCard(
                    totalExpense = totalExpense,
                    totalIncome = totalIncome,
                    timeRange = timeRange,
                    onTimeRangeChange = { viewModel.setTimeRange(it) }
                )
            }

            // 最近账单标题
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "最近账单",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "共 ${bills.size} 笔",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 账单列表
            if (bills.isEmpty()) {
                item {
                    EmptyState()
                }
            } else {
                items(
                    items = bills.take(50),
                    key = { it.id }
                ) { bill ->
                    BillCard(
                        bill = bill,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun OverviewCard(
    totalExpense: Double,
    totalIncome: Double,
    timeRange: TimeRange,
    onTimeRangeChange: (TimeRange) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(Primary, PrimaryDark)
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                // 时间范围选择
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TimeRange.entries.forEach { range ->
                        FilterChip(
                            selected = timeRange == range,
                            onClick = { onTimeRangeChange(range) },
                            label = {
                                Text(
                                    text = range.displayName,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (timeRange == range)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        Surface.copy(alpha = 0.9f)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = Surface.copy(alpha = 0.15f),
                                selectedContainerColor = Surface
                            ),
                            border = null
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 收支汇总
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // 支出
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.TrendingDown,
                                contentDescription = null,
                                tint = Surface.copy(alpha = 0.7f),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "支出",
                                style = MaterialTheme.typography.bodySmall,
                                color = Surface.copy(alpha = 0.7f)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "¥${"%.2f".format(totalExpense)}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Surface
                        )
                    }

                    // 收入
                    Column(horizontalAlignment = Alignment.End) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.TrendingUp,
                                contentDescription = null,
                                tint = Surface.copy(alpha = 0.7f),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "收入",
                                style = MaterialTheme.typography.bodySmall,
                                color = Surface.copy(alpha = 0.7f)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "¥${"%.2f".format(totalIncome)}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Surface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 结余
                val balance = totalIncome - totalExpense
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Surface.copy(alpha = 0.15f))
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "结余",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Surface.copy(alpha = 0.8f)
                        )
                        Text(
                            text = "${if (balance >= 0) "+" else ""}¥${"%.2f".format(balance)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (balance >= 0) IncomeGreen else ExpenseRed
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(48.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "📋",
                style = MaterialTheme.typography.displayLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "暂无账单记录",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "开启通知监听后，账单会自动同步\n你也可以手动添加或导入CSV账单",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}
