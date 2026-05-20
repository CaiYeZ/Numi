package com.herb.numi.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.herb.numi.data.Record
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

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
//                        leadingIcon = {
//                            Icon(Icons.Default.KeyboardArrowDown, null, modifier = Modifier.size(16.dp))
//                        }
                    )
                    FilterChip(
                        selected = selectedChartType == "income",
                        onClick = { onChartTypeChange("income") },
                        label = { Text("收入", fontSize = 12.sp) },
//                        leadingIcon = {
//                            Icon(Icons.Default.KeyboardArrowUp, null, modifier = Modifier.size(16.dp))
//                        }
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
 * 包含饼图和分类列表
 */
@Composable
private fun CategoryReportContent(
    categoryData: Map<String, Double>,
    modifier: Modifier = Modifier
) {
    val total = categoryData.values.sum()

    val sortedData = categoryData.entries.sortedByDescending { it.value }

    val primaryColor = MaterialTheme.colorScheme.primary

    val maxAmount = sortedData.firstOrNull()?.value ?: 1.0
    val minAmount = sortedData.lastOrNull()?.value ?: 0.0

    val colors = sortedData.map { entry ->
        calculateColorForAmount(
            amount = entry.value,
            maxAmount = maxAmount,
            minAmount = minAmount,
            baseColor = primaryColor
        )
    }

    val percentages = sortedData.map { entry ->
        (entry.value / total * 100)
    }

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        CategoryPieChartWithAnnotations(
            sortedData = sortedData,
            colors = colors,
            percentages = percentages,
            total = total,
            modifier = Modifier.size(220.dp)
        )
    }
}

/**
 * 带批注的饼图组件
 * 从圆心向外延伸标注线，标注线末端显示分类名称和百分比
 */
@Composable
private fun CategoryPieChartWithAnnotations(
    sortedData: List<Map.Entry<String, Double>>,
    colors: List<Color>,
    percentages: List<Double>,
    total: Double,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val textPaint = android.graphics.Paint().apply {
        color = android.graphics.Color.GRAY
        textAlign = android.graphics.Paint.Align.LEFT
        isAntiAlias = true
    }

    Canvas(modifier = modifier) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val radius = minOf(centerX, centerY) * 0.65f

        var startAngle = -90f
        sortedData.forEachIndexed { index, entry ->
            val sweepAngle = (entry.value / total * 360).toFloat()
            val midAngle = Math.toRadians((startAngle + sweepAngle / 2).toDouble())

            drawArc(
                color = colors[index],
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = true,
                topLeft = Offset(centerX - radius, centerY - radius),
                size = Size(radius * 2, radius * 2)
            )

            val lineEndX = centerX + (radius * 1.3f) * cos(midAngle).toFloat()
            val lineEndY = centerY + (radius * 1.3f) * sin(midAngle).toFloat()

            drawLine(
                color = colors[index].copy(alpha = 0.6f),
                start = Offset(centerX, centerY),
                end = Offset(lineEndX, lineEndY),
                strokeWidth = 1.5f
            )

            // 绘制批注文本
            val labelText = "${sortedData[index].key} ${String.format("%.1f", percentages[index])}%"
            textPaint.textSize = with(density) { 12.sp.toPx() }
            textPaint.color = android.graphics.Color.GRAY

            val textX = if (lineEndX > centerX) lineEndX + 4 else lineEndX - textPaint.measureText(labelText) - 4
            val textY = lineEndY + textPaint.textSize / 3

            drawContext.canvas.nativeCanvas.drawText(labelText, textX, textY, textPaint)

            startAngle += sweepAngle
        }

        drawCircle(
            color = Color.White,
            radius = radius * 0.5f,
            center = Offset(centerX, centerY)
        )
    }
}

/**
 * 饼图组件（备用/简化版）
 */
@Composable
private fun CategoryPieChart(
    sortedData: List<Map.Entry<String, Double>>,
    colors: List<Color>,
    total: Double,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        var startAngle = -90f
        sortedData.forEachIndexed { index, entry ->
            val sweepAngle = (entry.value / total * 360).toFloat()
            drawArc(
                color = colors[index],
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = true,
                size = Size(size.width, size.height)
            )
            startAngle += sweepAngle
        }
    }
}

/**
 * 根据金额计算颜色深浅
 * 金额越大颜色越深，金额越小颜色越浅
 */
private fun calculateColorForAmount(
    amount: Double,
    maxAmount: Double,
    minAmount: Double,
    baseColor: Color
): Color {
    if (maxAmount == minAmount) return baseColor

    val ratio = ((amount - minAmount) / (maxAmount - minAmount)).toFloat().coerceIn(0f, 1f)

    val lightestColor = baseColor.copy(alpha = 0.15f)

    return lerpColor(lightestColor, baseColor, ratio)
}

/**
 * 颜色线性插值
 */
private fun lerpColor(start: Color, end: Color, fraction: Float): Color {
    return Color(
        red = start.red + (end.red - start.red) * fraction,
        green = start.green + (end.green - start.green) * fraction,
        blue = start.blue + (end.blue - start.blue) * fraction,
        alpha = start.alpha + (end.alpha - start.alpha) * fraction
    )
}
