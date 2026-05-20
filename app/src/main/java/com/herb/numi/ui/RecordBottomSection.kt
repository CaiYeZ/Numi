package com.herb.numi.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    isEditingMode: Boolean,
    onNoteChange: (String) -> Unit,
    onShowTimePicker: () -> Unit,
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
            backgroundColor = backgroundColor,
            onNoteChange = onNoteChange,
            onShowTimePicker = onShowTimePicker
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
 * 输入区域（备注区 + 时间选择区）
 */
@Composable
private fun InputSection(
    note: String,
    selectedTime: Calendar,
    backgroundColor: Color,
    onNoteChange: (String) -> Unit,
    onShowTimePicker: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        NoteInputRow(
            note = note,
            onNoteChange = onNoteChange
        )
        
        TimeSelectorRow(
            selectedTime = selectedTime,
            onShowTimePicker = onShowTimePicker
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
 */
@Composable
private fun TimeSelectorRow(
    selectedTime: Calendar,
    onShowTimePicker: () -> Unit
) {
    val now = Calendar.getInstance()
    val isToday = selectedTime.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
            selectedTime.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR)

    val timeText = if (isToday) {
        "今天 ${java.text.SimpleDateFormat("HH:mm", Locale.getDefault()).format(selectedTime.time)}"
    } else {
        java.text.SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()).format(selectedTime.time)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 时间选择药丸形标签
        TimePill(
            text = timeText,
            onClick = onShowTimePicker
        )
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
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium
        )
    }
}
