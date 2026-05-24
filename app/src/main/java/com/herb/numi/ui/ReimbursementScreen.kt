package com.herb.numi.ui

import android.os.SystemClock
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.herb.numi.NumiApplication
import com.herb.numi.data.Record
import com.herb.numi.data.ReimburseStatus
import com.herb.numi.ui.common.OperationResult
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * 报销统计界面
 * 展示非报销和已报销记录的统计信息和明细列表
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
fun ReimbursementScreen(
    viewModel: RecordViewModel = viewModel(),
    onNavigateBack: () -> Unit,
    onRecordStatusPresetChange: (String) -> Unit,
    onEditRecord: (Record) -> Unit = {},
    onBatchModeChange: (Boolean) -> Unit = {}
) {
    val allRecords by viewModel.allRecords.collectAsState()
    val deleteOperationResult by viewModel.deleteOperationResult.collectAsState()
    val customCategories by viewModel.customCategories.collectAsState()
    val context = LocalContext.current.applicationContext as? NumiApplication

    val calendar = Calendar.getInstance()
    var selectedYear by remember { mutableIntStateOf(calendar.get(Calendar.YEAR)) }
    var selectedMonth by remember { mutableIntStateOf(calendar.get(Calendar.MONTH) + 1) }
    var viewMode by remember { mutableIntStateOf(0) }
    var selectedTab by remember { mutableIntStateOf(0) }
    var showDetailSheet by remember { mutableStateOf(false) }
    var selectedRecord by remember { mutableStateOf<Record?>(null) }
    var showSingleDeleteConfirm by remember { mutableStateOf(false) }
    var isDateSelectorExpanded by remember { mutableStateOf(false) }
    var lastBackUptime by remember { mutableLongStateOf(0L) }
    var expandedGroups by remember { mutableStateOf(mapOf<String, Boolean>()) }
    var isExpansionStateRestored by remember { mutableStateOf(false) }
    var showBatchScreen by remember { mutableStateOf(false) }

    fun toggleGroupExpanded(groupKey: String) {
        val newExpanded = !expandedGroups.getOrDefault(groupKey, true)
        expandedGroups = expandedGroups.toMutableMap().apply {
            put(groupKey, newExpanded)
        }
        context?.setGroupExpanded(groupKey, newExpanded)
    }

    val availableYears = remember(allRecords, selectedYear) {
        val nowYear = Calendar.getInstance().get(Calendar.YEAR)
        val years = allRecords.map { record ->
            Calendar.getInstance().apply { timeInMillis = record.createdAt }.get(Calendar.YEAR)
        } + selectedYear + nowYear
        (years.maxOrNull()!! downTo years.minOrNull()!!).toList()
    }

    val filteredRecords = remember(allRecords, selectedYear, selectedMonth, viewMode) {
        if (viewMode == 0) {
            val startCal = Calendar.getInstance().apply {
                set(selectedYear, selectedMonth - 1, 1, 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val endCal = Calendar.getInstance().apply {
                set(selectedYear, selectedMonth - 1, 1, 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
                add(Calendar.MONTH, 1)
            }
            allRecords.filter { it.createdAt >= startCal.timeInMillis && it.createdAt < endCal.timeInMillis }
        } else {
            val startCal = Calendar.getInstance().apply {
                set(selectedYear, 0, 1, 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val endCal = Calendar.getInstance().apply {
                set(selectedYear, 0, 1, 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
                add(Calendar.YEAR, 1)
            }
            allRecords.filter { it.createdAt >= startCal.timeInMillis && it.createdAt < endCal.timeInMillis }
        }
    }

    val pendingRecords = filteredRecords.filter { it.reimburseStatus == ReimburseStatus.PENDING.value }
    val reimbursedRecords = filteredRecords.filter { it.reimburseStatus == ReimburseStatus.REIMBURSED.value }
    val pendingAmount = pendingRecords.sumOf { it.amount }
    val reimbursedAmount = reimbursedRecords.sumOf { it.amount }
    val currentRecords = if (selectedTab == 0) pendingRecords else reimbursedRecords

    LaunchedEffect(context, currentRecords, viewMode, isExpansionStateRestored) {
        if (context == null || isExpansionStateRestored) return@LaunchedEffect

        val dateKeys = if (viewMode == 0) {
            currentRecords.map { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it.createdAt)) }.toSet()
        } else {
            currentRecords.map { SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date(it.createdAt)) }.toSet()
        }

        val restoredStates = dateKeys.associateWith { dateKey -> context.isGroupExpanded(dateKey) }
        expandedGroups = (expandedGroups.toList() + restoredStates.toList()).toMap()
        isExpansionStateRestored = true
    }

    val recordStatusPreset = if (selectedTab == 0) ReimburseStatus.PENDING.value else ReimburseStatus.REIMBURSED.value

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    /**
     * 消费 ViewModel 中的操作消息事件
     */
    LaunchedEffect(Unit) {
        viewModel.operationMessageEvent.collect { event ->
            event?.getContentIfNotHandled()?.let { message ->
                scope.launch {
                    snackbarHostState.showSnackbar(message)
                }
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
                showSingleDeleteConfirm = false
                showDetailSheet = false
                selectedRecord = null
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

    LaunchedEffect(recordStatusPreset) {
        onRecordStatusPresetChange(recordStatusPreset)
    }

    fun showSnackbar(message: String) {
        scope.launch { snackbarHostState.showSnackbar(message) }
    }

    fun handleBack() {
        val now = SystemClock.uptimeMillis()
        if (now - lastBackUptime < 350L) return
        lastBackUptime = now

        when {
            showBatchScreen -> { showBatchScreen = false; onBatchModeChange(false) }
            showDetailSheet -> { showDetailSheet = false; selectedRecord = null }
            isDateSelectorExpanded -> { isDateSelectorExpanded = false }
            else -> onNavigateBack()
        }
    }

    BackHandler { handleBack() }

    if (showBatchScreen) {
        ReimbursementBatchScreen(
            records = currentRecords,
            selectedTab = selectedTab,
            viewModel = viewModel,
            onNavigateBack = {
                showBatchScreen = false
                onBatchModeChange(false)
            },
            onEditRecord = onEditRecord
        )
    } else {
        Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { paddingValues ->
            val topInset = paddingValues.calculateTopPadding()
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .edgeSwipeBack { handleBack() }
                    .consumeTopInset(topInset)
                    .padding(bottom = paddingValues.calculateBottomPadding())
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(start = 16.dp, end = 16.dp, top = TopContentSpacing + 8.dp, bottom = 8.dp)
                    ) {
                        ReimbursementTopBar(
                            viewMode = viewMode,
                            onViewModeChange = {
                                viewMode = it
                                isDateSelectorExpanded = false
                                isExpansionStateRestored = false
                            },
                            onNavigateBack = { handleBack() }
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        ReimbursementYearMonthNavigator(
                            year = selectedYear,
                            month = selectedMonth,
                            viewMode = viewMode,
                            availableYears = availableYears,
                            isSelectorExpanded = isDateSelectorExpanded,
                            onSelectorExpandedChange = { isDateSelectorExpanded = it },
                            onYearChange = { selectedYear = it; isExpansionStateRestored = false },
                            onMonthChange = { selectedMonth = it; isExpansionStateRestored = false }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            ReimbursementTabRow(
                                selectedTab = selectedTab,
                                onTabSelected = {
                                    selectedTab = it
                                    onRecordStatusPresetChange(if (it == 0) ReimburseStatus.PENDING.value else ReimburseStatus.REIMBURSED.value)
                                    isDateSelectorExpanded = false
                                    isExpansionStateRestored = false
                                },
                                pendingCount = pendingRecords.size,
                                reimbursedCount = reimbursedRecords.size,
                                pendingAmount = pendingAmount,
                                reimbursedAmount = reimbursedAmount
                            )
                        }

                        item {
                            ReimbursementBillDetailsHeader(
                                selectedTab = selectedTab,
                                currentRecords = currentRecords,
                                onNavigateToBatch = {
                                    showBatchScreen = true
                                    onBatchModeChange(true)
                                }
                            )
                        }

                        if (currentRecords.isEmpty()) {
                            item {
                                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                    Text(
                                        text = if (selectedTab == 0) "无待报销记录" else "无已报销记录",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        } else {
                            val groupedRecords = currentRecords.groupBy {
                                SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date(it.createdAt))
                            }.mapValues { (_, records) -> records.sortedByDescending { it.createdAt } }
                                .toSortedMap(compareByDescending { it })

                            groupedRecords.forEach { (monthKey, records) ->
                                item(key = "reimburse_month_$monthKey") {
                                    ReimbursementMonthCard(
                                        monthKey = monthKey,
                                        records = records,
                                        isExpanded = expandedGroups[monthKey] ?: true,
                                        onToggleExpanded = { toggleGroupExpanded(monthKey) },
                                        isSelectionMode = false,
                                        selectedIds = emptySet(),
                                        onRecordClick = { selectedRecord = it; showDetailSheet = true },
                                        onRecordLongClick = {},
                                        onGroupSelect = {},
                                        customCategories = customCategories
                                    )
                                }
                            }
                        }

                        item { Spacer(modifier = Modifier.height(72.dp)) }
                    }
                }
            }
        }

        // 记录详情底部弹窗
        if (showDetailSheet && selectedRecord != null) {
            RecordDetailBottomSheet(
                record = selectedRecord!!,
                onDismiss = { showDetailSheet = false; selectedRecord = null },
                onEdit = {
                    showDetailSheet = false
                    selectedRecord?.let { onEditRecord(it) }
                    selectedRecord = null
                },
                onDelete = {
                    showDetailSheet = false
                    showSingleDeleteConfirm = true
                },
                isLoading = isDeleting
            )
        }

        // 单条记录删除确认对话框（显示记录信息）
        if (showSingleDeleteConfirm && selectedRecord != null) {
            SingleDeleteConfirmDialog(
                record = selectedRecord!!,
                onConfirm = {
                    viewModel.deleteRecord(selectedRecord!!)
                },
                onDismiss = {
                    if (!isDeleting) {
                        showSingleDeleteConfirm = false
                        selectedRecord = null
                    }
                },
                isLoading = isDeleting
            )
        }
    }
}

/**
 * 报销账单明细头部
 *
 * @param selectedTab 当前选中的标签（0=待报销，1=已报销）
 * @param currentRecords 当前显示的记录列表
 * @param onNavigateToBatch 进入批量管理回调
 */
@Composable
private fun ReimbursementBillDetailsHeader(
    selectedTab: Int,
    currentRecords: List<Record>,
    onNavigateToBatch: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "账单明细",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Surface(
            onClick = { if (currentRecords.isNotEmpty()) onNavigateToBatch() },
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            enabled = currentRecords.isNotEmpty()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Checklist,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = if (currentRecords.isNotEmpty()) MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                )
            }
        }
    }
}

