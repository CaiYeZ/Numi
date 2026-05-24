package com.herb.numi.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.herb.numi.data.CategoryIcon
import com.herb.numi.data.imageVector

/**
 * 交易信息概览组件（水平布局：种类在前，金额在后）
 * 左侧显示类别图标+名称，右侧显示金额数值
 *
 * @param category 类别名称（用于显示文本）
 * @param amount 金额字符串
 * @param recordType 记录类型（"expense" 或 "income"，用于决定颜色）
 * @param categoryIcon 类别图标枚举，由调用方根据预设或自定义分类解析后传入
 */
@Composable
fun RecordTransactionOverview(
    category: String,
    amount: String,
    recordType: String,
    categoryIcon: CategoryIcon,
    modifier: Modifier = Modifier
) {
    val displayAmount = if (amount.isEmpty()) "0.00" else amount
    val typeColor = if (recordType == "expense") Color(0xFFF44336) else Color(0xFF4CAF50)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CategoryDisplay(
            category = category,
            categoryIcon = categoryIcon,
            typeColor = typeColor
        )
        AmountDisplay(amount = displayAmount)
    }
}

/**
 * 类别显示（图标 + 名称）
 */
@Composable
private fun CategoryDisplay(
    category: String,
    categoryIcon: CategoryIcon,
    typeColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        CategoryIconBox(
            icon = categoryIcon,
            typeColor = typeColor
        )
        Text(
            text = category,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * 类别图标（圆形背景）
 */
@Composable
private fun CategoryIconBox(
    icon: CategoryIcon,
    typeColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(typeColor.copy(alpha = 0.1f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon.imageVector,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = typeColor
        )
    }
}

/**
 * 金额显示
 */
@Composable
private fun AmountDisplay(
    amount: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = amount,
        fontSize = 36.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = modifier
    )
}
