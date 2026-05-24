package com.herb.numi.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.herb.numi.data.CustomCategory
import com.herb.numi.data.CustomCategoryRepositoryInterface
import com.herb.numi.data.Record
import com.herb.numi.data.RecordRepositoryInterface
import com.herb.numi.data.`import`.CsvImporter
import com.herb.numi.data.`import`.ImportResult
import com.herb.numi.ui.common.OperationResult
import com.herb.numi.ui.common.UiEvent
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

/**
 * 记账 ViewModel
 * 负责管理记账界面的状态和数据
 *
 * 遵循单一职责原则：只负责记账业务逻辑，不处理 UI 渲染
 * 遵循依赖注入原则：Repository 通过构造函数传入
 */
class RecordViewModel(
    application: Application,
    private val repository: RecordRepositoryInterface,
    private val customCategoryRepository: CustomCategoryRepositoryInterface
) : AndroidViewModel(application) {

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

    // 删除操作状态
    private val _deleteOperationResult = MutableStateFlow<OperationResult<Int>>(OperationResult.Idle)
    val deleteOperationResult: StateFlow<OperationResult<Int>> = _deleteOperationResult.asStateFlow()

    // 操作提示事件（一次性事件，避免重复消费）
    private val _operationMessageEvent = MutableStateFlow<UiEvent<String>?>(null)
    val operationMessageEvent: StateFlow<UiEvent<String>?> = _operationMessageEvent.asStateFlow()

    // 自定义分类列表
    private val _customCategories = MutableStateFlow<List<CustomCategory>>(emptyList())
    val customCategories: StateFlow<List<CustomCategory>> = _customCategories.asStateFlow()

    // 是否显示添加自定义分类对话框
    private val _showAddCustomCategoryDialog = MutableStateFlow(false)
    val showAddCustomCategoryDialog: StateFlow<Boolean> = _showAddCustomCategoryDialog.asStateFlow()

    init {
        _themeMode.value = (application as com.herb.numi.NumiApplication).getThemeMode()
        loadCustomCategories()
    }

    /**
     * 加载自定义分类
     */
    private fun loadCustomCategories() {
        viewModelScope.launch {
            customCategoryRepository.getAllCategories().collect { categories ->
                _customCategories.value = categories
            }
        }
    }

    /**
     * 获取指定类型的自定义分类
     */
    fun getCustomCategoriesByType(type: String): List<CustomCategory> {
        return _customCategories.value.filter { it.type == type }
    }

    /**
     * 显示添加分类对话框
     */
    fun showAddCategoryDialog() {
        _showAddCustomCategoryDialog.value = true
    }

    /**
     * 隐藏添加分类对话框
     */
    fun hideAddCategoryDialog() {
        _showAddCustomCategoryDialog.value = false
    }

    /**
     * 添加自定义分类
     */
    fun addCustomCategory(name: String, icon: com.herb.numi.data.CategoryIcon, type: String) {
        viewModelScope.launch {
            val category = CustomCategory(name = name, icon = icon, type = type)
            customCategoryRepository.insertCategory(category)
            hideAddCategoryDialog()
        }
    }

    /**
     * 删除自定义分类
     */
    fun deleteCustomCategory(category: CustomCategory) {
        viewModelScope.launch {
            customCategoryRepository.deleteCategory(category)
        }
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
     * 时间戳秒和毫秒清零，只保留到分钟精度
     */
    fun saveRecord(onSuccess: () -> Unit) {
        val amountValue = _amount.value.toDoubleOrNull() ?: return
        if (amountValue <= 0) return

        viewModelScope.launch {
            val currentTime = _selectedTime.value.apply {
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
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
     * 带操作状态管理和结果反馈
     *
     * @param record 要删除的记录
     * @param onComplete 完成回调（无论成功与否都会调用）
     */
    fun deleteRecord(record: Record, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            _deleteOperationResult.value = OperationResult.Loading
            try {
                repository.deleteRecord(record)
                _deleteOperationResult.value = OperationResult.Success(message = "记录已删除")
                _operationMessageEvent.value = UiEvent("记录已删除")
            } catch (e: Exception) {
                _deleteOperationResult.value = OperationResult.Error(message = "删除失败：${e.message}")
                _operationMessageEvent.value = UiEvent("删除失败，请重试")
            } finally {
                onComplete()
            }
        }
    }

    /**
     * 批量删除记录
     * 带操作状态管理和结果反馈
     *
     * @param ids 要删除的记录ID列表
     * @param onComplete 完成回调（无论成功与否都会调用，参数为实际删除数量）
     */
    fun deleteRecordsByIds(ids: List<Long>, onComplete: (Int) -> Unit = {}) {
        viewModelScope.launch {
            _deleteOperationResult.value = OperationResult.Loading
            try {
                val count = ids.size
                repository.deleteRecordsByIds(ids)
                _deleteOperationResult.value = OperationResult.Success(data = count, message = "已删除 $count 条记录")
                _operationMessageEvent.value = UiEvent("已删除 $count 条记录")
                onComplete(count)
            } catch (e: Exception) {
                _deleteOperationResult.value = OperationResult.Error(message = "批量删除失败：${e.message}")
                _operationMessageEvent.value = UiEvent("批量删除失败，请重试")
                onComplete(0)
            }
        }
    }

    /**
     * 重置删除操作状态为空闲
     * 在UI消费完结果后调用
     */
    fun resetDeleteOperationResult() {
        _deleteOperationResult.value = OperationResult.Idle
    }

    /**
     * 消费操作消息事件
     * 返回事件内容并标记为已消费
     */
    fun consumeOperationMessageEvent(): String? {
        return _operationMessageEvent.value?.getContentIfNotHandled()
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
     * 时间戳秒和毫秒清零，只保留到分钟精度
     */
    fun updateRecord(record: Record, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val selectedTime = _selectedTime.value.apply {
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            val updatedTime = Calendar.getInstance().apply {
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            val updatedRecord = record.copy(
                amount = _amount.value.toDoubleOrNull() ?: record.amount,
                type = _recordType.value,
                category = _selectedCategory.value,
                note = _note.value.ifEmpty { null },
                createdAt = selectedTime,
                updatedAt = updatedTime,
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

    /**
     * 从CSV内容导入数据
     * 解析CSV内容并批量插入记录和自定义分类
     * 自动跳过已存在的记录（基于金额、类型、分类、创建时间判断重复）
     *
     * @param csvContent CSV格式字符串
     * @return 导入结果
     */
    suspend fun importFromCsv(csvContent: String): ImportResult {
        val parseResult = CsvImporter.import(csvContent)
        val records = CsvImporter.getLastImportedRecords()
        val categories = CsvImporter.getLastImportedCategories()

        return when (parseResult) {
            is ImportResult.Success, is ImportResult.Partial -> {
                var newRecordCount = 0
                var duplicateCount = 0

                if (records.isNotEmpty()) {
                    newRecordCount = repository.importRecordsWithDeduplication(records)
                    duplicateCount = records.size - newRecordCount
                }

                if (categories.isNotEmpty()) {
                    customCategoryRepository.importCategoriesWithDeduplication(categories)
                }

                if (duplicateCount > 0) {
                    ImportResult.Success(
                        recordCount = records.size,
                        newRecordCount = newRecordCount,
                        duplicateCount = duplicateCount,
                        categoryCount = categories.size
                    )
                } else {
                    ImportResult.Success(
                        recordCount = records.size,
                        newRecordCount = newRecordCount,
                        duplicateCount = 0,
                        categoryCount = categories.size
                    )
                }
            }
            is ImportResult.Error -> parseResult
            is ImportResult.Empty -> parseResult
        }
    }

    /**
     * 清空所有数据
     * 删除所有记录和自定义分类
     */
    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllData()
            _operationMessageEvent.value = UiEvent("所有数据已清空")
        }
    }
}
