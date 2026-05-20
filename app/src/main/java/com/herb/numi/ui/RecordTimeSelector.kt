package com.herb.numi.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

/**
 * 交易时间选择区域（简化格式：今天 14:30）
 */
@Composable
fun RecordTimeSelector(
    selectedTime: Calendar,
    onShowTimePicker: () -> Unit,
    modifier: Modifier = Modifier
) {
    val now = Calendar.getInstance()
    val isToday = selectedTime.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
            selectedTime.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR)

    val timeText = if (isToday) {
        "今天 ${SimpleDateFormat("HH:mm", Locale.getDefault()).format(selectedTime.time)}"
    } else {
        SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()).format(selectedTime.time)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onShowTimePicker)
            .padding(vertical = 8.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = timeText,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium
        )
    }
}
