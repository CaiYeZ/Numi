package com.herb.numi.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/**
 * 交易类型选择器（支持滑动的 Tabs）
 * 收入/支出两个Tab，支持下划线指示器和平滑切换动画
 * 滑动区域覆盖整个内容区域，在种类展示区滑动可以切换Tab
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RecordTransactionTypeSelector(
    selectedType: String,
    onTypeChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (page: Int) -> Unit
) {
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(
        initialPage = if (selectedType == "expense") 0 else 1,
        pageCount = { 2 }
    )

    LaunchedEffect(pagerState.currentPage) {
        val newType = if (pagerState.currentPage == 0) "expense" else "income"
        if (newType != selectedType) {
            onTypeChange(newType)
        }
    }

    LaunchedEffect(selectedType) {
        val targetPage = if (selectedType == "expense") 0 else 1
        if (pagerState.currentPage != targetPage) {
            pagerState.animateScrollToPage(targetPage)
        }
    }

    Column(modifier = modifier) {
        TransactionTypeTabs(
            pagerState = pagerState,
            scope = scope
        )
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            content(page)
        }
    }
}

/**
 * 交易类型标签行
 */
@Composable
private fun TransactionTypeTabs(
    pagerState: PagerState,
    scope: kotlinx.coroutines.CoroutineScope
) {
    val indicatorColor: Color by animateColorAsState(
        targetValue = if (pagerState.currentPage == 0) Color(0xFFF44336) else Color(0xFF4CAF50),
        animationSpec = tween(durationMillis = 300),
        label = "indicator_color"
    )

    TabRow(
        selectedTabIndex = pagerState.currentPage,
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onSurface,
        indicator = { tabPositions ->
            val pageOffset = pagerState.currentPageOffsetFraction
            val currentIndex = pagerState.currentPage
            
            Box(
                Modifier
                    .fillMaxWidth()
                    .wrapContentSize(androidx.compose.ui.Alignment.BottomCenter)
                    .fillMaxWidth()
            ) {
                if (currentIndex < tabPositions.size - 1) {
                    val currentTab = tabPositions[currentIndex]
                    val nextTab = tabPositions[currentIndex + 1]
                    val widthFraction = 0.4f
                    val startOffset = currentTab.left + (currentTab.width * (1 - widthFraction) / 2)
                    val endOffset = nextTab.left + (nextTab.width * (1 - widthFraction) / 2)
                    val currentOffset = startOffset + (endOffset - startOffset) * pageOffset
                    
                    Box(
                        Modifier
                            .offset(x = currentOffset)
                            .width(currentTab.width * widthFraction)
                            .height(3.dp)
                            .background(
                                color = indicatorColor,
                                shape = RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp)
                            )
                    )
                } else if (currentIndex < tabPositions.size) {
                    val currentTab = tabPositions[currentIndex]
                    val widthFraction = 0.4f
                    Box(
                        Modifier
                            .offset(x = currentTab.left + (currentTab.width * (1 - widthFraction) / 2))
                            .width(currentTab.width * widthFraction)
                            .height(3.dp)
                            .background(
                                color = indicatorColor,
                                shape = RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp)
                            )
                    )
                }
            }
        },
        divider = {}
    ) {
        val expenseColor: Color by animateColorAsState(
            targetValue = if (pagerState.currentPage == 0) Color(0xFFF44336)
                          else MaterialTheme.colorScheme.onSurfaceVariant,
            animationSpec = tween(durationMillis = 300),
            label = "expense_color"
        )
        val incomeColor: Color by animateColorAsState(
            targetValue = if (pagerState.currentPage == 1) Color(0xFF4CAF50)
                          else MaterialTheme.colorScheme.onSurfaceVariant,
            animationSpec = tween(durationMillis = 300),
            label = "income_color"
        )

        TransactionTab(
            text = "支出",
            selected = pagerState.currentPage == 0,
            onClick = {
                scope.launch {
                    pagerState.animateScrollToPage(0)
                }
            },
            color = expenseColor
        )

        TransactionTab(
            text = "收入",
            selected = pagerState.currentPage == 1,
            onClick = {
                scope.launch {
                    pagerState.animateScrollToPage(1)
                }
            },
            color = incomeColor
        )
    }
}

/**
 * 单个交易类型标签
 */
@Composable
private fun TransactionTab(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    color: Color
) {
    Tab(
        selected = selected,
        onClick = onClick,
        text = {
            Text(
                text = text,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = color
            )
        }
    )
}
