package com.billsync.app.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.billsync.app.data.entity.BillCategory
import com.billsync.app.data.entity.BillType
import com.billsync.app.ui.theme.ExpenseRed
import com.billsync.app.ui.theme.IncomeGreen
import com.billsync.app.viewmodel.BillViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBillScreen(
    viewModel: BillViewModel,
    onNavigateBack: () -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var counterparty by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(BillType.EXPENSE) }
    var selectedCategory by remember { mutableStateOf(BillCategory.OTHER) }
    var showError by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("手动记账") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val amountValue = amount.toDoubleOrNull()
                            if (amountValue != null && amountValue > 0) {
                                viewModel.addManualBill(
                                    amount = amountValue,
                                    type = selectedType,
                                    category = selectedCategory,
                                    description = description.ifBlank { selectedCategory.displayName },
                                    counterparty = counterparty
                                )
                                onNavigateBack()
                            } else {
                                showError = true
                            }
                        }
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = "保存")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 收支类型选择
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                BillType.entries.forEach { type ->
                    FilterChip(
                        selected = selectedType == type,
                        onClick = { selectedType = type },
                        label = {
                            Text(
                                text = type.displayName,
                                fontWeight = if (selectedType == type) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = when (type) {
                                BillType.EXPENSE -> ExpenseRed.copy(alpha = 0.15f)
                                BillType.INCOME -> IncomeGreen.copy(alpha = 0.15f)
                                BillType.TRANSFER -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            }
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 金额输入
            OutlinedTextField(
                value = amount,
                onValueChange = {
                    if (it.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                        amount = it
                        showError = false
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("金额") },
                prefix = { Text("¥ ") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = showError,
                supportingText = if (showError) {
                    { Text("请输入有效金额") }
                } else null,
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // 描述输入
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("描述（可选）") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // 交易对方
            OutlinedTextField(
                value = counterparty,
                onValueChange = { counterparty = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("交易对方（可选）") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // 分类选择
            Text(
                text = "选择分类",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            val categories = if (selectedType == BillType.INCOME) {
                listOf(
                    BillCategory.SALARY, BillCategory.BONUS, BillCategory.RED_PACKET,
                    BillCategory.REFUND, BillCategory.TRANSFER, BillCategory.OTHER
                )
            } else {
                listOf(
                    BillCategory.FOOD, BillCategory.TRANSPORT, BillCategory.SHOPPING,
                    BillCategory.ENTERTAINMENT, BillCategory.HOUSING, BillCategory.MEDICAL,
                    BillCategory.EDUCATION, BillCategory.COMMUNICATION, BillCategory.CLOTHING,
                    BillCategory.BEAUTY, BillCategory.SOCIAL, BillCategory.TRAVEL,
                    BillCategory.TRANSFER, BillCategory.OTHER
                )
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(categories) { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        label = {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = category.icon,
                                    fontSize = 20.sp
                                )
                                Text(
                                    text = category.displayName,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
