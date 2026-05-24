package com.herb.numi.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.herb.numi.data.Record
import java.util.*
import kotlin.math.abs

/**
 * 分类报表卡片
 * 显示支出/收入的分类统计和饼图
 */
@Composable
fun CategoryReportCard(
    records: List<Record>,
    timePeriod: TimePeriod,
    selectedDate: Calendar,
    selectedChartType: String,
    onChartTypeChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val filteredRecords = records.filter { record ->
        val recordCalendar = Calendar.getInstance().apply { timeInMillis = record.createdAt }
        if (timePeriod == TimePeriod.MONTH) {
            recordCalendar.get(Calendar.YEAR) == selectedDate.get(Calendar.YEAR) &&
            recordCalendar.get(Calendar.MONTH) == selectedDate.get(Calendar.MONTH)
        } else {
            recordCalendar.get(Calendar.YEAR) == selectedDate.get(Calendar.YEAR)
        }
    }

    val categoryData = filteredRecords
        .filter { it.type == selectedChartType }
        .groupBy { it.category }
        .mapValues { it.value.sumOf { record -> record.amount } }
        .filter { it.value > 0 }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("分类报表", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = selectedChartType == "expense",
                        onClick = { onChartTypeChange("expense") },
                        label = { Text("支出", fontSize = 12.sp) },
                    )
                    FilterChip(
                        selected = selectedChartType == "income",
                        onClick = { onChartTypeChange("income") },
                        label = { Text("收入", fontSize = 12.sp) },
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (categoryData.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("暂无数据", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                CategoryReportContent(categoryData = categoryData)
            }
        }
    }
}

/**
 * 分类报表内容
 * 包含环形图和分类图例列表
 */
@Composable
private fun CategoryReportContent(
    categoryData: Map<String, Double>,
    modifier: Modifier = Modifier
) {
    val total = categoryData.values.sum()

    // 大数据量适配：当分类数量 > 8 时，合并小分类到"其他"
    val processedData = processCategoryData(categoryData)

    val sortedData = processedData.entries.sortedByDescending { it.value }

    val primaryColor = MaterialTheme.colorScheme.primary

    val maxAmount = sortedData.firstOrNull()?.value ?: 1.0
    val minAmount = sortedData.lastOrNull()?.value ?: 0.0

    val colors = generateDistinctColors(sortedData.size)

    val percentages = sortedData.map { entry ->
        (entry.value / total * 100)
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 环形图
        DonutChart(
            sortedData = sortedData,
            colors = colors,
            total = total,
            modifier = Modifier.size(180.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 图例列表
        CategoryLegend(
            sortedData = sortedData,
            colors = colors,
            percentages = percentages,
            total = total,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 200.dp)
                .verticalScroll(rememberScrollState())
        )
    }
}

/**
 * 环形图组件
 * 中间显示总金额数值
 */
@Composable
private fun DonutChart(
    sortedData: List<Map.Entry<String, Double>>,
    colors: List<Color>,
    total: Double,
    modifier: Modifier = Modifier
) {
    val centerTextColor = MaterialTheme.colorScheme.onSurface

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerX = size.width / 2
            val centerY = size.height / 2
            val radius = minOf(centerX, centerY) * 0.9f
            val strokeWidth = radius * 0.35f

            var startAngle = -90f
            sortedData.forEachIndexed { index, entry ->
                val sweepAngle = (entry.value / total * 360).toFloat()

                drawArc(
                    color = colors[index],
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = Offset(centerX - radius, centerY - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                )

                startAngle += sweepAngle
            }
        }

        // 中间只显示总金额数值
        Text(
            text = "¥${String.format("%.0f", total)}",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = centerTextColor,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * 分类图例列表
 * 显示分类名称、颜色块、金额和百分比
 */
@Composable
private fun CategoryLegend(
    sortedData: List<Map.Entry<String, Double>>,
    colors: List<Color>,
    percentages: List<Double>,
    total: Double,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        sortedData.forEachIndexed { index, entry ->
            CategoryLegendItem(
                category = entry.key,
                color = colors[index],
                amount = entry.value,
                percentage = percentages[index],
                total = total
            )
        }
    }
}

/**
 * 单个图例项
 */
@Composable
private fun CategoryLegendItem(
    category: String,
    color: Color,
    amount: Double,
    percentage: Double,
    total: Double,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            // 颜色块
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(color)
            )

            // 分类名称
            Text(
                text = category,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 金额
            Text(
                text = "¥${String.format("%.2f", amount)}",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )

            // 百分比
            Text(
                text = "${String.format("%.1f", percentage)}%",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(45.dp),
                textAlign = TextAlign.End
            )
        }
    }
}

/**
 * 大数据量适配
 * 当分类数量 > 8 时，将占比最小的分类合并到"其他"
 */
private fun processCategoryData(categoryData: Map<String, Double>): Map<String, Double> {
    if (categoryData.size <= 8) {
        return categoryData
    }

    val sortedEntries = categoryData.entries.sortedByDescending { it.value }
    val topEntries = sortedEntries.take(7)
    val otherAmount = sortedEntries.drop(7).sumOf { it.value }

    val result = topEntries.associate { it.key to it.value }.toMutableMap()
    if (otherAmount > 0) {
        result["其他"] = otherAmount
    }

    return result
}

/**
 * 生成一组视觉区分度高的颜色
 * 使用 HSL 色彩空间，确保颜色之间有足够的色相差异
 * 避免使用主题色及其近似色
 */
private fun generateDistinctColors(count: Int): List<Color> {
    if (count <= 0) return emptyList()

    // 预定义一组高区分度的颜色（避免使用 Material 主题蓝/靛蓝）
    val baseColors = listOf(
        Color(0xFFE53935), // 红
        Color(0xFF43A047), // 绿
        Color(0xFFFB8C00), // 橙
        Color(0xFF8E24AA), // 紫
        Color(0xFF00ACC1), // 青
        Color(0xFFFDD835), // 黄
        Color(0xFF6D4C41), // 棕
        Color(0xFFEC407A), // 粉红
        Color(0xFF3949AB), // 深蓝
        Color(0xFF00897B), // 青绿
        Color(0xFFFF7043), // 珊瑚
        Color(0xFF5E35B1), // 深紫
        Color(0xFF039BE5), // 天蓝
        Color(0xFF7CB342), // 浅绿
        Color(0xFFD81B60), // 玫红
    )

    return (0 until count).map { index ->
        baseColors[index % baseColors.size]
    }
}
