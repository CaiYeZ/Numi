package com.herb.numi.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
 * 报销页面通用组件
 * 包含分类图标、复选框、记录项等
 */

/**
 * 分类图标（圆形主题色背景）
 * 直接使用传入的 CategoryIcon 枚举显示对应图标
 */
@Composable
fun ReimbursementCategoryIcon(
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
private fun resolveReimbursementCategoryIcon(
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
 * 报销列表批量选择圆形勾选控件
 */
@Composable
fun ReimbursementSelectionCheckbox(
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
 * 报销记录项组件
 */
@Composable
fun ReimbursementRecordItem(
    record: Record,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    customCategories: List<CustomCategory> = emptyList(),
    modifier: Modifier = Modifier
) {
    val status = ReimburseStatus.fromValue(record.reimburseStatus)
    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
    val scale = if (isSelected) 0.98f else 1f
    val categoryIcon = resolveReimbursementCategoryIcon(record.category, customCategories)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (isSelectionMode) {
                    ReimbursementSelectionCheckbox(isSelected = isSelected)
                } else {
                    ReimbursementCategoryIcon(categoryIcon = categoryIcon)
                }

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = record.category,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (!record.note.isNullOrBlank()) {
                            Text(
                                text = " · ",
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = record.note,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    val timeStr = remember(record.createdAt) {
                        SimpleDateFormat("MM/dd HH:mm", Locale.getDefault()).format(Date(record.createdAt))
                    }
                    Text(
                        text = "$timeStr",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (record.type == "expense") "-" else "+"}${String.format("%.2f", record.amount)}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (record.type == "expense") MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.primary
                )
                if (status.label != "非报销") {
                    Text(
                        text = status.label,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}
