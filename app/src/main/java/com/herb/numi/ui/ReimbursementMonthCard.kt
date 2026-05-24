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
import java.util.*

/**
 * 报销统计月份分组卡片（按月）
 * 支持展开/折叠，显示月份、总金额、记录列表
 *
 * @param amountMode 计算方式: "sum"=总和, "balance"=收入-支出
 */
@Composable
fun ReimbursementMonthCard(
    monthKey: String,
    records: List<Record>,
    isExpanded: Boolean,
    onToggleExpanded: () -> Unit,
    isSelectionMode: Boolean,
    selectedIds: Set<Long>,
    onRecordClick: (Record) -> Unit,
    onRecordLongClick: (Record) -> Unit,
    onGroupSelect: () -> Unit,
    amountMode: String = "sum",
    customCategories: List<CustomCategory> = emptyList(),
    modifier: Modifier = Modifier
) {
    val totalAmount = when (amountMode) {
        "balance" -> {
            val income = records.filter { it.type == "income" }.sumOf { it.amount }
            val expense = records.filter { it.type == "expense" }.sumOf { it.amount }
            income - expense
        }
        else -> records.sumOf { it.amount }
    }
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

                    val parts = monthKey.split("-")
                    val yearStr = parts[0]
                    val monthStr = parts[1]



                    // 普通模式与批量模式保持一致的月份显示
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
                            text = "${monthStr.toInt()}月",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 14.sp
                        )
                        Text(
                            text = yearStr,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 10.sp
                        )
                    }

                    Column {
                        Text(
                            text = "${yearStr}年${monthStr.toInt()}月",
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
