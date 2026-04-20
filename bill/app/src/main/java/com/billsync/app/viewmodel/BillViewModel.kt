package com.billsync.app.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.billsync.app.data.database.AppDatabase
import com.billsync.app.data.database.CategorySum
import com.billsync.app.data.entity.BillCategory
import com.billsync.app.data.entity.BillEntity
import com.billsync.app.data.entity.BillSource
import com.billsync.app.data.entity.BillType
import com.billsync.app.data.repository.BillRepository
import com.billsync.app.service.CsvImporter
import com.billsync.app.service.CsvSource
import com.billsync.app.service.ImportResult
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

class BillViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: BillRepository
    private val csvImporter: CsvImporter

    // ==================== UI 状态 ====================

    /** 所有账单 */
    val allBills: StateFlow<List<BillEntity>>

    /** 账单总数 */
    val billCount: StateFlow<Int>

    /** 当前选择的时间范围 */
    private val _timeRange = MutableStateFlow(TimeRange.THIS_MONTH)
    val timeRange: StateFlow<TimeRange> = _timeRange.asStateFlow()

    /** 本月总支出 */
    val totalExpense: StateFlow<Double>

    /** 本月总收入 */
    val totalIncome: StateFlow<Double>

    /** 按分类统计支出 */
    val expenseByCategory: StateFlow<List<CategorySum>>

    /** 导入结果 */
    private val _importResult = MutableStateFlow<ImportResult?>(null)
    val importResult: StateFlow<ImportResult?> = _importResult.asStateFlow()

    /** 搜索关键字 */
    private val _searchKeyword = MutableStateFlow("")
    val searchKeyword: StateFlow<String> = _searchKeyword.asStateFlow()

    /** 搜索结果 */
    val searchResults: StateFlow<List<BillEntity>>

    /** 当前来源筛选 */
    private val _selectedSource = MutableStateFlow<BillSource?>(null)
    val selectedSource: StateFlow<BillSource?> = _selectedSource.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        val dao = database.billDao()
        repository = BillRepository(dao)
        csvImporter = CsvImporter(application, repository)

        allBills = repository.allBills
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        billCount = repository.billCount
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

        // 根据时间范围计算起止时间
        val timeRangeFlow = _timeRange.map { getTimeRangeBounds(it) }

        totalExpense = timeRangeFlow.flatMapLatest { (start, end) ->
            repository.getTotalExpense(start, end)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

        totalIncome = timeRangeFlow.flatMapLatest { (start, end) ->
            repository.getTotalIncome(start, end)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

        expenseByCategory = timeRangeFlow.flatMapLatest { (start, end) ->
            repository.getExpenseByCategory(start, end)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        searchResults = _searchKeyword.flatMapLatest { keyword ->
            if (keyword.isBlank()) {
                flowOf(emptyList())
            } else {
                repository.searchBills(keyword)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    // ==================== 操作方法 ====================

    fun setTimeRange(range: TimeRange) {
        _timeRange.value = range
    }

    fun search(keyword: String) {
        _searchKeyword.value = keyword
    }

    fun setSourceFilter(source: BillSource?) {
        _selectedSource.value = source
    }

    fun addBill(bill: BillEntity) {
        viewModelScope.launch {
            repository.insert(bill)
        }
    }

    fun deleteBill(bill: BillEntity) {
        viewModelScope.launch {
            repository.delete(bill)
        }
    }

    fun updateBill(bill: BillEntity) {
        viewModelScope.launch {
            repository.update(bill)
        }
    }

    fun importCsv(uri: Uri, source: CsvSource) {
        viewModelScope.launch {
            val result = csvImporter.importCsv(uri, source)
            _importResult.value = result
        }
    }

    fun clearImportResult() {
        _importResult.value = null
    }

    /**
     * 手动添加账单
     */
    fun addManualBill(
        amount: Double,
        type: BillType,
        category: BillCategory,
        description: String,
        counterparty: String = ""
    ) {
        viewModelScope.launch {
            val bill = BillEntity(
                amount = if (type == BillType.EXPENSE) -amount else amount,
                type = type,
                source = BillSource.MANUAL,
                category = category,
                description = description,
                counterparty = counterparty,
                isConfirmed = true
            )
            repository.insert(bill)
        }
    }

    // ==================== 工具方法 ====================

    private fun getTimeRangeBounds(range: TimeRange): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        val end = calendar.timeInMillis

        when (range) {
            TimeRange.TODAY -> {
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
            }
            TimeRange.THIS_WEEK -> {
                calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
            }
            TimeRange.THIS_MONTH -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
            }
            TimeRange.THIS_YEAR -> {
                calendar.set(Calendar.MONTH, Calendar.JANUARY)
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
            }
            TimeRange.ALL -> {
                return Pair(0L, end)
            }
        }
        return Pair(calendar.timeInMillis, end)
    }
}

enum class TimeRange(val displayName: String) {
    TODAY("今日"),
    THIS_WEEK("本周"),
    THIS_MONTH("本月"),
    THIS_YEAR("今年"),
    ALL("全部")
}
