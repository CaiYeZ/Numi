package com.herb.numi.ui.common

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 带加载状态的按钮组件
 * 在加载时显示进度指示器并禁用点击
 *
 * @param text 按钮文本
 * @param onClick 点击回调
 * @param isLoading 是否处于加载状态
 * @param enabled 是否启用（加载时自动禁用）
 * @param modifier 修饰符
 * @param containerColor 按钮背景色
 * @param contentColor 按钮内容色
 * @param textColor 文本颜色（优先级高于 contentColor）
 */
@Composable
fun LoadingTextButton(
    text: String,
    onClick: () -> Unit,
    isLoading: Boolean = false,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    textColor: Color? = null
) {
    val throttler = rememberClickThrottler()
    val isClickable = enabled && !isLoading

    Surface(
        onClick = {
            throttler.tryClick(onClick)
        },
        enabled = isClickable,
        shape = RoundedCornerShape(20.dp),
        color = if (isClickable) containerColor else containerColor.copy(alpha = 0.5f),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = contentColor
                )
            } else {
                Text(
                    text = text,
                    fontSize = 15.sp,
                    color = textColor ?: contentColor
                )
            }
        }
    }
}

/**
 * 带加载状态的文本按钮（用于 AlertDialog 内）
 *
 * @param text 按钮文本
 * @param onClick 点击回调
 * @param isLoading 是否处于加载状态
 * @param enabled 是否启用
 * @param contentColor 内容颜色
 */
@Composable
fun LoadingDialogButton(
    text: String,
    onClick: () -> Unit,
    isLoading: Boolean = false,
    enabled: Boolean = true,
    contentColor: Color = MaterialTheme.colorScheme.primary
) {
    val throttler = rememberClickThrottler()
    val isClickable = enabled && !isLoading

    TextButton(
        onClick = {
            throttler.tryClick(onClick)
        },
        enabled = isClickable,
        colors = ButtonDefaults.textButtonColors(
            contentColor = if (isClickable) contentColor else contentColor.copy(alpha = 0.38f)
        )
    ) {
        if (isLoading) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(14.dp),
                    strokeWidth = 2.dp,
                    color = contentColor
                )
                Text(text)
            }
        } else {
            Text(text)
        }
    }
}
