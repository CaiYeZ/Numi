package com.herb.numi.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

/**
 * 将 yyyy-MM-dd 格式化为 MM/dd E（如 05/18 周一）
 */
internal fun formatGroupDate(dateKey: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MM/dd E", Locale.CHINA)
        val date = inputFormat.parse(dateKey)
        date?.let { outputFormat.format(it) } ?: dateKey
    } catch (_: Exception) {
        dateKey
    }
}
