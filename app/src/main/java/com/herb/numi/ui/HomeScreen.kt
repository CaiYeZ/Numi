package com.herb.numi.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.herb.numi.data.CustomCategory
import com.herb.numi.data.Record
import com.herb.numi.ui.common.OperationResult
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * 首页主入口
 * 包含本月概览、近期记录列表、批量选择模式、详情弹窗
 *
 * 职责：页面布局和状态管理，具体功能组件已拆分到独立文件
 *
 * 功能特性：
 * - 记录详情弹窗支持修改和删除操作
 * - 删除操作带加载状态、防重复点击、结果反馈
 * - 操作完成后自动关闭弹窗并刷新列表（通过Flow自动刷新）
 * - 屏幕适配：底部安全区域处理
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: RecordViewModel = viewModel(),
    onNavigateToRecord: () -> Unit,
    onEditRecord: (Record) -> Unit,
    onNavigateToReimbursement: () -> Unit,
    onNavigateToBillsDetail: () -> Unit
) {
    val allRecords by viewModel.allRecords.collectAsState()
    val monthExpense by viewModel.monthExpense.collectAsState()
    val monthIncome by viewModel.monthIncome.collectAsState()
    val deleteOperationResult by viewModel.deleteOperationResult.collectAsState()
    val customCategories by viewModel.customCategories.collectAsState()

    var isSelectionMode by remember { mutableStateOf(false) }
    var selectedIds by remember { mutableStateOf(setOf<Long>()) }
    var showDetailSheet by remember { mutableStateOf(false) }
    var selectedRecord by remember { mutableStateOf<Record?>(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var recordToDelete by remember { mutableStateOf<Record?>(null) }
    var showBatchDeleteConfirm by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    /**
     * 显示 Snackbar 提示
     */
    fun showSnackbar(message: String) {
        scope.launch {
            snackbarHostState.showSnackbar(message)
        }
    }

    /**
     * 消费 ViewModel 中的操作消息事件
     */
    LaunchedEffect(Unit) {
        viewModel.operationMessageEvent.collect { event ->
            event?.getContentIfNotHandled()?.let { message ->
                showSnackbar(message)
            }
        }
    }

    /**
     * 监听删除操作结果，自动清理状态
     */
    LaunchedEffect(deleteOperationResult) {
        when (deleteOperationResult) {
            is OperationResult.Success -> {
                // 删除成功，自动关闭确认对话框和详情弹窗
                showDeleteConfirm = false
                showBatchDeleteConfirm = false
                showDetailSheet = false
                recordToDelete = null
                selectedRecord = null
                isSelectionMode = false
                selectedIds = emptySet()
                viewModel.resetDeleteOperationResult()
            }
            is OperationResult.Error -> {
                // 删除失败，关闭加载状态但保留对话框以便重试
                viewModel.resetDeleteOperationResult()
            }
            else -> { /* Idle 或 Loading 状态不处理 */ }
        }
    }

    val isDeleting = deleteOperationResult.isLoading

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            HomeContent(
                allRecords = allRecords,
                monthExpense = monthExpense,
                monthIncome = monthIncome,
                isSelectionMode = isSelectionMode,
                selectedIds = selectedIds,
                onRecordClick = { record ->
                    if (isSelectionMode) {
                        selectedIds = if (selectedIds.contains(record.id)) {
                            selectedIds - record.id
                        } else {
                            selectedIds + record.id
                        }
                        if (selectedIds.isEmpty()) {
                            isSelectionMode = false
                        }
                    } else {
                        selectedRecord = record
                        showDetailSheet = true
                    }
                },
                onRecordLongClick = { record ->
                    if (!isSelectionMode) {
                        isSelectionMode = true
                        selectedIds = setOf(record.id)
                    }
                },
                onCancelSelection = {
                    isSelectionMode = false
                    selectedIds = emptySet()
                },
                onSelectAll = {
                    val recentRecords = allRecords.filter {
                        val cal = Calendar.getInstance()
                        cal.add(Calendar.DAY_OF_YEAR, -2)
                        cal.set(Calendar.HOUR_OF_DAY, 0)
                        cal.set(Calendar.MINUTE, 0)
                        cal.set(Calendar.SECOND, 0)
                        cal.set(Calendar.MILLISECOND, 0)
                        it.createdAt >= cal.timeInMillis
                    }
                    selectedIds = if (selectedIds.size == recentRecords.size) {
                        emptySet()
                    } else {
                        recentRecords.map { it.id }.toSet()
                    }
                },
                onBatchDelete = {
                    if (selectedIds.isNotEmpty()) {
                        showBatchDeleteConfirm = true
                    }
                },
                onDeleteRecord = { record ->
                    recordToDelete = record
                    showDeleteConfirm = true
                },
                onNavigateToReimbursement = onNavigateToReimbursement,
                onNavigateToBillsDetail = onNavigateToBillsDetail,
                customCategories = customCategories,
                paddingValues = paddingValues
            )

            if (isSelectionMode) {
                val cal = Calendar.getInstance()
                cal.add(Calendar.DAY_OF_YEAR, -2)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val threeDaysAgo = cal.timeInMillis
                val recentRecords = allRecords.filter { it.createdAt >= threeDaysAgo }
                HomeBatchActionBar(
                    selectedCount = selectedIds.size,
                    totalCount = recentRecords.size,
                    onCancel = {
                        isSelectionMode = false
                        selectedIds = emptySet()
                    },
                    onSelectAll = {
                        selectedIds = if (selectedIds.size == recentRecords.size) {
                            emptySet()
                        } else {
                            recentRecords.map { r -> r.id }.toSet()
                        }
                    },
                    onDelete = {
                        if (selectedIds.isNotEmpty()) {
                            showBatchDeleteConfirm = true
                        }
                    },
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }

    // 记录详情底部弹窗
    if (showDetailSheet && selectedRecord != null) {
        RecordDetailBottomSheet(
            record = selectedRecord!!,
            onDismiss = {
                showDetailSheet = false
                selectedRecord = null
            },
            onEdit = {
                showDetailSheet = false
                onEditRecord(selectedRecord!!)
                selectedRecord = null
            },
            onDelete = {
                recordToDelete = selectedRecord
                showDeleteConfirm = true
                showDetailSheet = false
            },
            isLoading = isDeleting
        )
    }

    // 单条记录删除确认对话框
    if (showDeleteConfirm && recordToDelete != null) {
        SingleDeleteConfirmDialog(
            record = recordToDelete!!,
            onConfirm = {
                viewModel.deleteRecord(recordToDelete!!)
            },
            onDismiss = {
                if (!isDeleting) {
                    showDeleteConfirm = false
                    recordToDelete = null
                }
            },
            isLoading = isDeleting
        )
    }

    // 批量删除确认对话框
    if (showBatchDeleteConfirm) {
        BatchDeleteConfirmDialog(
            selectedCount = selectedIds.size,
            onConfirm = {
                viewModel.deleteRecordsByIds(selectedIds.toList()) { count ->
                    if (count > 0) {
                        isSelectionMode = false
                        selectedIds = emptySet()
                    }
                }
            },
            onDismiss = {
                if (!isDeleting) {
                    showBatchDeleteConfirm = false
                }
            },
            isLoading = isDeleting
        )
    }
}

/**
 * 首页内容区
 *
 * @param allRecords 所有记录列表
 * @param monthExpense 本月支出
 * @param monthIncome 本月收入
 * @param isSelectionMode 是否处于批量选择模式
 * @param selectedIds 已选中的记录ID集合
 * @param onRecordClick 记录点击回调
 * @param onRecordLongClick 记录长按回调
 * @param onCancelSelection 取消选择回调
 * @param onSelectAll 全选/取消全选回调
 * @param onBatchDelete 批量删除回调
 * @param onDeleteRecord 删除单条记录回调
 * @param onNavigateToReimbursement 导航到报销统计回调
 * @param customCategories 自定义分类列表，用于解析记录图标
 * @param paddingValues Scaffold 的内边距
 */
@Composable
private fun HomeContent(
    allRecords: List<Record>,
    monthExpense: Double,
    monthIncome: Double,
    isSelectionMode: Boolean,
    selectedIds: Set<Long>,
    onRecordClick: (Record) -> Unit,
    onRecordLongClick: (Record) -> Unit,
    onCancelSelection: () -> Unit,
    onSelectAll: () -> Unit,
    onBatchDelete: () -> Unit,
    onDeleteRecord: (Record) -> Unit,
    onNavigateToReimbursement: () -> Unit,
    onNavigateToBillsDetail: () -> Unit,
    customCategories: List<CustomCategory>,
    paddingValues: PaddingValues
) {
    // 按日期分组，每组内部按时间降序排列（仅显示近3天记录）
    val groupedRecords = remember(allRecords) {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -2)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val threeDaysAgo = cal.timeInMillis

        allRecords
            .filter { it.createdAt >= threeDaysAgo }
            .groupBy {
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it.createdAt))
            }.mapValues { (_, records) ->
                records.sortedByDescending { it.createdAt }
            }.toSortedMap(compareByDescending { it })
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(bottom = paddingValues.calculateBottomPadding()),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = TopContentSpacing,
            bottom = 16.dp
        ),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        item {
            HomeMonthOverviewCard(
                monthExpense = monthExpense,
                monthIncome = monthIncome
            )
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "近三天记录",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        onClick = onNavigateToBillsDetail,
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Text(
                            text = "明细",
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    Surface(
                        onClick = onNavigateToReimbursement,
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Text(
                            text = "报销统计",
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        if (groupedRecords.isEmpty()) {
            item {
                EmptyRecordsMessage()
            }
        } else {
            groupedRecords.forEach { (dateKey, records) ->
                item(key = "card_$dateKey") {
                    HomeRecordDayCard(
                        dateKey = dateKey,
                        records = records,
                        isSelectionMode = isSelectionMode,
                        selectedIds = selectedIds,
                        onRecordClick = onRecordClick,
                        onRecordLongClick = onRecordLongClick,
                        customCategories = customCategories,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(72.dp))
        }
    }
}

/**
 * 空记录提示
 *
 * @param modifier 修饰符
 */
@Composable
private fun EmptyRecordsMessage(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "暂无记录，点击右下角按钮开始记账",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
