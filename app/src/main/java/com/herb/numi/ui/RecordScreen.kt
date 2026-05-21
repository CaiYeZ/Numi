package com.herb.numi.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * 记账页面主入口
 * 从上至下的布局结构，包含顶部区域和底部整合区域
 *
 * 职责：页面布局和状态管理，具体功能组件已拆分到独立文件
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordScreen(
    viewModel: RecordViewModel = viewModel(),
    onRecordingComplete: () -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    val amount by viewModel.amount.collectAsState()
    val recordType by viewModel.recordType.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val note by viewModel.note.collectAsState()
    val selectedTime by viewModel.selectedTime.collectAsState()
    val reimburseStatus by viewModel.reimburseStatus.collectAsState()
    val editingRecordId by viewModel.editingRecordId.collectAsState()
    var showTimePicker by remember { mutableStateOf(false) }

    val isEditingMode = editingRecordId != null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        RecordTopAppBar(
            isEditingMode = isEditingMode,
            onBack = {
                viewModel.cancelEdit()
                onNavigateBack()
            },
            modifier = Modifier.padding(top = TopContentSpacing - 8.dp)
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(0.dp)) // 顶部导航栏 与 Tabs 间隔
            RecordTransactionTypeSelector(
                selectedType = recordType,
                onTypeChange = { viewModel.setRecordType(it) }
            ) { page ->
                val type = if (page == 0) "expense" else "income"
                Column {
                    Spacer(modifier = Modifier.height(8.dp)) // Tabs 与 金额显示区 间隔
                    RecordTransactionOverview(
                        category = if (selectedCategory.isEmpty()) 
                            (if (type == "expense") "支出" else "收入") 
                            else selectedCategory,
                        amount = amount,
                        recordType = type
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    RecordCategorySelector(
                        selectedType = type,
                        selectedCategory = if (recordType == type) selectedCategory else "",
                        onCategoryChange = { viewModel.selectCategory(it) }
                    )
                }
            }
            Spacer(modifier = Modifier.weight(1f))
        }

        RecordBottomSection(
            amount = amount,
            note = note,
            selectedTime = selectedTime,
            reimburseStatus = reimburseStatus,
            isEditingMode = isEditingMode,
            onNoteChange = { viewModel.setNote(it) },
            onShowTimePicker = { showTimePicker = true },
            onReimburseStatusChange = { viewModel.setReimburseStatus(it) },
            onNumberClick = { viewModel.appendNumber(it) },
            onDeleteClick = { viewModel.deleteLastChar() },
            onSaveClick = {
                if (isEditingMode) {
                    viewModel.allRecords.value.find { it.id == editingRecordId }?.let { record ->
                        viewModel.updateRecord(record) {
                            onRecordingComplete()
                        }
                    }
                } else {
                    viewModel.saveRecord {
                        onRecordingComplete()
                    }
                }
            },
            onReRecordClick = {
                viewModel.saveRecord {
                    viewModel.resetInput()
                }
            }
        )
    }

    if (showTimePicker) {
        RecordTimePickerDialog(
            selectedTime = selectedTime,
            onTimeSelected = {
                viewModel.setSelectedTime(it)
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false }
        )
    }
}

/**
 * 顶部导航栏
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecordTopAppBar(
    isEditingMode: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = {
            Text(
                text = if (isEditingMode) "编辑记录" else "记账",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "返回"
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        ),
        windowInsets = WindowInsets(0.dp),
        modifier = modifier
    )
}
