package com.herb.numi.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.herb.numi.data.ReimburseStatus
import java.util.*

/**
 * 底部整合区域（备注区 + 时间选择区 + 键盘区）
 * 三个区域整合为一个整体，固定在页面底部
 * 参考图片风格：浅灰色背景，白色药丸形标签，无白色背景的备注区
 */
@Composable
fun RecordBottomSection(
    amount: String,
    note: String,
    selectedTime: Calendar,
    reimburseStatus: String,
    isEditingMode: Boolean,
    onNoteChange: (String) -> Unit,
    onShowTimePicker: () -> Unit,
    onReimburseStatusChange: (String) -> Unit,
    onNumberClick: (String) -> Unit,
    onDeleteClick: () -> Unit,
    onSaveClick: () -> Unit,
    onReRecordClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = MaterialTheme.colorScheme.surfaceContainerLow

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundColor)
    ) {
        InputSection(
            note = note,
            selectedTime = selectedTime,
            reimburseStatus = reimburseStatus,
            backgroundColor = backgroundColor,
            onNoteChange = onNoteChange,
            onShowTimePicker = onShowTimePicker,
            onReimburseStatusChange = onReimburseStatusChange
        )
        RecordNumberKeyboard(
            amount = amount,
            isEditingMode = isEditingMode,
            onNumberClick = onNumberClick,
            onDeleteClick = onDeleteClick,
            onSaveClick = onSaveClick,
            onReRecordClick = onReRecordClick
        )
    }
}

/**
 * 输入区域（备注区 + 时间选择区 + 报销状态选择区）
 */
@Composable
private fun InputSection(
    note: String,
    selectedTime: Calendar,
    reimburseStatus: String,
    backgroundColor: Color,
    onNoteChange: (String) -> Unit,
    onShowTimePicker: () -> Unit,
    onReimburseStatusChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(vertical = 0.dp),
    ) {
        NoteInputRow(
            note = note,
            onNoteChange = onNoteChange
        )

        TimeSelectorRow(
            selectedTime = selectedTime,
            reimburseStatus = reimburseStatus,
            onShowTimePicker = onShowTimePicker,
            onReimburseStatusChange = onReimburseStatusChange
        )
    }
}

/**
 * 备注输入行
 * 参考图片：无白色背景，灰色占位文字直接显示在浅灰背景上
 */
@Composable
private fun NoteInputRow(
    note: String,
    onNoteChange: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        BasicTextField(
            value = note,
            onValueChange = { newValue -> if (newValue.length <= 30) onNoteChange(newValue) },
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 14.sp
            ),
            decorationBox = { innerTextField ->
                if (note.isEmpty()) {
                    Text(
                        text = "点此输入备注...",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                }
                innerTextField()
            },
            maxLines = 1
        )
    }
}

/**
 * 时间选择行
 * 参考图片：白色药丸形背景，显示"今天 16:50"格式
 * 同时包含报销状态选择按钮
 */
@Composable
private fun TimeSelectorRow(
    selectedTime: Calendar,
    reimburseStatus: String,
    onShowTimePicker: () -> Unit,
    onReimburseStatusChange: (String) -> Unit
) {
    val now = Calendar.getInstance()
    val isToday = selectedTime.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
            selectedTime.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR)

    val timeText = if (isToday) {
        "今天 ${java.text.SimpleDateFormat("HH:mm", Locale.getDefault()).format(selectedTime.time)}"
    } else {
        java.text.SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()).format(selectedTime.time)
    }

    var showReimburseMenu by remember { mutableStateOf(false) }
    val currentStatus = ReimburseStatus.fromValue(reimburseStatus)
    val statusTextColor = if (currentStatus == ReimburseStatus.NONE) {
        MaterialTheme.colorScheme.onSurfaceVariant
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 0.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 时间选择药丸形标签
        TimePill(
            text = timeText,
            onClick = onShowTimePicker
        )

        // 报销状态选择药丸形标签
        Box {
            TimePill(
                text = currentStatus.label,
                textColor = statusTextColor,
                onClick = { showReimburseMenu = true }
            )

            DropdownMenu(
                expanded = showReimburseMenu,
                onDismissRequest = { showReimburseMenu = false }
            ) {
                ReimburseStatus.entries.forEach { status ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = status.label,
                                color = if (status == ReimburseStatus.NONE) {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            )
                        },
                        onClick = {
                            onReimburseStatusChange(status.value)
                            showReimburseMenu = false
                        }
                    )
                }
            }
        }
    }
}

/**
 * 药丸形标签
 * 白色圆角背景，用于时间选择等
 */
@Composable
private fun TimePill(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    textColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            color = textColor,
            fontWeight = FontWeight.Medium
        )
    }
}
