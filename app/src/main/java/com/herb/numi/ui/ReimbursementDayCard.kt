package com.herb.numi.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.herb.numi.data.CustomCategory
import com.herb.numi.data.Record
import java.text.SimpleDateFormat
import java.util.*

/**
 * 报销统计日期分组卡片（按天）
 * 支持展开/折叠，显示日期、总金额、记录列表
 */
@Composable
fun ReimbursementDayCard(
    dateKey: String,
    records: List<Record>,
    isExpanded: Boolean,
    onToggleExpanded: () -> Unit,
    isSelectionMode: Boolean,
    selectedIds: Set<Long>,
    onRecordClick: (Record) -> Unit,
    onRecordLongClick: (Record) -> Unit,
    onGroupSelect: () -> Unit,
    customCategories: List<CustomCategory> = emptyList(),
    modifier: Modifier = Modifier
) {
    val totalAmount = records.sumOf { it.amount }
    val groupRecordIds = remember(records) { records.map { it.id }.toSet() }
    val isGroupFullySelected = remember(groupRecordIds, selectedIds) {
        groupRecordIds.isNotEmpty() && groupRecordIds.all { selectedIds.contains(it) }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpanded() }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    if (isSelectionMode) {
                        ReimbursementSelectionCheckbox(
                            isSelected = isGroupFullySelected,
                            modifier = Modifier.clickable { onGroupSelect() }
                        )
                    }

                    val cal = Calendar.getInstance().apply {
                        time = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateKey)!!
                    }

                    // 普通模式与批量模式保持一致的日期显示
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${cal.get(Calendar.MONTH) + 1}月",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 14.sp
                        )
                        Text(
                            text = "${cal.get(Calendar.YEAR)}",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 10.sp
                        )
                    }

                    Column {
                        Text(
                            text = formatReimbursementDate(dateKey),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${records.size}笔",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = String.format("%.2f", totalAmount),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "折叠" else "展开",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (isExpanded) {
                Column {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )
                    records.forEachIndexed { index, record ->
                        if (index > 0) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                            )
                        }
                        ReimbursementRecordItem(
                            record = record,
                            isSelectionMode = isSelectionMode,
                            isSelected = selectedIds.contains(record.id),
                            onClick = { onRecordClick(record) },
                            onLongClick = { onRecordLongClick(record) },
                            customCategories = customCategories
                        )
                    }
                }
            }
        }
    }
}

private fun formatReimbursementDate(dateKey: String): String {
    return try {
        val cal = Calendar.getInstance().apply {
            time = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateKey)!!
        }
        val dayOfWeek = SimpleDateFormat("EEEE", Locale.getDefault()).format(cal.time)
        val dayOfMonth = cal.get(Calendar.DAY_OF_MONTH)
        "$dayOfWeek $dayOfMonth"
    } catch (e: Exception) {
        dateKey
    }
}
