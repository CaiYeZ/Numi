package com.herb.numi.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.herb.numi.data.CategoryIcon
import com.herb.numi.data.ExpenseCategory
import com.herb.numi.data.IncomeCategory

/**
 * 交易种类选择区域（网格布局，每行4个）
 * 每个类别包含圆形图标背景和类别名称
 */
@Composable
fun RecordCategorySelector(
    selectedType: String,
    selectedCategory: String,
    onCategoryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = if (selectedType == "expense") {
        ExpenseCategory.categories
    } else {
        IncomeCategory.categories
    }

    val iconMap = if (selectedType == "expense") {
        ExpenseCategory.icons
    } else {
        IncomeCategory.icons
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 300.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { category ->
            CategoryGridItem(
                category = category,
                icon = iconMap[category] ?: CategoryIcon.OTHER,
                isSelected = selectedCategory == category,
                onClick = { onCategoryChange(category) }
            )
        }
    }
}

/**
 * 网格类别项（带圆形图标）
 */
@Composable
private fun CategoryGridItem(
    category: String,
    icon: CategoryIcon,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        label = "category_background"
    )
    val iconColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "category_icon_color"
    )

    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        CategoryCircleIcon(
            backgroundColor = backgroundColor,
            iconColor = iconColor,
            icon = icon
        )
        CategoryName(
            category = category,
            isSelected = isSelected
        )
    }
}

/**
 * 类别圆形图标
 */
@Composable
private fun CategoryCircleIcon(
    backgroundColor: Color,
    iconColor: Color,
    icon: CategoryIcon,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = getCategoryIconVector(icon),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = iconColor
        )
    }
}

/**
 * 根据 CategoryIcon 获取对应的 Material Icons 向量
 */
private fun getCategoryIconVector(icon: CategoryIcon): ImageVector {
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
 * 类别名称
 */
@Composable
private fun CategoryName(
    category: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier
) {
    Text(
        text = category,
        fontSize = 12.sp,
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
        modifier = modifier
    )
}
