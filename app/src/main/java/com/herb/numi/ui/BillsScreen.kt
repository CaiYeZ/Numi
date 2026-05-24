package com.herb.numi.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.herb.numi.data.Record
import com.herb.numi.ui.common.OperationResult
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * 账单页面主入口
 * 整合时间选择、统计概览、分类报表和明细报表
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
fun BillsScreen(
    viewModel: RecordViewModel = viewModel(),
    onNavigateToRecord: () -> Unit = {},
    onEditRecord: (Record) -> Unit = {}
) {
    val allRecords by viewModel.allRecords.collectAsState()
    val deleteOperationResult by viewModel.deleteOperationResult.collectAsState()
    val customCategories by viewModel.customCategories.collectAsState()

    var timePeriod by remember { mutableStateOf(TimePeriod.MONTH) }
    var selectedDate by remember { mutableStateOf(Calendar.getInstance()) }
    var showMonthPicker by remember { mutableStateOf(false) }
    var showYearPicker by remember { mutableStateOf(false) }
    var selectedChartType by remember { mutableStateOf("expense") }

    var showDayDetail by remember { mutableStateOf(false) }
    var selectedDay by remember { mutableIntStateOf(0) }
    var selectedDayRecords by remember { mutableStateOf<List<Record>>(emptyList()) }

    var selectedRecord by remember { mutableStateOf<Record?>(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var recordToDelete by remember { mutableStateOf<Record?>(null) }

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
                showDeleteConfirm = false
                showDayDetail = false
                selectedRecord = null
                recordToDelete = null
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                TimePeriodSelector(
                    selectedPeriod = timePeriod,
                    onPeriodChange = { timePeriod = it }
                )
            }

            item {
                TimeRangeSelector(
                    selectedDate = selectedDate,
                    timePeriod = timePeriod,
                    onShowMonthPicker = { showMonthPicker = true },
                    onShowYearPicker = { showYearPicker = true },
                    onDateChange = { selectedDate = it }
                )
            }

            item {
                OverviewGrid(
                    records = allRecords,
                    timePeriod = timePeriod,
                    selectedDate = selectedDate
                )
            }

            item {
                CategoryReportCard(
                    records = allRecords,
                    timePeriod = timePeriod,
                    selectedDate = selectedDate,
                    selectedChartType = selectedChartType,
                    onChartTypeChange = { selectedChartType = it }
                )
            }

            item {
                DetailReport(
                    records = allRecords,
                    timePeriod = timePeriod,
                    selectedDate = selectedDate,
                    onDayClick = { day, records ->
                        selectedDay = day
                        selectedDayRecords = records
                        showDayDetail = true
                    },
                    modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp)
                )
            }
        }
    }

    if (showMonthPicker) {
        MonthPickerDialog(
            selectedDate = selectedDate,
            onDateSelected = {
                selectedDate = it
                showMonthPicker = false
            },
            onDismiss = { showMonthPicker = false }
        )
    }

    if (showYearPicker) {
        YearPickerDialog(
            selectedDate = selectedDate,
            onDateSelected = {
                selectedDate = it
                showYearPicker = false
            },
            onDismiss = { showYearPicker = false }
        )
    }

    // 当天消费详情底部弹窗
    if (showDayDetail) {
        DayDetailBottomSheet(
            day = selectedDay,
            records = selectedDayRecords,
            timePeriod = timePeriod,
            selectedDate = selectedDate,
            onDismiss = { showDayDetail = false },
            onRecordClick = { record ->
                selectedRecord = record
            },
            customCategories = customCategories
        )
    }

    // 记录详情底部弹窗
    if (selectedRecord != null) {
        RecordDetailBottomSheet(
            record = selectedRecord!!,
            onDismiss = {
                selectedRecord = null
            },
            onEdit = {
                val record = selectedRecord!!
                selectedRecord = null
                onEditRecord(record)
            },
            onDelete = {
                recordToDelete = selectedRecord
                showDeleteConfirm = true
                selectedRecord = null
            },
            isLoading = isDeleting
        )
    }

    // 单条记录删除确认对话框（显示记录信息）
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
}
