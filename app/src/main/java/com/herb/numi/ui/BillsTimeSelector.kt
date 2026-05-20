package com.herb.numi.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

/**
 * 时间周期枚举
 */
enum class TimePeriod {
    MONTH, YEAR
}

/**
 * 时间周期选择器
 * 提供月/年的切换选择
 */
@Composable
fun TimePeriodSelector(
    selectedPeriod: TimePeriod,
    onPeriodChange: (TimePeriod) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        TimePeriod.entries.forEach { period ->
            val isSelected = selectedPeriod == period
            val backgroundColor by animateColorAsState(
                targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                label = "tab_color"
            )
            val textColor by animateColorAsState(
                targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                label = "text_color"
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(backgroundColor)
                    .clickable { onPeriodChange(period) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (period == TimePeriod.MONTH) "月" else "年",
                    fontSize = 14.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = textColor
                )
            }
        }
    }
}

/**
 * 时间范围选择器
 * 显示当前选中的月份或年份，支持左右切换
 */
@Composable
fun TimeRangeSelector(
    selectedDate: Calendar,
    timePeriod: TimePeriod,
    onShowMonthPicker: () -> Unit,
    onShowYearPicker: () -> Unit,
    onDateChange: (Calendar) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                val newDate = selectedDate.clone() as Calendar
                if (timePeriod == TimePeriod.MONTH) newDate.add(Calendar.MONTH, -1) else newDate.add(Calendar.YEAR, -1)
                onDateChange(newDate)
            }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "上一个", tint = MaterialTheme.colorScheme.primary)
            }

            Text(
                text = if (timePeriod == TimePeriod.MONTH) {
                    SimpleDateFormat("yyyy年MM月", Locale.getDefault()).format(selectedDate.time)
                } else {
                    SimpleDateFormat("yyyy年", Locale.getDefault()).format(selectedDate.time)
                },
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.clickable {
                    if (timePeriod == TimePeriod.MONTH) onShowMonthPicker() else onShowYearPicker()
                }
            )

            IconButton(onClick = {
                val newDate = selectedDate.clone() as Calendar
                if (timePeriod == TimePeriod.MONTH) newDate.add(Calendar.MONTH, 1) else newDate.add(Calendar.YEAR, 1)
                onDateChange(newDate)
            }) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, "下一个", tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
