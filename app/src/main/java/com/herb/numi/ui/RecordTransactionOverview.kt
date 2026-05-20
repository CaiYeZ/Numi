package com.herb.numi.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.herb.numi.data.ExpenseCategory
import com.herb.numi.data.IncomeCategory

/**
 * 交易信息概览组件（水平布局：种类在前，金额在后）
 * 左侧显示类别图标+名称，右侧显示金额数值
 */
@Composable
fun RecordTransactionOverview(
    category: String,
    amount: String,
    recordType: String,
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
            recordType = recordType,
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
    recordType: String,
    typeColor: Color,
    modifier: Modifier = Modifier
) {
    val icon = getCategoryIcon(category, recordType)
    
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        CategoryIconBox(
            icon = icon,
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
 * 获取类别对应的图标
 */
private fun getCategoryIcon(category: String, recordType: String): CategoryIcon {
    val iconMap = if (recordType == "expense") {
        ExpenseCategory.icons
    } else {
        IncomeCategory.icons
    }
    return iconMap[category] ?: CategoryIcon.OTHER
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
            imageVector = getCategoryIconVector(icon),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = typeColor
        )
    }
}

/**
 * 根据 CategoryIcon 获取对应的 Material Icons 向量
 */
private fun getCategoryIconVector(icon: CategoryIcon): androidx.compose.ui.graphics.vector.ImageVector {
    return when (icon) {
        CategoryIcon.RESTAURANT -> Icons.Filled.Restaurant
        CategoryIcon.TRANSPORT -> Icons.Filled.DirectionsCar
        CategoryIcon.SHOPPING -> Icons.Filled.ShoppingBag
        CategoryIcon.ENTERTAINMENT -> Icons.Filled.SportsEsports
        CategoryIcon.DAILY -> Icons.Filled.LocalConvenienceStore
        CategoryIcon.SALARY -> Icons.Filled.AccountBalance
        CategoryIcon.LIVING -> Icons.Filled.Home
        CategoryIcon.ALLOWANCE -> Icons.Filled.CardGiftcard
        CategoryIcon.TRANSFER -> Icons.Filled.SwapHoriz
        CategoryIcon.OTHER -> Icons.Filled.MoreHoriz
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
