package com.herb.numi.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.herb.numi.data.Record
import com.herb.numi.data.ReimburseStatus
import com.herb.numi.ui.common.OperationResult
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * 账单明细批量管理界面
 * 独立的批量操作页面，支持全选、批量变更报销状态、批量删除
 */
@Composable
fun BillsDetailBatchScreen(
    records: List<Record>,
    selectedTab: Int,
    viewModel: RecordViewModel,
    onNavigateBack: () -> Unit,
    onEditRecord: (Record) -> Unit = {}
) {
    var selectedIds by remember { mutableStateOf(setOf<Long>()) }
    var showBatchDeleteConfirm by remember { mutableStateOf(false) }
    var expandedGroups by remember { mutableStateOf(mapOf<String, Boolean>()) }
    var showDetailSheet by remember { mutableStateOf(false) }
    var selectedRecord by remember { mutableStateOf<Record?>(null) }
    val deleteOperationResult by viewModel.deleteOperationResult.collectAsState()
    val customCategories by viewModel.customCategories.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val isDeleting = deleteOperationResult.isLoading

    // Tab 3=待报销 -> 已报销, Tab 4=已报销 -> 待报销
    val targetStatus = if (selectedTab == 3) ReimburseStatus.REIMBURSED.value else ReimburseStatus.PENDING.value

    val groupedRecords = remember(records) {
        records.groupBy {
            SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date(it.createdAt))
        }.mapValues { (_, rs) -> rs.sortedByDescending { it.createdAt } }
            .toSortedMap(compareByDescending { it })
    }

    val isAllSelected = records.isNotEmpty() && selectedIds.size == records.size

    val emptyMessage = when (selectedTab) {
        0 -> "无记录"
        1 -> "无支出记录"
        2 -> "无收入记录"
        3 -> "无待报销记录"
        4 -> "无已报销记录"
        else -> "无记录"
    }

    fun exitSelectionMode() {
        selectedIds = emptySet()
    }

    fun toggleRecordSelection(record: Record) {
        selectedIds = if (selectedIds.contains(record.id)) {
            selectedIds - record.id
        } else {
            selectedIds + record.id
        }
    }

    fun toggleGroupSelection(groupRecords: List<Record>) {
        val groupIds = groupRecords.map { it.id }.toSet()
        selectedIds = if (groupIds.all { selectedIds.contains(it) }) {
            selectedIds - groupIds
        } else {
            selectedIds + groupIds
        }
    }

    fun toggleGroupExpanded(groupKey: String) {
        expandedGroups = expandedGroups.toMutableMap().apply {
            put(groupKey, !getOrDefault(groupKey, true))
        }
    }

    fun applyBatchReimburseStatus() {
        if (selectedIds.isEmpty()) return
        val ids = selectedIds.toList()
        val count = ids.size
        val message = if (selectedTab == 3) "已报销 $count 条记录" else "已撤销 $count 条记录"
        viewModel.updateRecordsReimburseStatus(ids, targetStatus) {
            scope.launch { snackbarHostState.showSnackbar(message) }
            exitSelectionMode()
        }
    }

    fun showSnackbar(message: String) {
        scope.launch { snackbarHostState.showSnackbar(message) }
    }

    LaunchedEffect(deleteOperationResult) {
        when (deleteOperationResult) {
            is OperationResult.Success -> {
                showBatchDeleteConfirm = false
                showDetailSheet = false
                selectedRecord = null
                exitSelectionMode()
                viewModel.resetDeleteOperationResult()
            }
            is OperationResult.Error -> {
                viewModel.resetDeleteOperationResult()
            }
            else -> {}
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Spacer(modifier = Modifier.height(TopContentSpacing))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "返回"
                        )
                    }
                    Text(
                        text = "批量管理",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    TextButton(
                        onClick = {
                            selectedIds = if (isAllSelected) emptySet()
                            else records.map { it.id }.toSet()
                        }
                    ) {
                        Text(
                            text = if (isAllSelected) "取消全选" else "全选",
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                if (records.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = emptyMessage,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        groupedRecords.forEach { (monthKey, groupRecords) ->
                            item(key = "batch_$monthKey") {
                                ReimbursementMonthCard(
                                    monthKey = monthKey,
                                    records = groupRecords,
                                    isExpanded = expandedGroups[monthKey] ?: true,
                                    onToggleExpanded = { toggleGroupExpanded(monthKey) },
                                    isSelectionMode = true,
                                    selectedIds = selectedIds,
                                    onRecordClick = { toggleRecordSelection(it) },
                                    onRecordLongClick = {},
                                    onGroupSelect = { toggleGroupSelection(groupRecords) },
                                    amountMode = if (selectedTab == 0) "balance" else "sum",
                                    customCategories = customCategories
                                )
                            }
                        }

                        item { Spacer(modifier = Modifier.height(8.dp)) }
                    }
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 0.dp,
                shadowElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = {
                            selectedIds = if (isAllSelected) emptySet()
                            else records.map { it.id }.toSet()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Checklist,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${selectedIds.size}/${records.size}",
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // 只有Tab 3和Tab 4时才能变更报销状态
                        if (selectedTab == 3 || selectedTab == 4) {
                            FilledTonalButton(
                                onClick = { applyBatchReimburseStatus() },
                                enabled = selectedIds.isNotEmpty() && !isDeleting,
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Text(
                                    text = if (selectedTab == 3) "设为已报销" else "撤销报销",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        FilledTonalButton(
                            onClick = { if (selectedIds.isNotEmpty()) showBatchDeleteConfirm = true },
                            enabled = selectedIds.isNotEmpty() && !isDeleting,
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "删除",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }

    if (showBatchDeleteConfirm) {
        BatchDeleteConfirmDialog(
            selectedCount = selectedIds.size,
            onConfirm = {
                val ids = selectedIds.toList()
                viewModel.deleteRecordsByIds(ids) { count ->
                    if (count > 0) {
                        exitSelectionMode()
                    }
                }
            },
            onDismiss = {
                if (!isDeleting) showBatchDeleteConfirm = false
            },
            isLoading = isDeleting
        )
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
            },
            isLoading = isDeleting
        )
    }
}