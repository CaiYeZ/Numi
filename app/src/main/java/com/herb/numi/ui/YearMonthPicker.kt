package com.herb.numi.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

/**
 * 极简现代风格年月选择器
 * 类似 iOS / Apple TV / 游戏 UI 风格
 */
@Composable
fun YearMonthPicker(
    selectedYear: Int,
    selectedMonth: Int,
    viewMode: Int, // 0=月视图, 1=年视图
    availableYears: List<Int>,
    onYearChange: (Int) -> Unit,
    onMonthChange: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val startYear = 2005
    val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
    val yearsPerPage = 10

    val totalYears = currentYear - startYear + 1
    val totalPages = if (totalYears > 0) (totalYears + yearsPerPage - 1) / yearsPerPage else 1

    // 计算当前选中年份所在页
    val currentYearIndex = selectedYear - startYear
    val selectedPage = if (currentYearIndex >= 0) currentYearIndex / yearsPerPage else 0

    // 跟踪当前显示的页（仅通过箭头切换）
    var displayPage by remember { mutableIntStateOf(selectedPage) }

    // 当弹窗打开时，同步到选中的页
    LaunchedEffect(selectedPage) {
        displayPage = selectedPage
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .width(320.dp)
                .wrapContentHeight(),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
            tonalElevation = 0.dp,
            shadowElevation = 0.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 标题
                Text(
                    text = if (viewMode == 0) "选择年月" else "选择年份",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(20.dp))

                // 切换区域
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            if (viewMode == 0) {
                                onYearChange(selectedYear - 1)
                            } else {
                                if (displayPage > 0) {
                                    displayPage -= 1
                                }
                            }
                        },
                        enabled = true
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "上一个",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    Text(
                        text = if (viewMode == 0) {
                            String.format("%04d年%02d月", selectedYear, selectedMonth)
                        } else {
                            val rangeStart = startYear + displayPage * yearsPerPage
                            val rangeEnd = minOf(rangeStart + yearsPerPage - 1, currentYear)
                            "$rangeStart-$rangeEnd"
                        },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    IconButton(
                        onClick = {
                            if (viewMode == 0) {
                                onYearChange(selectedYear + 1)
                            } else {
                                if (displayPage < totalPages - 1) {
                                    displayPage += 1
                                }
                            }
                        },
                        enabled = true
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "下一个",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 网格选择区
                if (viewMode == 0) {
                    // 月份选择: 2行6列
                    MonthGrid(
                        selectedMonth = selectedMonth,
                        onMonthSelect = { month ->
                            onMonthChange(month)
                            onDismiss()
                        }
                    )
                } else {
                    // 年份选择: 每页10个，5个一行，起始2005年
                    YearGrid(
                        selectedYear = selectedYear,
                        displayPage = displayPage,
                        onYearSelect = { year ->
                            onYearChange(year)
                            onDismiss()
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 取消按钮
                TextButton(
                    onClick = onDismiss
                ) {
                    Text(
                        text = "取消",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun MonthGrid(
    selectedMonth: Int,
    onMonthSelect: (Int) -> Unit
) {
    val months = (1..12).toList()
    val rows = months.chunked(6)

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        rows.forEach { rowMonths ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowMonths.forEach { month ->
                    MonthItem(
                        month = month,
                        isSelected = month == selectedMonth,
                        onClick = { onMonthSelect(month) },
                        modifier = Modifier.weight(1f)
                    )
                }
                // 填充空白格子
                repeat(6 - rowMonths.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun MonthItem(
    month: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    val scale by animateFloatAsState(
        targetValue = if (isHovered) 1.08f else 1f,
        animationSpec = tween(durationMillis = 200),
        label = "scale"
    )

    val alpha by animateFloatAsState(
        targetValue = if (isHovered) 1f else 0.7f,
        animationSpec = tween(durationMillis = 200),
        label = "alpha"
    )

    val monthNames = listOf(
        "1月", "2月", "3月", "4月", "5月", "6月",
        "7月", "8月", "9月", "10月", "11月", "12月"
    )

    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        Color.Transparent
    }

    val textColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = alpha)
    }

    Box(
        modifier = modifier
            .height(40.dp)
            .scale(scale)
            .then(
                if (isSelected) {
                    Modifier.shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(12.dp),
                        ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                        spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                    )
                } else Modifier
            )
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = monthNames[month - 1],
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = textColor
        )
    }
}

@Composable
private fun YearGrid(
    selectedYear: Int,
    displayPage: Int,
    onYearSelect: (Int) -> Unit
) {
    val startYear = 2005
    val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
    val yearsPerPage = 10
    val yearsPerRow = 5

    val totalYears = currentYear - startYear + 1
    val totalPages = if (totalYears > 0) (totalYears + yearsPerPage - 1) / yearsPerPage else 1

    val displayYears = (0 until yearsPerPage).map { index ->
        startYear + displayPage * yearsPerPage + index
    }.filter { it <= currentYear }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        displayYears.chunked(yearsPerRow).forEach { rowYears ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                rowYears.forEach { year ->
                    YearItem(
                        year = year,
                        isSelected = year == selectedYear,
                        onClick = { onYearSelect(year) },
                        modifier = Modifier.weight(1f)
                    )
                }
                repeat(yearsPerRow - rowYears.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }

    // 分页指示器
    if (totalPages > 1) {
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(totalPages) { page ->
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(
                            if (page == displayPage) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                        )
                )
                if (page < totalPages - 1) {
                    Spacer(modifier = Modifier.width(4.dp))
                }
            }
        }
    }
}

@Composable
private fun YearItem(
    year: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    val scale by animateFloatAsState(
        targetValue = if (isHovered) 1.08f else 1f,
        animationSpec = tween(durationMillis = 200),
        label = "scale"
    )

    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        Color.Transparent
    }

    val textColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = modifier
            .height(44.dp)
            .scale(scale)
            .then(
                if (isSelected) {
                    Modifier.shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(12.dp),
                        ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                        spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                    )
                } else Modifier
            )
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "${year}",
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = textColor
        )
    }
}