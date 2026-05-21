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
import com.herb.numi.data.Record
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * 首页主入口
 * 包含本月概览、近期记录列表、批量选择模式、详情弹窗
 *
 * 职责：页面布局和状态管理，具体功能组件已拆分到独立文件
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: RecordViewModel = viewModel(),
    onNavigateToRecord: () -> Unit,
    onEditRecord: (Record) -> Unit,
    onNavigateToReimbursement: () -> Unit
) {
    val allRecords by viewModel.allRecords.collectAsState()
    val monthExpense by viewModel.monthExpense.collectAsState()
    val monthIncome by viewModel.monthIncome.collectAsState()

    var isSelectionMode by remember { mutableStateOf(false) }
    var selectedIds by remember { mutableStateOf(setOf<Long>()) }
    var showDetailSheet by remember { mutableStateOf(false) }
    var selectedRecord by remember { mutableStateOf<Record?>(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var recordToDelete by remember { mutableStateOf<Record?>(null) }
    var showBatchDeleteConfirm by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    fun showSnackbar(message: String) {
        scope.launch {
            snackbarHostState.showSnackbar(message)
        }
    }

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
                    selectedIds = if (selectedIds.size == allRecords.size) {
                        emptySet()
                    } else {
                        allRecords.map { it.id }.toSet()
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
                paddingValues = paddingValues
            )

            if (isSelectionMode) {
                HomeBatchActionBar(
                    selectedCount = selectedIds.size,
                    totalCount = allRecords.size,
                    onCancel = {
                        isSelectionMode = false
                        selectedIds = emptySet()
                    },
                    onSelectAll = {
                        selectedIds = if (selectedIds.size == allRecords.size) {
                            emptySet()
                        } else {
                            allRecords.map { it.id }.toSet()
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
            }
        )
    }

    if (showDeleteConfirm && recordToDelete != null) {
        DeleteConfirmDialog(
            onConfirm = {
                viewModel.deleteRecord(recordToDelete!!)
                showSnackbar("记录已删除")
                showDeleteConfirm = false
                recordToDelete = null
            },
            onDismiss = {
                showDeleteConfirm = false
                recordToDelete = null
            }
        )
    }

    if (showBatchDeleteConfirm) {
        BatchDeleteConfirmDialog(
            selectedCount = selectedIds.size,
            onConfirm = {
                viewModel.deleteRecordsByIds(selectedIds.toList()) {
                    showSnackbar("已删除 ${selectedIds.size} 条记录")
                    isSelectionMode = false
                    selectedIds = emptySet()
                }
                showBatchDeleteConfirm = false
            },
            onDismiss = {
                showBatchDeleteConfirm = false
            }
        )
    }
}

/**
 * 首页内容区
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
                    text = "近期记录",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Surface(
                    onClick = onNavigateToReimbursement,
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = "报销统计",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
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

/**
 * 切换选择状态
 */
private fun toggleSelection(id: Long): (Set<Long>) -> Set<Long> {
    return { selectedIds ->
        if (selectedIds.contains(id)) {
            selectedIds - id
        } else {
            selectedIds + id
        }
    }
}
