package com.herb.numi.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.herb.numi.data.Record
import kotlinx.coroutines.launch
import java.util.*

/**
 * 账单页面主入口
 * 整合时间选择、统计概览、分类报表和明细报表
 *
 * 职责：页面布局和状态管理，具体功能组件已拆分到独立文件
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillsScreen(
    viewModel: RecordViewModel = viewModel(),
    onNavigateToRecord: () -> Unit = {},
    onEditRecord: (Record) -> Unit = {}
) {
    val allRecords by viewModel.allRecords.collectAsState()
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

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
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

    if (showDayDetail) {
        DayDetailBottomSheet(
            day = selectedDay,
            records = selectedDayRecords,
            timePeriod = timePeriod,
            selectedDate = selectedDate,
            onDismiss = { showDayDetail = false },
            onRecordClick = { record ->
                selectedRecord = record
            }
        )
    }

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
            }
        )
    }

    if (showDeleteConfirm && recordToDelete != null) {
        DeleteConfirmDialog(
            onConfirm = {
                viewModel.deleteRecord(recordToDelete!!)
                scope.launch {
                    snackbarHostState.showSnackbar("记录已删除")
                }
                showDeleteConfirm = false
                recordToDelete = null
            },
            onDismiss = {
                showDeleteConfirm = false
                recordToDelete = null
            }
        )
    }
}
