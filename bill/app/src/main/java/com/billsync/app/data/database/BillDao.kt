package com.billsync.app.data.database

import androidx.room.*
import com.billsync.app.data.entity.BillEntity
import com.billsync.app.data.entity.BillSource
import com.billsync.app.data.entity.BillType
import com.billsync.app.data.entity.BillCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface BillDao {

    // ==================== 插入 ====================

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(bill: BillEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(bills: List<BillEntity>): List<Long>

    // ==================== 更新 ====================

    @Update
    suspend fun update(bill: BillEntity)

    // ==================== 删除 ====================

    @Delete
    suspend fun delete(bill: BillEntity)

    @Query("DELETE FROM bills WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM bills")
    suspend fun deleteAll()

    // ==================== 查询 ====================

    /** 获取所有账单，按时间倒序 */
    @Query("SELECT * FROM bills ORDER BY timestamp DESC")
    fun getAllBills(): Flow<List<BillEntity>>

    /** 根据 ID 获取账单 */
    @Query("SELECT * FROM bills WHERE id = :id")
    suspend fun getBillById(id: Long): BillEntity?

    /** 根据来源获取账单 */
    @Query("SELECT * FROM bills WHERE source = :source ORDER BY timestamp DESC")
    fun getBillsBySource(source: BillSource): Flow<List<BillEntity>>

    /** 根据类型获取账单 */
    @Query("SELECT * FROM bills WHERE type = :type ORDER BY timestamp DESC")
    fun getBillsByType(type: BillType): Flow<List<BillEntity>>

    /** 根据分类获取账单 */
    @Query("SELECT * FROM bills WHERE category = :category ORDER BY timestamp DESC")
    fun getBillsByCategory(category: BillCategory): Flow<List<BillEntity>>

    /** 根据时间范围获取账单 */
    @Query("SELECT * FROM bills WHERE timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp DESC")
    fun getBillsByTimeRange(startTime: Long, endTime: Long): Flow<List<BillEntity>>

    /** 搜索账单（按描述或交易对方） */
    @Query("SELECT * FROM bills WHERE description LIKE '%' || :keyword || '%' OR counterparty LIKE '%' || :keyword || '%' ORDER BY timestamp DESC")
    fun searchBills(keyword: String): Flow<List<BillEntity>>

    // ==================== 统计 ====================

    /** 获取时间范围内的总支出 */
    @Query("SELECT COALESCE(SUM(ABS(amount)), 0) FROM bills WHERE type = 'EXPENSE' AND timestamp BETWEEN :startTime AND :endTime")
    fun getTotalExpense(startTime: Long, endTime: Long): Flow<Double>

    /** 获取时间范围内的总收入 */
    @Query("SELECT COALESCE(SUM(amount), 0) FROM bills WHERE type = 'INCOME' AND timestamp BETWEEN :startTime AND :endTime")
    fun getTotalIncome(startTime: Long, endTime: Long): Flow<Double>

    /** 按分类统计支出 */
    @Query("SELECT category, SUM(ABS(amount)) as total FROM bills WHERE type = 'EXPENSE' AND timestamp BETWEEN :startTime AND :endTime GROUP BY category ORDER BY total DESC")
    fun getExpenseByCategory(startTime: Long, endTime: Long): Flow<List<CategorySum>>

    /** 按来源统计支出 */
    @Query("SELECT source, SUM(ABS(amount)) as total FROM bills WHERE type = 'EXPENSE' AND timestamp BETWEEN :startTime AND :endTime GROUP BY source ORDER BY total DESC")
    fun getExpenseBySource(startTime: Long, endTime: Long): Flow<List<SourceSum>>

    /** 获取账单总数 */
    @Query("SELECT COUNT(*) FROM bills")
    fun getBillCount(): Flow<Int>

    // ==================== 去重 ====================

    /** 检查是否已存在相同的账单（通过 uniqueHash） */
    @Query("SELECT COUNT(*) FROM bills WHERE uniqueHash = :hash")
    suspend fun existsByHash(hash: String): Int
}

/** 分类统计结果 */
data class CategorySum(
    val category: BillCategory,
    val total: Double
)

/** 来源统计结果 */
data class SourceSum(
    val source: BillSource,
    val total: Double
)
