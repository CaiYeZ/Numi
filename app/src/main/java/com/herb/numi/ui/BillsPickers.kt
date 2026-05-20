package com.herb.numi.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.*

/**
 * 月份选择对话框
 * 支持选择年份和月份
 */
@Composable
fun MonthPickerDialog(
    selectedDate: Calendar,
    onDateSelected: (Calendar) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var tempYear by remember { mutableIntStateOf(selectedDate.get(Calendar.YEAR)) }
    var tempMonth by remember { mutableIntStateOf(selectedDate.get(Calendar.MONTH)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择月份", fontWeight = FontWeight.SemiBold) },
        text = {
            Column {
                MonthYearSelector(
                    year = tempYear,
                    onYearChange = { tempYear = it }
                )
                Spacer(modifier = Modifier.height(16.dp))
                MonthGrid(
                    selectedMonth = tempMonth,
                    onMonthChange = { tempMonth = it }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val newDate = selectedDate.clone() as Calendar
                newDate.set(Calendar.YEAR, tempYear)
                newDate.set(Calendar.MONTH, tempMonth)
                onDateSelected(newDate)
            }) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

/**
 * 年份选择器
 */
@Composable
private fun MonthYearSelector(
    year: Int,
    onYearChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { onYearChange(year - 1) }) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "上一年")
        }
        Text("$year 年", fontWeight = FontWeight.SemiBold)
        IconButton(onClick = { onYearChange(year + 1) }) {
            Icon(Icons.AutoMirrored.Filled.ArrowForward, "下一年")
        }
    }
}

/**
 * 月份网格
 */
@Composable
private fun MonthGrid(
    selectedMonth: Int,
    onMonthChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val months = (0..11).chunked(3)
    months.forEach { row ->
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            row.forEach { month ->
                MonthItem(
                    month = month,
                    isSelected = selectedMonth == month,
                    onClick = { onMonthChange(month) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

/**
 * 月份项
 */
@Composable
private fun MonthItem(
    month: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Box(
            modifier = Modifier.padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "${month + 1}月",
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                       else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 年份选择对话框
 * 支持快速选择年份
 */
@Composable
fun YearPickerDialog(
    selectedDate: Calendar,
    onDateSelected: (Calendar) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var tempYear by remember { mutableIntStateOf(selectedDate.get(Calendar.YEAR)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择年份", fontWeight = FontWeight.SemiBold) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                YearNavigationBar(
                    year = tempYear,
                    onYearChange = { tempYear = it },
                    step = 10
                )
                Spacer(modifier = Modifier.height(16.dp))
                YearGrid(
                    selectedYear = tempYear,
                    onYearChange = { tempYear = it }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val newDate = selectedDate.clone() as Calendar
                newDate.set(Calendar.YEAR, tempYear)
                onDateSelected(newDate)
            }) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

/**
 * 年份导航栏
 */
@Composable
private fun YearNavigationBar(
    year: Int,
    onYearChange: (Int) -> Unit,
    step: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { onYearChange(year - step) }) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "向前${step}年")
        }
        Text("$year 年", fontWeight = FontWeight.Bold, fontSize = 24.sp)
        IconButton(onClick = { onYearChange(year + step) }) {
            Icon(Icons.AutoMirrored.Filled.ArrowForward, "向后${step}年")
        }
    }
}

/**
 * 年份网格
 */
@Composable
private fun YearGrid(
    selectedYear: Int,
    onYearChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val years = (selectedYear - 5..selectedYear + 4).toList().chunked(3)
    years.forEach { row ->
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            row.forEach { year ->
                YearItem(
                    year = year,
                    isSelected = selectedYear == year,
                    onClick = { onYearChange(year) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

/**
 * 年份项
 */
@Composable
private fun YearItem(
    year: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Box(
            modifier = Modifier.padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "$year",
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                       else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
