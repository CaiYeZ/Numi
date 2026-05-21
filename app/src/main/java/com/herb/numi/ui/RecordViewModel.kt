package com.herb.numi.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.herb.numi.data.Record
import com.herb.numi.data.RecordRepositoryInterface
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

/**
 * 记账 ViewModel
 * 负责管理记账界面的状态和数据
 *
 * 遵循单一职责原则：只负责记账业务逻辑，不处理 UI 渲染
 * 遵循依赖注入原则：通过 Application 接收 Repository 接口实例
 */
class RecordViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: RecordRepositoryInterface

    init {
        val numiApp = application as com.herb.numi.NumiApplication
        repository = numiApp.repository
    }

    // 金额输入状态
    private val _amount = MutableStateFlow("")
    val amount: StateFlow<String> = _amount.asStateFlow()

    // 记录类型：income 或 expense
    private val _recordType = MutableStateFlow("expense")
    val recordType: StateFlow<String> = _recordType.asStateFlow()

    // 当前选中的分类
    private val _selectedCategory = MutableStateFlow("餐饮")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    // 备注
    private val _note = MutableStateFlow("")
    val note: StateFlow<String> = _note.asStateFlow()

    // 选中的时间
    private val _selectedTime = MutableStateFlow(Calendar.getInstance())
    val selectedTime: StateFlow<Calendar> = _selectedTime.asStateFlow()

    // 报销状态：none（非报销）、pending（待报销）、reimbursed（已报销）
    private val _reimburseStatus = MutableStateFlow("none")
    val reimburseStatus: StateFlow<String> = _reimburseStatus.asStateFlow()

    // 正在编辑的记录ID（null表示新增模式）
    private val _editingRecordId = MutableStateFlow<Long?>(null)

    // 主题模式
    private val _themeMode = MutableStateFlow("system")
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    init {
        _themeMode.value = (application as com.herb.numi.NumiApplication).getThemeMode()
    }

    fun setThemeMode(mode: String) {
        _themeMode.value = mode
        (getApplication<com.herb.numi.NumiApplication>()).setThemeMode(mode)
    }

    // 所有记录
    val allRecords: StateFlow<List<Record>> = repository.allRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 当月数据
    private val monthRange = getCurrentMonthRange()

    val monthExpense: StateFlow<Double> = repository.getMonthExpense(monthRange.first, monthRange.second)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val monthIncome: StateFlow<Double> = repository.getMonthIncome(monthRange.first, monthRange.second)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    /**
     * 追加数字到金额
     */
    fun appendNumber(num: String) {
        val current = _amount.value
        // 防止输入过多小数位
        if (num == "." && current.contains(".")) return
        if (current.contains(".") && current.substringAfter(".").length >= 2) return
        // 防止金额过大
        if (current.replace(".", "").length >= 10) return
        // 首位为0时，自动补小数点
        if (current == "0" && num != ".") {
            _amount.value = num
            return
        }
        _amount.value = current + num
    }

    /**
     * 删除最后一个字符
     */
    fun deleteLastChar() {
        if (_amount.value.isNotEmpty()) {
            _amount.value = _amount.value.dropLast(1)
        }
    }

    /**
     * 清空金额
     */
    fun clearAmount() {
        _amount.value = ""
    }

    /**
     * 切换记录类型
     */
    fun setRecordType(type: String) {
        _recordType.value = type
        // 切换类型时重置分类
        _selectedCategory.value = if (type == "expense") "餐饮" else "工资"
    }

    /**
     * 选择分类
     */
    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    /**
     * 设置备注
     */
    fun setNote(note: String) {
        _note.value = note
    }

    /**
     * 设置选中的时间
     */
    fun setSelectedTime(time: Calendar) {
        _selectedTime.value = time
    }

    /**
     * 设置报销状态
     */
    fun setReimburseStatus(status: String) {
        _reimburseStatus.value = status
    }

    /**
     * 重置输入
     */
    fun resetInput() {
        _amount.value = ""
        _note.value = ""
        _selectedTime.value = Calendar.getInstance()
        _reimburseStatus.value = "none"
    }

    /**
     * 保存记录（从当前输入状态）
     */
    fun saveRecord(onSuccess: () -> Unit) {
        val amountValue = _amount.value.toDoubleOrNull() ?: return
        if (amountValue <= 0) return

        viewModelScope.launch {
            val currentTime = _selectedTime.value.timeInMillis
            val record = Record(
                amount = amountValue,
                type = _recordType.value,
                category = _selectedCategory.value,
                note = _note.value.ifEmpty { null },
                createdAt = currentTime,
                updatedAt = currentTime,
                reimburseStatus = _reimburseStatus.value
            )

            repository.insertRecord(record)

            // 清空输入状态
            _amount.value = ""
            _note.value = ""
            _selectedTime.value = Calendar.getInstance()
            _reimburseStatus.value = "none"

            onSuccess()
        }
    }

    /**
     * 保存记录（直接传入 Record 对象，用于复制功能）
     */
    fun saveRecord(record: Record, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.insertRecord(record)
            onSuccess()
        }
    }

    /**
     * 删除记录
     */
    fun deleteRecord(record: Record) {
        viewModelScope.launch {
            repository.deleteRecord(record)
        }
    }

    /**
     * 批量删除记录
     */
    fun deleteRecordsByIds(ids: List<Long>, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteRecordsByIds(ids)
            onSuccess()
        }
    }

    /**
     * 加载记录用于编辑
     */
    fun loadRecordForEdit(record: Record) {
        _amount.value = record.amount.toString()
        _recordType.value = record.type
        _selectedCategory.value = record.category
        _note.value = record.note ?: ""
        _selectedTime.value = Calendar.getInstance().apply {
            timeInMillis = record.createdAt
        }
        _reimburseStatus.value = record.reimburseStatus
        _editingRecordId.value = record.id
    }

    /**
     * 更新记录
     */
    fun updateRecord(record: Record, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val updatedRecord = record.copy(
                amount = _amount.value.toDoubleOrNull() ?: record.amount,
                type = _recordType.value,
                category = _selectedCategory.value,
                note = _note.value.ifEmpty { null },
                createdAt = _selectedTime.value.timeInMillis,
                updatedAt = System.currentTimeMillis(),
                reimburseStatus = _reimburseStatus.value
            )
            repository.updateRecord(updatedRecord)

            // 清空输入状态
            _amount.value = ""
            _note.value = ""
            _selectedTime.value = Calendar.getInstance()
            _reimburseStatus.value = "none"
            _editingRecordId.value = null

            onSuccess()
        }
    }

    /**
     * 获取当前编辑的记录ID
     */
    val editingRecordId: StateFlow<Long?> = _editingRecordId.asStateFlow()

    /**
     * 取消编辑
     */
    fun cancelEdit() {
        _amount.value = ""
        _note.value = ""
        _selectedTime.value = Calendar.getInstance()
        _reimburseStatus.value = "none"
        _editingRecordId.value = null
    }

    /**
     * 批量更新记录的报销状态
     * @param ids 记录ID列表
     * @param status 目标报销状态
     * @param onSuccess 成功回调
     */
    fun updateRecordsReimburseStatus(ids: List<Long>, status: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            repository.updateRecordsReimburseStatus(ids, status)
            onSuccess()
        }
    }

    /**
     * 获取当前月份的起始和结束时间戳
     */
    private fun getCurrentMonthRange(): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val start = calendar.timeInMillis

        calendar.add(Calendar.MONTH, 1)
        val end = calendar.timeInMillis

        return Pair(start, end)
    }
}
