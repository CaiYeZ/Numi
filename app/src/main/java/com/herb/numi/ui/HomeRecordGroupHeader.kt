package com.herb.numi.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.herb.numi.data.Record
import java.text.SimpleDateFormat
import java.util.*

/**
 * 首页记录日期分组头部组件
 * 显示日期（如 05/18 周一）和当日收支汇总
 *
 * 职责：展示单日分组标题及该日期的收支统计
 *
 * @param dateKey 日期键，格式为 yyyy-MM-dd
 * @param records 该日期下的所有记录
 * @param modifier Modifier
 */
@Composable
fun HomeRecordGroupHeader(
    dateKey: String,
    records: List<Record>,
    modifier: Modifier = Modifier
) {
    val dayIncome = records.filter { it.type == "income" }.sumOf { it.amount }
    val dayExpense = records.filter { it.type == "expense" }.sumOf { it.amount }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 8.dp, start = 4.dp, end = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = formatGroupDateFromHeader(dateKey),
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (dayIncome > 0) {
                Text(
                    text = "收${String.format("%.2f", dayIncome)}",
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Normal
                )
            }
            if (dayExpense > 0) {
                Text(
                    text = "支${String.format("%.2f", dayExpense)}",
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}

/**
 * 将 yyyy-MM-dd 格式化为 MM/dd E（如 05/18 周一）
 */
internal fun formatGroupDateFromHeader(dateKey: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MM/dd E", Locale.CHINA)
        val date = inputFormat.parse(dateKey)
        date?.let { outputFormat.format(it) } ?: dateKey
    } catch (_: Exception) {
        dateKey
    }
}
