package com.herb.numi.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.herb.numi.data.Record
import java.text.SimpleDateFormat
import java.util.*

/**
 * 明细报表
 * 显示每天/每月的收入、支出、结余明细
 */
@Composable
fun DetailReport(
    records: List<Record>,
    timePeriod: TimePeriod,
    selectedDate: Calendar,
    onDayClick: (Int, List<Record>) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = if (timePeriod == TimePeriod.MONTH) "日报表" else "月报表",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))
            DetailReportHeader()
            DetailReportBody(
                records = records,
                timePeriod = timePeriod,
                selectedDate = selectedDate,
                onDayClick = onDayClick
            )
        }
    }
}

/**
 * 明细报表表头
 */
@Composable
private fun DetailReportHeader(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "日期",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "收入",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End
        )
        Text(
            text = "支出",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End
        )
        Text(
            text = "结余",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End
        )
    }
}

/**
 * 明细报表内容
 */
@Composable
private fun DetailReportBody(
    records: List<Record>,
    timePeriod: TimePeriod,
    selectedDate: Calendar,
    onDayClick: (Int, List<Record>) -> Unit,
    modifier: Modifier = Modifier
) {
    val days = if (timePeriod == TimePeriod.MONTH) selectedDate.getActualMaximum(Calendar.DAY_OF_MONTH) else 12

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        (1..days).forEach { day ->
            val dayRecords = filterRecordsByDay(
                records = records,
                timePeriod = timePeriod,
                selectedDate = selectedDate,
                day = day
            )

            if (dayRecords.isNotEmpty()) {
                val dayExpense = dayRecords.filter { it.type == "expense" }.sumOf { it.amount }
                val dayIncome = dayRecords.filter { it.type == "income" }.sumOf { it.amount }
                val dayBalance = dayIncome - dayExpense

                DetailReportRow(
                    day = day,
                    dayIncome = dayIncome,
                    dayExpense = dayExpense,
                    dayBalance = dayBalance,
                    timePeriod = timePeriod,
                    selectedDate = selectedDate,
                    onClick = { onDayClick(day, dayRecords) }
                )
            }
        }
    }
}

/**
 * 过滤指定日期的记录
 */
private fun filterRecordsByDay(
    records: List<Record>,
    timePeriod: TimePeriod,
    selectedDate: Calendar,
    day: Int
): List<Record> {
    return if (timePeriod == TimePeriod.MONTH) {
        records.filter { record ->
            val c = Calendar.getInstance().apply { timeInMillis = record.createdAt }
            c.get(Calendar.YEAR) == selectedDate.get(Calendar.YEAR) &&
            c.get(Calendar.MONTH) == selectedDate.get(Calendar.MONTH) &&
            c.get(Calendar.DAY_OF_MONTH) == day
        }
    } else {
        records.filter { record ->
            val c = Calendar.getInstance().apply { timeInMillis = record.createdAt }
            c.get(Calendar.YEAR) == selectedDate.get(Calendar.YEAR) &&
            c.get(Calendar.MONTH) == day - 1
        }
    }
}

/**
 * 明细报表单行
 */
@Composable
private fun DetailReportRow(
    day: Int,
    dayIncome: Double,
    dayExpense: Double,
    dayBalance: Double,
    timePeriod: TimePeriod,
    selectedDate: Calendar,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (timePeriod == TimePeriod.MONTH) String.format("%02d-%02d", selectedDate.get(Calendar.MONTH) + 1, day)
                   else "${day}月",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = if (dayIncome > 0) "¥${String.format("%.2f", dayIncome)}" else "¥0.00",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End
        )
        Text(
            text = if (dayExpense > 0) "¥${String.format("%.2f", dayExpense)}" else "¥0.00",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End
        )
        Text(
            text = "¥${String.format("%.2f", dayBalance)}",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End
        )
    }
}

/**
 * 当天消费详情底部弹窗
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayDetailBottomSheet(
    day: Int,
    records: List<Record>,
    timePeriod: TimePeriod,
    selectedDate: Calendar,
    onDismiss: () -> Unit,
    onRecordClick: (Record) -> Unit
) {
    val dateTitle = if (timePeriod == TimePeriod.MONTH) {
        String.format("%02d-%02d", selectedDate.get(Calendar.MONTH) + 1, day)
    } else {
        "${day}月"
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = dateTitle,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (records.isEmpty()) {
                Text(
                    text = "暂无记录",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                records.sortedByDescending { it.createdAt }.forEachIndexed { index, record ->
                    if (index > 0) {
                        HorizontalDivider(
                            thickness = 0.1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                    DayDetailItem(
                        record = record,
                        onClick = { onRecordClick(record) }
                    )
                }
            }
        }
    }
}

/**
 * 当天消费详情单条记录
 */
@Composable
private fun DayDetailItem(
    record: Record,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CategoryIconSmall(category = record.category)
            
            Text(
                text = record.category,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            if (!record.note.isNullOrBlank()) {
                Text(
                    text = " · ${record.note}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }

        Text(
            text = if (record.type == "income") {
                "+¥${String.format("%.2f", record.amount)}"
            } else {
                "-¥${String.format("%.2f", record.amount)}"
            },
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (record.type == "income") {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.error
            }
        )
    }
}

/**
 * 小尺寸分类图标
 */
@Composable
private fun CategoryIconSmall(
    category: String,
    modifier: Modifier = Modifier
) {
    val icon = getCategoryIconForName(category)
    
    Box(
        modifier = modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = category,
            modifier = Modifier.size(14.dp),
            tint = Color.White
        )
    }
}

/**
 * 根据分类名称获取对应图标向量
 */
private fun getCategoryIconForName(category: String): ImageVector {
    val expenseIcon = com.herb.numi.data.ExpenseCategory.icons[category]
    val incomeIcon = com.herb.numi.data.IncomeCategory.icons[category]
    val icon = expenseIcon ?: incomeIcon ?: com.herb.numi.data.CategoryIcon.OTHER

    return when (icon) {
        com.herb.numi.data.CategoryIcon.RESTAURANT -> Icons.Filled.Restaurant
        com.herb.numi.data.CategoryIcon.TRANSPORT -> Icons.Filled.DirectionsCar
        com.herb.numi.data.CategoryIcon.SHOPPING -> Icons.Filled.ShoppingBag
        com.herb.numi.data.CategoryIcon.ENTERTAINMENT -> Icons.Filled.SportsEsports
        com.herb.numi.data.CategoryIcon.DAILY -> Icons.Filled.LocalConvenienceStore
        com.herb.numi.data.CategoryIcon.SALARY -> Icons.Filled.AccountBalance
        com.herb.numi.data.CategoryIcon.LIVING -> Icons.Filled.Home
        com.herb.numi.data.CategoryIcon.ALLOWANCE -> Icons.Filled.CardGiftcard
        com.herb.numi.data.CategoryIcon.TRANSFER -> Icons.Filled.SwapHoriz
        com.herb.numi.data.CategoryIcon.OTHER -> Icons.Filled.MoreHoriz
    }
}
