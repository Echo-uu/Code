package com.billsync.app.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.billsync.app.data.database.CategorySum
import com.billsync.app.data.entity.BillCategory
import com.billsync.app.ui.theme.*
import com.billsync.app.viewmodel.BillViewModel
import com.billsync.app.viewmodel.TimeRange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(viewModel: BillViewModel) {
    val totalExpense by viewModel.totalExpense.collectAsState()
    val totalIncome by viewModel.totalIncome.collectAsState()
    val expenseByCategory by viewModel.expenseByCategory.collectAsState()
    val timeRange by viewModel.timeRange.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("统计分析") }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 时间范围选择
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TimeRange.entries.forEach { range ->
                        FilterChip(
                            selected = timeRange == range,
                            onClick = { viewModel.setTimeRange(range) },
                            label = { Text(range.displayName) }
                        )
                    }
                }
            }

            // 收支概览
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 总支出卡片
                    SummaryCard(
                        title = "总支出",
                        amount = totalExpense,
                        color = ExpenseRed,
                        modifier = Modifier.weight(1f)
                    )
                    // 总收入卡片
                    SummaryCard(
                        title = "总收入",
                        amount = totalIncome,
                        color = IncomeGreen,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 结余卡片
            item {
                val balance = totalIncome - totalExpense
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (balance >= 0)
                            IncomeGreen.copy(alpha = 0.1f)
                        else
                            ExpenseRed.copy(alpha = 0.1f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "结余",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${if (balance >= 0) "+" else ""}¥${"%.2f".format(balance)}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (balance >= 0) IncomeGreen else ExpenseRed
                        )
                    }
                }
            }

            // 支出分类排行
            item {
                Text(
                    text = "支出分类排行",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (expenseByCategory.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "暂无支出数据",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(expenseByCategory) { categoryStat ->
                    CategoryStatItem(
                        categoryStat = categoryStat,
                        totalExpense = totalExpense
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(
    title: String,
    amount: Double,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = color.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "¥${"%.2f".format(amount)}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
private fun CategoryStatItem(
    categoryStat: CategorySum,
    totalExpense: Double
) {
    val percentage = if (totalExpense > 0) categoryStat.total / totalExpense else 0.0

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
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
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(getCategoryColor(categoryStat.category).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = categoryStat.category.icon,
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = categoryStat.category.displayName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "¥${"%.2f".format(categoryStat.total)}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = ExpenseRed
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 进度条
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LinearProgressIndicator(
                        progress = { percentage.toFloat() },
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = getCategoryColor(categoryStat.category),
                        trackColor = getCategoryColor(categoryStat.category).copy(alpha = 0.1f),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${"%.1f".format(percentage * 100)}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private fun getCategoryColor(category: BillCategory): Color {
    return when (category) {
        BillCategory.FOOD -> CategoryFood
        BillCategory.TRANSPORT -> CategoryTransport
        BillCategory.SHOPPING -> CategoryShopping
        BillCategory.ENTERTAINMENT -> CategoryEntertainment
        BillCategory.HOUSING -> CategoryHousing
        BillCategory.MEDICAL -> CategoryMedical
        BillCategory.EDUCATION -> CategoryEducation
        else -> CategoryOther
    }
}
