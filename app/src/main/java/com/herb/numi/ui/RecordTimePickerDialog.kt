package com.herb.numi.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

/**
 * 时间选择对话框
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordTimePickerDialog(
    selectedTime: Calendar,
    onTimeSelected: (Calendar) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var tempDate by remember { mutableStateOf(selectedTime.clone() as Calendar) }
    var tempHour by remember { mutableIntStateOf(selectedTime.get(Calendar.HOUR_OF_DAY)) }
    var tempMinute by remember { mutableIntStateOf(selectedTime.get(Calendar.MINUTE)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "选择时间",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column {
                DateSelectionSection(
                    tempDate = tempDate,
                    onDateChange = { tempDate = it }
                )
                Spacer(modifier = Modifier.height(16.dp))
                TimeSelectionSection(
                    tempHour = tempHour,
                    tempMinute = tempMinute,
                    onHourChange = { tempHour = it },
                    onMinuteChange = { tempMinute = it }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val newTime = Calendar.getInstance().apply {
                    set(Calendar.YEAR, tempDate.get(Calendar.YEAR))
                    set(Calendar.MONTH, tempDate.get(Calendar.MONTH))
                    set(Calendar.DAY_OF_MONTH, tempDate.get(Calendar.DAY_OF_MONTH))
                    set(Calendar.HOUR_OF_DAY, tempHour)
                    set(Calendar.MINUTE, tempMinute)
                    set(Calendar.SECOND, 0)
                }
                onTimeSelected(newTime)
            }) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
        modifier = modifier
    )
}

/**
 * 日期选择区域
 */
@Composable
private fun DateSelectionSection(
    tempDate: Calendar,
    onDateChange: (Calendar) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "日期",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        DateNavigationRow(
            tempDate = tempDate,
            onDateChange = onDateChange
        )
        Spacer(modifier = Modifier.height(8.dp))
        DayGrid(
            tempDate = tempDate,
            onDateChange = onDateChange
        )
    }
}

/**
 * 日期导航行
 */
@Composable
private fun DateNavigationRow(
    tempDate: Calendar,
    onDateChange: (Calendar) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = {
                onDateChange((tempDate.clone() as Calendar).apply {
                    add(Calendar.MONTH, -1)
                })
            }
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "上个月")
        }

        Text(
            text = SimpleDateFormat("yyyy年MM月", Locale.getDefault())
                .format(tempDate.time),
            fontWeight = FontWeight.Bold
        )

        IconButton(
            onClick = {
                onDateChange((tempDate.clone() as Calendar).apply {
                    add(Calendar.MONTH, 1)
                })
            }
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowForward, "下个月")
        }
    }
}

/**
 * 日期网格
 */
@Composable
private fun DayGrid(
    tempDate: Calendar,
    onDateChange: (Calendar) -> Unit,
    modifier: Modifier = Modifier
) {
    val daysInMonth = tempDate.getActualMaximum(Calendar.DAY_OF_MONTH)
    val currentDay = tempDate.get(Calendar.DAY_OF_MONTH)

    Column(modifier = modifier) {
        (1..daysInMonth).chunked(7).forEach { week ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                week.forEach { day ->
                    DayItem(
                        day = day,
                        isSelected = day == currentDay,
                        onClick = {
                            onDateChange((tempDate.clone() as Calendar).apply {
                                set(Calendar.DAY_OF_MONTH, day)
                            })
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

/**
 * 日期项
 */
@Composable
private fun DayItem(
    day: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .size(36.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        color = if (isSelected) MaterialTheme.colorScheme.primary
               else Color.Transparent,
        shape = CircleShape
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = day.toString(),
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                       else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * 时间选择区域
 */
@Composable
private fun TimeSelectionSection(
    tempHour: Int,
    tempMinute: Int,
    onHourChange: (Int) -> Unit,
    onMinuteChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "时间",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NumberPicker(
                value = tempHour,
                range = 0..23,
                onValueChange = onHourChange
            )
            Text(
                text = ":",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            NumberPicker(
                value = tempMinute,
                range = 0..59,
                onValueChange = onMinuteChange
            )
        }
    }
}

/**
 * 数字选择器
 */
@Composable
private fun NumberPicker(
    value: Int,
    range: IntRange,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        IconButton(
            onClick = {
                if (value < range.last) onValueChange(value + 1)
            }
        ) {
            Icon(Icons.Default.KeyboardArrowUp, "增加")
        }

        Surface(
            modifier = Modifier.width(60.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = String.format("%02d", value),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        IconButton(
            onClick = {
                if (value > range.first) onValueChange(value - 1)
            }
        ) {
            Icon(Icons.Default.KeyboardArrowDown, "减少")
        }
    }
}
