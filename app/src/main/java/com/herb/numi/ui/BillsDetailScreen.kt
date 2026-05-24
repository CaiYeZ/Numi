package com.herb.numi.ui

import android.os.SystemClock
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
 * 账单明细界面
 * 展示全部记录的分类统计和明细列表
 *
 * 5个Tab: 结余(全部) | 支出 | 收入 | 待报销 | 已报销
 *
 * 职责：页面布局和状态管理，具体功能组件已拆分到独立文件
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillsDetailScreen(
    viewModel: RecordViewModel = viewModel(),
    onNavigateBack: () -> Unit,
    onEditRecord: (Record) -> Unit = {}
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

    val tabFilteredRecords = when (selectedTab) {
        0 -> filteredRecords
        1 -> filteredRecords.filter { it.type == "expense" }
        2 -> filteredRecords.filter { it.type == "income" }
        3 -> filteredRecords.filter { it.reimburseStatus == ReimburseStatus.PENDING.value }
        4 -> filteredRecords.filter { it.reimburseStatus == ReimburseStatus.REIMBURSED.value }
        else -> filteredRecords
    }

    val balanceCount = filteredRecords.size
    val expenseCount = filteredRecords.count { it.type == "expense" }
    val incomeCount = filteredRecords.count { it.type == "income" }
    val pendingCount = filteredRecords.count { it.reimburseStatus == ReimburseStatus.PENDING.value }
    val reimbursedCount = filteredRecords.count { it.reimburseStatus == ReimburseStatus.REIMBURSED.value }

    val expenseAmount = filteredRecords.filter { it.type == "expense" }.sumOf { it.amount }
    val incomeAmount = filteredRecords.filter { it.type == "income" }.sumOf { it.amount }
    val balanceAmount = incomeAmount - expenseAmount
    val pendingAmount = filteredRecords.filter { it.reimburseStatus == ReimburseStatus.PENDING.value }.sumOf { it.amount }
    val reimbursedAmount = filteredRecords.filter { it.reimburseStatus == ReimburseStatus.REIMBURSED.value }.sumOf { it.amount }

    LaunchedEffect(context, tabFilteredRecords, viewMode, isExpansionStateRestored) {
        if (context == null || isExpansionStateRestored) return@LaunchedEffect

        val dateKeys = if (viewMode == 0) {
            tabFilteredRecords.map { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it.createdAt)) }.toSet()
        } else {
            tabFilteredRecords.map { SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date(it.createdAt)) }.toSet()
        }

        val restoredStates = dateKeys.associateWith { dateKey -> context.isGroupExpanded(dateKey) }
        expandedGroups = (expandedGroups.toList() + restoredStates.toList()).toMap()
        isExpansionStateRestored = true
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.operationMessageEvent.collect { event ->
            event?.getContentIfNotHandled()?.let { message ->
                scope.launch { snackbarHostState.showSnackbar(message) }
            }
        }
    }

    LaunchedEffect(deleteOperationResult) {
        when (deleteOperationResult) {
            is OperationResult.Success -> {
                showSingleDeleteConfirm = false
                showDetailSheet = false
                selectedRecord = null
                viewModel.resetDeleteOperationResult()
            }
            is OperationResult.Error -> {
                viewModel.resetDeleteOperationResult()
            }
            else -> {}
        }
    }

    val isDeleting = deleteOperationResult.isLoading

    fun handleBack() {
        val now = SystemClock.uptimeMillis()
        if (now - lastBackUptime < 350L) return
        lastBackUptime = now

        when {
            showBatchScreen -> { showBatchScreen = false }
            showDetailSheet -> { showDetailSheet = false; selectedRecord = null }
            isDateSelectorExpanded -> { isDateSelectorExpanded = false }
            else -> onNavigateBack()
        }
    }

    BackHandler { handleBack() }

    if (showBatchScreen) {
        BillsDetailBatchScreen(
            records = tabFilteredRecords,
            selectedTab = selectedTab,
            viewModel = viewModel,
            onNavigateBack = { showBatchScreen = false },
            onEditRecord = onEditRecord
        )
    } else {
        Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .edgeSwipeBack { handleBack() }
                    .padding(bottom = paddingValues.calculateBottomPadding())
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(start = 16.dp, end = 16.dp, top = TopContentSpacing + 8.dp, bottom = 8.dp)
                    ) {
                        BillsDetailTopBar(
                            viewMode = viewMode,
                            onViewModeChange = {
                                viewMode = it
                                isDateSelectorExpanded = false
                                isExpansionStateRestored = false
                            },
                            onNavigateBack = { handleBack() }
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        BillsDetailYearMonthNavigator(
                            year = selectedYear,
                            month = selectedMonth,
                            viewMode = viewMode,
                            availableYears = availableYears,
                            isSelectorExpanded = isDateSelectorExpanded,
                            onSelectorExpandedChange = { isDateSelectorExpanded = it },
                            onYearChange = { selectedYear = it; isExpansionStateRestored = false },
                            onMonthChange = { selectedMonth = it; isExpansionStateRestored = false },
                            onExpansionStateChange = { isExpansionStateRestored = false }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            BillsDetailTabRow(
                                selectedTab = selectedTab,
                                onTabSelected = {
                                    selectedTab = it
                                    isDateSelectorExpanded = false
                                    isExpansionStateRestored = false
                                },
                                balanceCount = balanceCount,
                                expenseCount = expenseCount,
                                incomeCount = incomeCount,
                                pendingCount = pendingCount,
                                reimbursedCount = reimbursedCount,
                                balanceAmount = balanceAmount,
                                expenseAmount = expenseAmount,
                                incomeAmount = incomeAmount,
                                pendingAmount = pendingAmount,
                                reimbursedAmount = reimbursedAmount
                            )
                        }

                        item {
                            BillsDetailBillDetailsHeader(
                                currentRecords = tabFilteredRecords,
                                onNavigateToBatch = { showBatchScreen = true }
                            )
                        }

                        if (tabFilteredRecords.isEmpty()) {
                            item {
                                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                    Text(
                                        text = when (selectedTab) {
                                            0 -> "无记录"
                                            1 -> "无支出记录"
                                            2 -> "无收入记录"
                                            3 -> "无待报销记录"
                                            4 -> "无已报销记录"
                                            else -> "无记录"
                                        },
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        } else {
                            val groupedRecords = tabFilteredRecords.groupBy {
                                SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date(it.createdAt))
                            }.mapValues { (_, records) -> records.sortedByDescending { it.createdAt } }
                                .toSortedMap(compareByDescending { it })

                            groupedRecords.forEach { (monthKey, records) ->
                                item(key = "detail_month_$monthKey") {
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
                                        amountMode = if (selectedTab == 0) "balance" else "sum",
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
