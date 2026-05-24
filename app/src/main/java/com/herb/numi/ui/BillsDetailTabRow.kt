package com.herb.numi.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 账单明细选项卡区域
 * 包含"结余"、"支出"、"收入"、"待报销"、"已报销"五个选项卡，显示数量和金额
 */
@Composable
fun BillsDetailTabRow(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    balanceCount: Int,
    expenseCount: Int,
    incomeCount: Int,
    pendingCount: Int,
    reimbursedCount: Int,
    balanceAmount: Double,
    expenseAmount: Double,
    incomeAmount: Double,
    pendingAmount: Double,
    reimbursedAmount: Double,
    modifier: Modifier = Modifier
) {
    val tabs = listOf(
        TabInfo("结余", balanceCount, balanceAmount),
        TabInfo("支出", expenseCount, expenseAmount),
        TabInfo("收入", incomeCount, incomeAmount),
        TabInfo("待报销", pendingCount, pendingAmount),
        TabInfo("已报销", reimbursedCount, reimbursedAmount)
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        tabs.forEachIndexed { index, tab ->
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
                    .clip(RoundedCornerShape(12.dp))
                    .background(backgroundColor)
                    .border(
                        androidx.compose.foundation.BorderStroke(1.5.dp, borderColor),
                        RoundedCornerShape(12.dp)
                    )
                    .clickable { onTabSelected(index) }
                    .padding(vertical = 4.dp, horizontal = 6.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "${tab.label}(${tab.count})",
                    fontSize = 10.sp,
                    fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                    color = if (isSelected) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = String.format("%.0f", tab.amount),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
            }
        }
    }
}

private data class TabInfo(
    val label: String,
    val count: Int,
    val amount: Double
)