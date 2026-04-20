package com.billsync.app.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.billsync.app.data.entity.BillSource
import com.billsync.app.ui.components.BillCard
import com.billsync.app.viewmodel.BillViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillListScreen(viewModel: BillViewModel) {
    val allBills by viewModel.allBills.collectAsState()
    val searchKeyword by viewModel.searchKeyword.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val selectedSource by viewModel.selectedSource.collectAsState()

    var showSearch by remember { mutableStateOf(false) }

    // 根据筛选条件过滤账单
    val filteredBills = remember(allBills, selectedSource, searchKeyword, searchResults) {
        when {
            searchKeyword.isNotBlank() -> searchResults
            selectedSource != null -> allBills.filter { it.source == selectedSource }
            else -> allBills
        }
    }

    // 按日期分组
    val groupedBills = remember(filteredBills) {
        filteredBills.groupBy { bill ->
            SimpleDateFormat("yyyy年MM月dd日", Locale.CHINA).format(Date(bill.timestamp))
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 顶部栏
        TopAppBar(
            title = { Text("账单明细") },
            actions = {
                IconButton(onClick = { showSearch = !showSearch }) {
                    Icon(
                        if (showSearch) Icons.Filled.Close else Icons.Filled.Search,
                        contentDescription = "搜索"
                    )
                }
            }
        )

        // 搜索框
        if (showSearch) {
            OutlinedTextField(
                value = searchKeyword,
                onValueChange = { viewModel.search(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("搜索账单...") },
                singleLine = true,
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchKeyword.isNotBlank()) {
                        IconButton(onClick = { viewModel.search("") }) {
                            Icon(Icons.Filled.Close, contentDescription = "清除")
                        }
                    }
                }
            )
        }

        // 来源筛选
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedSource == null,
                    onClick = { viewModel.setSourceFilter(null) },
                    label = { Text("全部") }
                )
            }
            items(
                items = listOf(
                    BillSource.WECHAT to "微信",
                    BillSource.ALIPAY to "支付宝",
                    BillSource.MEITUAN to "美团",
                    BillSource.CMB_DEBIT to "招行储蓄",
                    BillSource.CMB_CREDIT to "招行信用",
                    BillSource.CCB_DEBIT to "建行储蓄",
                    BillSource.CCB_CREDIT to "建行信用",
                    BillSource.MANUAL to "手动"
                )
            ) { (source, label) ->
                FilterChip(
                    selected = selectedSource == source,
                    onClick = {
                        viewModel.setSourceFilter(
                            if (selectedSource == source) null else source
                        )
                    },
                    label = { Text(label) }
                )
            }
        }

        // 账单列表
        if (groupedBills.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(48.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (searchKeyword.isNotBlank()) "未找到匹配的账单" else "暂无账单记录",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                groupedBills.forEach { (date, bills) ->
                    // 日期标题
                    item(key = "header_$date") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = date,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            // 当日小计
                            val dayExpense = bills
                                .filter { it.amount < 0 }
                                .sumOf { kotlin.math.abs(it.amount) }
                            if (dayExpense > 0) {
                                Text(
                                    text = "支出 ¥${"%.2f".format(dayExpense)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                    // 当日账单
                    items(
                        items = bills,
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
}
