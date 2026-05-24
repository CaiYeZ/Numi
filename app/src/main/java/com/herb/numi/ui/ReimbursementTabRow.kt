package com.herb.numi.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 报销统计选项卡区域
 * 包含"待报销"和"已报销"两个选项卡，显示数量和金额
 */
@Composable
fun ReimbursementTabRow(
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
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        tabs.forEachIndexed { index, (label, count, amount) ->
            val isSelected = selectedTab == index
            val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
            val backgroundColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
            } else {
                MaterialTheme.colorScheme.surface
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(backgroundColor)
                    .border(
                        androidx.compose.foundation.BorderStroke(1.5.dp, borderColor),
                        RoundedCornerShape(16.dp)
                    )
                    .clickable { onTabSelected(index) }
                    .padding(vertical = 5.dp, horizontal = 12.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "$label($count)",
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                    color = if (isSelected) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = String.format("%.2f", amount),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
