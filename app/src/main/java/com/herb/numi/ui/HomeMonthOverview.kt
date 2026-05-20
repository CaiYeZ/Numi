package com.herb.numi.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 首页本月概览卡片
 * 显示本月支出、收入、结余的关键数据
 */
@Composable
fun HomeMonthOverviewCard(
    monthExpense: Double,
    monthIncome: Double,
    modifier: Modifier = Modifier
) {
    val balance = monthIncome - monthExpense

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            MonthExpenseSection(monthExpense = monthExpense)
            Spacer(modifier = Modifier.height(16.dp))
            MonthSummarySection(monthIncome = monthIncome, balance = balance)
        }
    }
}

/**
 * 本月支出区块（重点显示）
 */
@Composable
private fun MonthExpenseSection(
    monthExpense: Double,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.Start,
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = "本月支出",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = String.format("%.2f", monthExpense),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * 本月收入和结余区块（紧凑排版）
 */
@Composable
private fun MonthSummarySection(
    monthIncome: Double,
    balance: Double,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "本月收入 ${String.format("%.2f", monthIncome)} | 本月结余 ${String.format("%.2f", balance)}",
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
