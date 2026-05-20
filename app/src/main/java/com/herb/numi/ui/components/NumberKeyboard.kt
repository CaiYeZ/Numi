package com.herb.numi.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 自定义数字键盘组件
 * 用于快速输入金额，彻底解决系统软键盘遮挡问题
 *
 * @param onNumberClick 数字键点击回调
 * @param onDeleteClick 删除键点击回调
 * @param onConfirmClick 确认键点击回调
 */
@Composable
fun NumberKeyboard(
    onNumberClick: (String) -> Unit,
    onDeleteClick: () -> Unit,
    onConfirmClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 第一行：1, 2, 3
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            NumberKey("1", onClick = { onNumberClick("1") }, modifier = Modifier.weight(1f))
            NumberKey("2", onClick = { onNumberClick("2") }, modifier = Modifier.weight(1f))
            NumberKey("3", onClick = { onNumberClick("3") }, modifier = Modifier.weight(1f))
        }

        // 第二行：4, 5, 6
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            NumberKey("4", onClick = { onNumberClick("4") }, modifier = Modifier.weight(1f))
            NumberKey("5", onClick = { onNumberClick("5") }, modifier = Modifier.weight(1f))
            NumberKey("6", onClick = { onNumberClick("6") }, modifier = Modifier.weight(1f))
        }

        // 第三行：7, 8, 9
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            NumberKey("7", onClick = { onNumberClick("7") }, modifier = Modifier.weight(1f))
            NumberKey("8", onClick = { onNumberClick("8") }, modifier = Modifier.weight(1f))
            NumberKey("9", onClick = { onNumberClick("9") }, modifier = Modifier.weight(1f))
        }

        // 第四行：., 0, 删除
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            NumberKey(".", onClick = { onNumberClick(".") }, modifier = Modifier.weight(1f))
            NumberKey("0", onClick = { onNumberClick("0") }, modifier = Modifier.weight(1f))
            ActionKey("删除", onClick = onDeleteClick, modifier = Modifier.weight(1f))
        }

        // 第五行：完成按钮
        ConfirmKey(onClick = onConfirmClick, modifier = Modifier.fillMaxWidth())
    }
}

/**
 * 数字键组件
 */
@Composable
private fun NumberKey(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 24.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * 操作键组件（删除）
 */
@Composable
private fun ActionKey(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

/**
 * 确认键组件
 */
@Composable
private fun ConfirmKey(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.primary)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "完成",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}
