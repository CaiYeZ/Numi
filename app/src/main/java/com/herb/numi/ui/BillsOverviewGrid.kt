package com.herb.numi.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.herb.numi.data.Record
import java.util.*

/**
 * 统计概览网格
 * 显示支出、收入、结余、日均支出等关键指标
 */
@Composable
fun OverviewGrid(
    records: List<Record>,
    timePeriod: TimePeriod,
    selectedDate: Calendar,
    modifier: Modifier = Modifier
) {
    val filteredRecords = records.filter { record ->
        val recordCalendar = Calendar.getInstance().apply { timeInMillis = record.createdAt }
        if (timePeriod == TimePeriod.MONTH) {
            recordCalendar.get(Calendar.YEAR) == selectedDate.get(Calendar.YEAR) &&
            recordCalendar.get(Calendar.MONTH) == selectedDate.get(Calendar.MONTH)
        } else {
            recordCalendar.get(Calendar.YEAR) == selectedDate.get(Calendar.YEAR)
        }
    }

    val totalExpense = filteredRecords.filter { it.type == "expense" }.sumOf { it.amount }
    val totalIncome = filteredRecords.filter { it.type == "income" }.sumOf { it.amount }
    val balance = totalIncome - totalExpense

    val days = if (timePeriod == TimePeriod.MONTH) {
        selectedDate.getActualMaximum(Calendar.DAY_OF_MONTH).toDouble()
    } else {
        12.0
    }
    val dailyExpense = if (days > 0) totalExpense / days else 0.0

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    label = "支出",
                    value = totalExpense
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    label = "收入",
                    value = totalIncome
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    label = "结余",
                    value = balance
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    label = "日均支出",
                    value = dailyExpense
                )
            }
        }
    }
}

/**
 * 统计卡片组件
 * 显示单个指标的标签和数值
 */
@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    label: String,
    value: Double
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "¥${String.format("%.2f", value)}",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
