package com.herb.numi.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocalConvenienceStore
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.herb.numi.data.Record
import com.herb.numi.data.ReimburseStatus
import kotlinx.coroutines.launch
import kotlin.math.max
import java.text.SimpleDateFormat
import java.util.*

/**
 * 报销统计界面
 * 展示非报销和已报销记录的统计信息和明细列表
 *
 * @param viewModel 记账 ViewModel
 * @param onNavigateBack 返回回调
 * @param onNavigateToRecord 跳转记账回调，参数为预设的报销状态
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReimbursementScreen(
    viewModel: RecordViewModel = viewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToRecord: (String) -> Unit
) {
    val allRecords by viewModel.allRecords.collectAsState()

    // 当前选中的年月，默认当前年月
    val calendar = Calendar.getInstance()
    var selectedYear by remember { mutableIntStateOf(calendar.get(Calendar.YEAR)) }
    var selectedMonth by remember { mutableIntStateOf(calendar.get(Calendar.MONTH) + 1) }

    // 视图模式：0=月视图(按天分组), 1=年视图(按月分组)
    var viewMode by remember { mutableIntStateOf(0) }

    // 选项卡状态：0=非报销, 1=已报销
    var selectedTab by remember { mutableIntStateOf(0) }

    // 详情弹窗状态
    var showDetailSheet by remember { mutableStateOf(false) }
    var selectedRecord by remember { mutableStateOf<Record?>(null) }

    // 根据年月筛选记录
    val filteredRecords = remember(allRecords, selectedYear, selectedMonth, viewMode) {
        if (viewMode == 0) {
            // 月视图：筛选当月
            val startCal = Calendar.getInstance().apply {
                set(selectedYear, selectedMonth - 1, 1, 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val endCal = Calendar.getInstance().apply {
                set(selectedYear, selectedMonth - 1, 1, 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
                add(Calendar.MONTH, 1)
            }
            allRecords.filter {
                it.createdAt >= startCal.timeInMillis && it.createdAt < endCal.timeInMillis
            }
        } else {
            // 年视图：筛选当年
            val startCal = Calendar.getInstance().apply {
                set(selectedYear, 0, 1, 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val endCal = Calendar.getInstance().apply {
                set(selectedYear, 0, 1, 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
                add(Calendar.YEAR, 1)
            }
            allRecords.filter {
                it.createdAt >= startCal.timeInMillis && it.createdAt < endCal.timeInMillis
            }
        }
    }

    // 待报销和已报销记录
    val pendingRecords = filteredRecords.filter { it.reimburseStatus == ReimburseStatus.PENDING.value }
    val reimbursedRecords = filteredRecords.filter { it.reimburseStatus == ReimburseStatus.REIMBURSED.value }

    // 待报销和已报销金额
    val pendingAmount = pendingRecords.sumOf { it.amount }
    val reimbursedAmount = reimbursedRecords.sumOf { it.amount }

    // Snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    val status = if (selectedTab == 0) {
                        ReimburseStatus.PENDING.value
                    } else {
                        ReimburseStatus.REIMBURSED.value
                    }
                    onNavigateToRecord(status)
                },
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier.size(56.dp),
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 0.dp
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "记账",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    ) { paddingValues ->
        val topInset = paddingValues.calculateTopPadding()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .consumeTopInset(topInset)
                .padding(bottom = paddingValues.calculateBottomPadding())
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(
                        start = 16.dp,
                        end = 16.dp,
                        top = TopContentSpacing + 8.dp,
                        bottom = 8.dp
                    )
            ) {
                // 顶栏：返回 + 月/年切换 + 筛选
                ReimbursementTopBar(
                    viewMode = viewMode,
                    onViewModeChange = { viewMode = it },
                    onNavigateBack = onNavigateBack
                )

                Spacer(modifier = Modifier.height(4.dp))

                // 年月导航
                YearMonthNavigator(
                    year = selectedYear,
                    month = selectedMonth,
                    viewMode = viewMode,
                    onYearChange = { selectedYear = it },
                    onMonthChange = { selectedMonth = it }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            val currentRecords = if (selectedTab == 0) pendingRecords else reimbursedRecords

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp) // 列表项间距
            ) {
                item {
                    ReimbursementTabRow(
                        selectedTab = selectedTab,
                        onTabSelected = { selectedTab = it },
                        pendingCount = pendingRecords.size,
                        reimbursedCount = reimbursedRecords.size,
                        pendingAmount = pendingAmount,
                        reimbursedAmount = reimbursedAmount
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(4.dp)) // 顶部间距
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "账单明细",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        IconButton(onClick = { /* 批量选择功能 */ }) {
                            Icon(
                                imageVector = Icons.Default.Checklist,
                                contentDescription = "批量选择",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(4.dp))
                }

                if (currentRecords.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (selectedTab == 0) "无待报销记录" else "无已报销记录",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                } else if (viewMode == 0) {
                    // 月视图：按天分组
                    val groupedRecords = currentRecords.groupBy {
                        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it.createdAt))
                    }.mapValues { (_, records) ->
                        records.sortedByDescending { it.createdAt }
                    }.toSortedMap(compareByDescending { it })

                    groupedRecords.forEach { (dateKey, records) ->
                        item(key = "reimburse_day_$dateKey") {
                            ReimbursementDayCard(
                                dateKey = dateKey,
                                records = records,
                                onRecordClick = { record ->
                                    selectedRecord = record
                                    showDetailSheet = true
                                }
                            )
                        }
                    }
                } else {
                    // 年视图：按月分组
                    val groupedRecords = currentRecords.groupBy {
                        SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date(it.createdAt))
                    }.mapValues { (_, records) ->
                        records.sortedByDescending { it.createdAt }
                    }.toSortedMap(compareByDescending { it })

                    groupedRecords.forEach { (monthKey, records) ->
                        item(key = "reimburse_month_$monthKey") {
                            ReimbursementMonthCard(
                                monthKey = monthKey,
                                records = records,
                                onRecordClick = { record ->
                                    selectedRecord = record
                                    showDetailSheet = true
                                }
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(72.dp))
                }
            }
        }
    }

    // 详情弹窗
    if (showDetailSheet && selectedRecord != null) {
        RecordDetailBottomSheet(
            record = selectedRecord!!,
            onDismiss = {
                showDetailSheet = false
                selectedRecord = null
            },
            onEdit = {
                showDetailSheet = false
                selectedRecord = null
            },
            onDelete = {
                showDetailSheet = false
                selectedRecord = null
            }
        )
    }
}

/**
 * 抵消父级传入的顶部内边距，让当前布局可以贴到状态栏起点绘制背景
 */
private fun Modifier.consumeTopInset(topInset: androidx.compose.ui.unit.Dp): Modifier = this.then(
    Modifier.layout { measurable, constraints ->
        val insetPx = topInset.roundToPx()
        val placeable = measurable.measure(constraints)
        val height = max(0, placeable.height - insetPx)
        layout(placeable.width, height) {
            placeable.placeRelative(0, -insetPx)
        }
    }
)

/**
 * 分类图标（圆形主题色背景）
 */
@Composable
private fun ReimbursementCategoryIcon(
    category: String,
    modifier: Modifier = Modifier
) {
    val icon = getCategoryIconForName(category)

    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = category,
            modifier = Modifier.size(20.dp),
            tint = Color.White
        )
    }
}

/**
 * 根据分类名称获取对应图标向量
 */
private fun getCategoryIconForName(category: String): androidx.compose.ui.graphics.vector.ImageVector {
    val expenseIcon = com.herb.numi.data.ExpenseCategory.icons[category]
    val incomeIcon = com.herb.numi.data.IncomeCategory.icons[category]
    val icon = expenseIcon ?: incomeIcon ?: com.herb.numi.data.CategoryIcon.OTHER

    return when (icon) {
        com.herb.numi.data.CategoryIcon.RESTAURANT -> Icons.Filled.Restaurant
        com.herb.numi.data.CategoryIcon.TRANSPORT -> Icons.Filled.DirectionsCar
        com.herb.numi.data.CategoryIcon.SHOPPING -> Icons.Filled.ShoppingBag
        com.herb.numi.data.CategoryIcon.ENTERTAINMENT -> Icons.Filled.SportsEsports
        com.herb.numi.data.CategoryIcon.DAILY -> Icons.Filled.LocalConvenienceStore
        com.herb.numi.data.CategoryIcon.SALARY -> Icons.Filled.AccountBalance
        com.herb.numi.data.CategoryIcon.LIVING -> Icons.Filled.Home
        com.herb.numi.data.CategoryIcon.ALLOWANCE -> Icons.Filled.CardGiftcard
        com.herb.numi.data.CategoryIcon.TRANSFER -> Icons.Filled.SwapHoriz
        com.herb.numi.data.CategoryIcon.OTHER -> Icons.Filled.MoreHoriz
    }
}

/**
 * 顶栏：返回按钮 + 月/年切换器 + 筛选图标
 */
@Composable
private fun ReimbursementTopBar(
    viewMode: Int,
    onViewModeChange: (Int) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 返回按钮
        IconButton(onClick = onNavigateBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "返回",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        // 月/年切换器
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ) {
            Row(
                modifier = Modifier.padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf("月" to 0, "年" to 1).forEach { (label, mode) ->
                    val isSelected = viewMode == mode
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.surface
                                else Color.Transparent
                            )
                            .clickable { onViewModeChange(mode) }
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 14.sp,
                            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.onSurface
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // 筛选图标
        IconButton(onClick = { /* 筛选功能 */ }) {
            Icon(
                imageVector = Icons.Default.FilterList,
                contentDescription = "筛选",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * 年月导航：左箭头 + 年月 + 下拉 + 右箭头
 */
@Composable
private fun YearMonthNavigator(
    year: Int,
    month: Int,
    viewMode: Int,
    onYearChange: (Int) -> Unit,
    onMonthChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 左箭头
        IconButton(
            onClick = {
                if (viewMode == 0) {
                    if (month > 1) {
                        onMonthChange(month - 1)
                    } else {
                        onMonthChange(12)
                        onYearChange(year - 1)
                    }
                } else {
                    onYearChange(year - 1)
                }
            }
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowLeft,
                contentDescription = "上一个",
                modifier = Modifier.size(24.dp)
            )
        }

        // 年月显示
        val displayText = if (viewMode == 0) {
            String.format("%04d-%02d", year, month)
        } else {
            "${year}年"
        }
        Text(
            text = displayText,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Icon(
            imageVector = Icons.Default.KeyboardArrowDown,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // 右箭头
        IconButton(
            onClick = {
                if (viewMode == 0) {
                    if (month < 12) {
                        onMonthChange(month + 1)
                    } else {
                        onMonthChange(1)
                        onYearChange(year + 1)
                    }
                } else {
                    onYearChange(year + 1)
                }
            }
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "下一个",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

/**
 * 报销统计选项卡区域
 * 包含非报销和已报销两个选项卡，白色圆角矩形背景，选中时主题色描边
 */
@Composable
private fun ReimbursementTabRow(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    pendingCount: Int,
    reimbursedCount: Int,
    pendingAmount: Double,
    reimbursedAmount: Double,
    modifier: Modifier = Modifier
) {
    val tabs = listOf(
        Triple("待报销", pendingCount, pendingAmount),
        Triple("已报销", reimbursedCount, reimbursedAmount)
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        tabs.forEachIndexed { index, (label, count, amount) ->
            val isSelected = selectedTab == index
            val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(
                        BorderStroke(1.5.dp, borderColor),
                        RoundedCornerShape(12.dp)
                    )
                    .clickable { onTabSelected(index) }
                    .padding(vertical = 16.dp, horizontal = 12.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "$label($count)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = String.format("%.2f", amount),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

/**
 * 报销统计日期分组卡片（按天）
 * 参考图片2实现，支持展开/折叠
 */
@Composable
private fun ReimbursementDayCard(
    dateKey: String,
    records: List<Record>,
    onRecordClick: (Record) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(true) }
    val totalAmount = records.sumOf { it.amount }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column {
            // 日期头部（可点击展开/折叠）
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 日期标签
                    val cal = Calendar.getInstance().apply {
                        time = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateKey)!!
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${cal.get(Calendar.MONTH) + 1}月",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${cal.get(Calendar.YEAR)}",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Column {
                        Text(
                            text = formatGroupDate(dateKey),
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
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (expanded) "折叠" else "展开",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 记录列表（可展开/折叠）
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
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
                            onClick = { onRecordClick(record) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * 报销统计月份分组卡片（按月）
 */
@Composable
private fun ReimbursementMonthCard(
    monthKey: String,
    records: List<Record>,
    onRecordClick: (Record) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(true) }
    val totalAmount = records.sumOf { it.amount }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column {
            // 月份头部（可点击展开/折叠）
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 月份标签
                    val parts = monthKey.split("-")
                    val yearStr = parts[0]
                    val monthStr = parts[1]
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${monthStr.toInt()}月",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = yearStr,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (expanded) "折叠" else "展开",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 记录列表（可展开/折叠）
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
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
                            onClick = { onRecordClick(record) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * 报销记录项
 * 参考图片2实现：左侧图标 + 分类/备注/时间 + 金额/状态
 */
@Composable
private fun ReimbursementRecordItem(
    record: Record,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val status = ReimburseStatus.fromValue(record.reimburseStatus)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左侧：图标 + 分类/备注/时间
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 分类图标（圆形主题色背景）
                ReimbursementCategoryIcon(category = record.category)

                Column {
                    // 第一行：分类 + 备注
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
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
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                    }
                    // 第二行：时间 + 自己
                    Spacer(modifier = Modifier.height(2.dp))
                    val timeStr = remember(record.createdAt) {
                        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(record.createdAt))
                    }
                    Text(
                        text = "$timeStr 自己",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 右侧：金额 + 状态
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "${if (record.type == "expense") "-" else "+"}${String.format("%.2f", record.amount)}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (record.type == "expense") MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.primary
                )
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
