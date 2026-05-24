package com.herb.numi.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.herb.numi.data.CategoryIcon
import com.herb.numi.data.CustomCategory
import com.herb.numi.data.ExpenseCategory
import com.herb.numi.data.IncomeCategory
import com.herb.numi.data.Record
import com.herb.numi.data.ReimburseStatus
import com.herb.numi.data.imageVector
import java.text.SimpleDateFormat
import java.util.*

/**
 * 首页记录日期分组卡片组件
 * 将日期头部和该日期下的所有记录包裹在一个卡片中
 *
 * @param dateKey 日期键，格式为 yyyy-MM-dd
 * @param records 该日期下的所有记录
 * @param isSelectionMode 是否处于选择模式
 * @param selectedIds 已选中的记录ID集合
 * @param onRecordClick 记录点击回调
 * @param onRecordLongClick 记录长按回调
 * @param customCategories 自定义分类列表，用于解析自定义分类的图标
 * @param modifier Modifier
 */
@Composable
fun HomeRecordDayCard(
    dateKey: String,
    records: List<Record>,
    isSelectionMode: Boolean,
    selectedIds: Set<Long>,
    onRecordClick: (Record) -> Unit,
    onRecordLongClick: (Record) -> Unit,
    customCategories: List<CustomCategory> = emptyList(),
    modifier: Modifier = Modifier
) {
    val dayIncome = records.filter { it.type == "income" }.sumOf { it.amount }
    val dayExpense = records.filter { it.type == "expense" }.sumOf { it.amount }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp // 默认阴影
        )
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 4.dp, start = 12.dp, end = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatGroupDate(dateKey),
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

            records.forEachIndexed { index, record ->
                if (index > 0) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                }
                HomeRecordItem(
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

/**
 * 首页记录项组件
 * 支持单击查看详情和长按批量选择
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeRecordItem(
    record: Record,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    customCategories: List<CustomCategory> = emptyList(),
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer
        else Color.Transparent,
        label = "background_color"
    )

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 0.98f else 1f,
        label = "scale"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .background(backgroundColor)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            )
            .padding(vertical = 12.dp, horizontal = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            HomeRecordItemLeftContent(
                record = record,
                isSelectionMode = isSelectionMode,
                isSelected = isSelected,
                customCategories = customCategories
            )
            HomeRecordItemRightContent(record = record)
        }
    }
}

/**
 * 记录项左侧内容（复选框/图标 + 分类、备注和时间）
 */
@Composable
private fun HomeRecordItemLeftContent(
    record: Record,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    customCategories: List<CustomCategory>,
    modifier: Modifier = Modifier
) {
    val categoryIcon = resolveHomeCategoryIcon(record.category, customCategories)

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (isSelectionMode) {
            SelectionCheckbox(isSelected = isSelected)
        } else {
            CategoryIconCircle(categoryIcon = categoryIcon)
        }

        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                RecordCategory(category = record.category)
                if (!record.note.isNullOrBlank()) {
                    Text(
                        text = " · ",
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    RecordNote(note = record.note)
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            RecordExactTime(timestamp = record.createdAt)
        }
    }
}

/**
 * 分类圆形图标
 * 使用传入的 CategoryIcon 枚举显示对应图标，统一使用主题蓝色背景
 */
@Composable
private fun CategoryIconCircle(
    categoryIcon: CategoryIcon,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = categoryIcon.imageVector,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = Color.White
        )
    }
}

/**
 * 根据分类名称解析对应的图标枚举
 * 优先从预设分类中查找，找不到则从自定义分类中查找
 */
private fun resolveHomeCategoryIcon(
    category: String,
    customCategories: List<CustomCategory>
): CategoryIcon {
    // 先从预设分类中查找（不区分支出/收入，两边都找）
    val presetIcon = ExpenseCategory.icons[category] ?: IncomeCategory.icons[category]
    if (presetIcon != null) {
        return presetIcon
    }
    // 再从自定义分类中查找
    return customCategories
        .find { it.name == category }
        ?.icon
        ?: CategoryIcon.MORE_HORIZ
}

/**
 * 复选框组件
 */
@Composable
private fun SelectionCheckbox(
    isSelected: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = CircleShape,
        color = if (isSelected) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier.size(24.dp)
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "已选中",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.padding(4.dp)
            )
        }
    }
}

/**
 * 记录分类名称
 */
@Composable
private fun RecordCategory(
    category: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = category,
        fontSize = 15.sp,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = modifier
    )
}

/**
 * 记录备注
 */
@Composable
private fun RecordNote(
    note: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = note,
        fontSize = 12.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier,
        maxLines = 1
    )
}

/**
 * 记录精确时间（HH:mm）
 */
@Composable
private fun RecordExactTime(
    timestamp: Long,
    modifier: Modifier = Modifier
) {
    val timeStr = remember(timestamp) {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))
    }
    Text(
        text = timeStr,
        fontSize = 12.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
    )
}

/**
 * 记录项右侧内容（金额 + 报销状态）
 */
@Composable
private fun HomeRecordItemRightContent(
    record: Record,
    modifier: Modifier = Modifier
) {
    val status = ReimburseStatus.fromValue(record.reimburseStatus)
    val showStatus = status != ReimburseStatus.NONE

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "${if (record.type == "expense") "-" else "+"}${String.format("%.2f", record.amount)}",
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (record.type == "expense") MaterialTheme.colorScheme.error
            else MaterialTheme.colorScheme.primary
        )
        if (showStatus) {
            Text(
                text = status.label,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
            )
        }
    }
}