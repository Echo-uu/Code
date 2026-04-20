package com.billsync.app.data.repository

import com.billsync.app.data.database.BillDao
import com.billsync.app.data.database.CategorySum
import com.billsync.app.data.database.SourceSum
import com.billsync.app.data.entity.BillCategory
import com.billsync.app.data.entity.BillEntity
import com.billsync.app.data.entity.BillSource
import com.billsync.app.data.entity.BillType
import kotlinx.coroutines.flow.Flow
import java.security.MessageDigest

class BillRepository(private val billDao: BillDao) {

    // ==================== 基础操作 ====================

    val allBills: Flow<List<BillEntity>> = billDao.getAllBills()
    val billCount: Flow<Int> = billDao.getBillCount()

    suspend fun insert(bill: BillEntity): Long {
        val billWithHash = if (bill.uniqueHash.isEmpty()) {
            bill.copy(uniqueHash = generateHash(bill))
        } else {
            bill
        }
        // 去重检查
        if (billDao.existsByHash(billWithHash.uniqueHash) > 0) {
            return -1L // 已存在
        }
        return billDao.insert(billWithHash)
    }

    suspend fun insertAll(bills: List<BillEntity>): List<Long> {
        val billsWithHash = bills.map { bill ->
            if (bill.uniqueHash.isEmpty()) {
                bill.copy(uniqueHash = generateHash(bill))
            } else {
                bill
            }
        }
        return billDao.insertAll(billsWithHash)
    }

    suspend fun update(bill: BillEntity) = billDao.update(bill)

    suspend fun delete(bill: BillEntity) = billDao.delete(bill)

    suspend fun deleteById(id: Long) = billDao.deleteById(id)

    suspend fun getBillById(id: Long) = billDao.getBillById(id)

    // ==================== 筛选查询 ====================

    fun getBillsBySource(source: BillSource) = billDao.getBillsBySource(source)

    fun getBillsByType(type: BillType) = billDao.getBillsByType(type)

    fun getBillsByCategory(category: BillCategory) = billDao.getBillsByCategory(category)

    fun getBillsByTimeRange(startTime: Long, endTime: Long) =
        billDao.getBillsByTimeRange(startTime, endTime)

    fun searchBills(keyword: String) = billDao.searchBills(keyword)

    // ==================== 统计 ====================

    fun getTotalExpense(startTime: Long, endTime: Long): Flow<Double> =
        billDao.getTotalExpense(startTime, endTime)

    fun getTotalIncome(startTime: Long, endTime: Long): Flow<Double> =
        billDao.getTotalIncome(startTime, endTime)

    fun getExpenseByCategory(startTime: Long, endTime: Long): Flow<List<CategorySum>> =
        billDao.getExpenseByCategory(startTime, endTime)

    fun getExpenseBySource(startTime: Long, endTime: Long): Flow<List<SourceSum>> =
        billDao.getExpenseBySource(startTime, endTime)

    // ==================== 工具方法 ====================

    /**
     * 生成账单唯一哈希，用于去重
     * 基于来源 + 时间戳 + 金额 + 交易对方
     */
    private fun generateHash(bill: BillEntity): String {
        val raw = "${bill.source}|${bill.timestamp}|${bill.amount}|${bill.counterparty}"
        val digest = MessageDigest.getInstance("MD5")
        val hashBytes = digest.digest(raw.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}
