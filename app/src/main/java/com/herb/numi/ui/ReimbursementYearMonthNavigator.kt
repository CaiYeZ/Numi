package com.herb.numi.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ReimbursementYearMonthNavigator(
    year: Int,
    month: Int,
    viewMode: Int,
    availableYears: List<Int>,
    isSelectorExpanded: Boolean,
    onSelectorExpandedChange: (Boolean) -> Unit,
    onYearChange: (Int) -> Unit,
    onMonthChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var showYearMonthPicker by remember { mutableStateOf(false) }

    if (showYearMonthPicker) {
        YearMonthPicker(
            selectedYear = year,
            selectedMonth = month,
            viewMode = viewMode,
            availableYears = availableYears,
            onYearChange = onYearChange,
            onMonthChange = onMonthChange,
            onDismiss = { showYearMonthPicker = false }
        )
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = {
                onSelectorExpandedChange(false)
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
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "上一个",
                modifier = Modifier.size(24.dp)
            )
        }

        val displayText = if (viewMode == 0) {
            String.format("%04d-%02d", year, month)
        } else {
            "${year}年"
        }
        Box {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .clickable { showYearMonthPicker = true },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = displayText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "选择年月",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        IconButton(
            onClick = {
                onSelectorExpandedChange(false)
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
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "下一个",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
