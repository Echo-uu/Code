package com.billsync.app.service

import android.content.Context
import android.net.Uri
import com.billsync.app.data.entity.*
import com.billsync.app.data.repository.BillRepository
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * CSV 账单导入器
 *
 * 支持导入微信和支付宝导出的 CSV 格式账单文件
 *
 * 微信账单CSV格式（大致）：
 * 交易时间, 交易类型, 交易对方, 商品, 收/支, 金额(元), 支付方式, 当前状态, 交易单号, 商户单号, 备注
 *
 * 支付宝账单CSV格式（大致）：
 * 交易时间, 交易分类, 交易对方, 对方账号, 商品说明, 收/支, 金额, 收/付款方式, 交易状态, 交易订单号, 商家订单号, 备注
 */
class CsvImporter(
    private val context: Context,
    private val repository: BillRepository
) {

    companion object {
        private val DATE_FORMATS = listOf(
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA),
            SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.CHINA),
            SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA),
            SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.CHINA),
            SimpleDateFormat("yyyy年MM月dd日 HH:mm", Locale.CHINA)
        )
    }

    /**
     * 导入CSV文件
     * @return 成功导入的账单数量
     */
    suspend fun importCsv(uri: Uri, source: CsvSource): ImportResult {
        val bills = mutableListOf<BillEntity>()
        var totalLines = 0
        var errorLines = 0

        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val reader = BufferedReader(InputStreamReader(inputStream, "UTF-8"))

                // 跳过可能的 BOM 和说明行
                var headerFound = false
                var headerLine = ""

                reader.forEachLine { line ->
                    val trimmedLine = line.trim().removeBom()
                    if (trimmedLine.isBlank()) return@forEachLine

                    if (!headerFound) {
                        // 微信和支付宝的 CSV 文件前面可能有几行说明，找到含"交易时间"的行作为表头
                        if (trimmedLine.contains("交易时间") || trimmedLine.contains("交易号")) {
                            headerFound = true
                            headerLine = trimmedLine
                        }
                        return@forEachLine
                    }

                    totalLines++
                    try {
                        val bill = when (source) {
                            CsvSource.WECHAT -> parseWechatCsvLine(trimmedLine)
                            CsvSource.ALIPAY -> parseAlipayCsvLine(trimmedLine)
                        }
                        if (bill != null) {
                            bills.add(bill)
                        }
                    } catch (e: Exception) {
                        errorLines++
                    }
                }
            }

            // 批量插入
            val insertedIds = repository.insertAll(bills)
            val successCount = insertedIds.count { it > 0 }

            return ImportResult(
                total = totalLines,
                success = successCount,
                duplicate = bills.size - successCount,
                error = errorLines
            )
        } catch (e: Exception) {
            return ImportResult(total = 0, success = 0, duplicate = 0, error = 1, errorMessage = e.message)
        }
    }

    /**
     * 解析微信 CSV 行
     * 列顺序: 交易时间, 交易类型, 交易对方, 商品, 收/支, 金额(元), 支付方式, 当前状态, 交易单号, 商户单号, 备注
     */
    private fun parseWechatCsvLine(line: String): BillEntity? {
        val columns = parseCsvColumns(line)
        if (columns.size < 8) return null

        val timeStr = columns[0].trim()
        val transType = columns[1].trim()       // 商户消费 / 转账 等
        val counterparty = columns[2].trim()
        val product = columns[3].trim()
        val direction = columns[4].trim()       // 收入 / 支出 / /
        val amountStr = columns[5].trim().replace("¥", "").replace("￥", "").replace(",", "")
        val payMethod = columns[6].trim()
        val status = columns[7].trim()

        // 跳过不成功的交易
        if (status.contains("退款") || status.contains("已退") || status.contains("关闭")) {
            // 退款也记录
            if (!status.contains("退款成功") && !status.contains("已退款")) return null
        }

        val amount = amountStr.toDoubleOrNull() ?: return null
        val timestamp = parseDate(timeStr) ?: System.currentTimeMillis()

        val type = when {
            direction.contains("收入") -> BillType.INCOME
            direction.contains("支出") -> BillType.EXPENSE
            transType.contains("转账") -> BillType.TRANSFER
            else -> BillType.EXPENSE
        }

        return BillEntity(
            amount = if (type == BillType.EXPENSE) -amount else amount,
            type = type,
            source = BillSource.CSV_IMPORT,
            category = guessCategory(counterparty, product),
            description = product.ifBlank { transType },
            counterparty = counterparty,
            timestamp = timestamp,
            rawContent = "微信CSV|$line",
            isConfirmed = true
        )
    }

    /**
     * 解析支付宝 CSV 行
     * 列顺序: 交易时间, 交易分类, 交易对方, 对方账号, 商品说明, 收/支, 金额, 收/付款方式, 交易状态, 交易订单号, 商家订单号, 备注
     */
    private fun parseAlipayCsvLine(line: String): BillEntity? {
        val columns = parseCsvColumns(line)
        if (columns.size < 9) return null

        val timeStr = columns[0].trim()
        val category = columns[1].trim()
        val counterparty = columns[2].trim()
        val product = columns[4].trim()
        val direction = columns[5].trim()
        val amountStr = columns[6].trim().replace("¥", "").replace("￥", "").replace(",", "")
        val status = columns[8].trim()

        // 跳过关闭的交易
        if (status.contains("关闭") || status.contains("失败")) return null

        val amount = amountStr.toDoubleOrNull() ?: return null
        val timestamp = parseDate(timeStr) ?: System.currentTimeMillis()

        val type = when {
            direction.contains("收入") -> BillType.INCOME
            direction.contains("支出") -> BillType.EXPENSE
            else -> BillType.EXPENSE
        }

        return BillEntity(
            amount = if (type == BillType.EXPENSE) -amount else amount,
            type = type,
            source = BillSource.CSV_IMPORT,
            category = guessCategory(counterparty, "$product $category"),
            description = product.ifBlank { category },
            counterparty = counterparty,
            timestamp = timestamp,
            rawContent = "支付宝CSV|$line",
            isConfirmed = true
        )
    }

    /**
     * 解析 CSV 列（处理引号包裹和逗号转义）
     */
    private fun parseCsvColumns(line: String): List<String> {
        val columns = mutableListOf<String>()
        var current = StringBuilder()
        var inQuotes = false

        for (char in line) {
            when {
                char == '"' -> inQuotes = !inQuotes
                char == ',' && !inQuotes -> {
                    columns.add(current.toString())
                    current = StringBuilder()
                }
                else -> current.append(char)
            }
        }
        columns.add(current.toString())
        return columns
    }

    private fun parseDate(dateStr: String): Long? {
        for (format in DATE_FORMATS) {
            try {
                return format.parse(dateStr)?.time
            } catch (_: Exception) {
            }
        }
        return null
    }

    private fun guessCategory(counterparty: String, content: String): BillCategory {
        val text = "$counterparty $content"
        return when {
            text.containsAny("餐", "饭", "食", "外卖", "饮", "咖啡", "奶茶", "美团", "饿了么") -> BillCategory.FOOD
            text.containsAny("打车", "出行", "滴滴", "公交", "地铁", "加油", "高德") -> BillCategory.TRANSPORT
            text.containsAny("超市", "商城", "购物", "淘宝", "京东", "拼多多") -> BillCategory.SHOPPING
            text.containsAny("电影", "游戏", "娱乐", "门票") -> BillCategory.ENTERTAINMENT
            text.containsAny("房租", "水电", "物业") -> BillCategory.HOUSING
            text.containsAny("医院", "药", "医疗") -> BillCategory.MEDICAL
            text.containsAny("学费", "培训", "课程", "教育") -> BillCategory.EDUCATION
            text.containsAny("话费", "流量", "充值") -> BillCategory.COMMUNICATION
            text.containsAny("红包") -> BillCategory.RED_PACKET
            text.containsAny("转账") -> BillCategory.TRANSFER
            text.containsAny("退款") -> BillCategory.REFUND
            text.containsAny("工资", "薪") -> BillCategory.SALARY
            else -> BillCategory.OTHER
        }
    }

    private fun String.containsAny(vararg keywords: String): Boolean {
        return keywords.any { this.contains(it) }
    }

    private fun String.removeBom(): String {
        return if (this.startsWith("\uFEFF")) this.substring(1) else this
    }
}

enum class CsvSource {
    WECHAT,
    ALIPAY
}

data class ImportResult(
    val total: Int,
    val success: Int,
    val duplicate: Int,
    val error: Int,
    val errorMessage: String? = null
)
