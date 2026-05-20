package com.herb.numi.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 金额输入区域和数字键盘
 * 布局参考图片：浅灰色背景，白色按键，红色保存按钮
 */
@Composable
fun RecordNumberKeyboard(
    amount: String,
    isEditingMode: Boolean = false,
    onNumberClick: (String) -> Unit,
    onDeleteClick: () -> Unit,
    onSaveClick: () -> Unit,
    onReRecordClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    NumberKeyboardLayout(
        isEditingMode = isEditingMode,
        onNumberClick = onNumberClick,
        onDeleteClick = onDeleteClick,
        onSaveClick = onSaveClick,
        onReRecordClick = onReRecordClick,
        modifier = modifier
    )
}

/**
 * 数字键盘布局
 * 参考图片布局：
 * - 第一行：1 2 3 删除
 * - 第二行：4 5 6 再记
 * - 第三行：7 8 9 [保存上半部分]
 * - 第四行：0 . [保存下半部分]
 * 保存按钮跨第3-4行右侧位置（2*1规格）
 * 浅色主题：浅灰背景，白色按键，黑色文字，红色保存按钮
 * 深色主题：深灰背景，深灰按键，白色文字，橙色保存按钮
 */
@Composable
private fun NumberKeyboardLayout(
    isEditingMode: Boolean,
    onNumberClick: (String) -> Unit,
    onDeleteClick: () -> Unit,
    onSaveClick: () -> Unit,
    onReRecordClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 根据主题设置配色方案
    val keyboardBackground = MaterialTheme.colorScheme.surfaceContainerLow
    val keyBackground = MaterialTheme.colorScheme.surface
    val textColor = MaterialTheme.colorScheme.onSurface
    val saveButtonColor = MaterialTheme.colorScheme.primary

    val keyHeight = 56.dp
    val keySpacing = 8.dp

    // 保存按钮高度 = 两行高度 + 间距
    val saveButtonHeight = keyHeight * 2 + keySpacing

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(keyboardBackground)
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(keySpacing)
    ) {
        // 第一行：1 2 3 删除
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(keySpacing)
        ) {
            NumberButton(
                text = "1",
                onClick = { onNumberClick("1") },
                modifier = Modifier.weight(1f),
                keyHeight = keyHeight,
                backgroundColor = keyBackground,
                contentColor = textColor
            )
            NumberButton(
                text = "2",
                onClick = { onNumberClick("2") },
                modifier = Modifier.weight(1f),
                keyHeight = keyHeight,
                backgroundColor = keyBackground,
                contentColor = textColor
            )
            NumberButton(
                text = "3",
                onClick = { onNumberClick("3") },
                modifier = Modifier.weight(1f),
                keyHeight = keyHeight,
                backgroundColor = keyBackground,
                contentColor = textColor
            )
            DeleteButton(
                onClick = onDeleteClick,
                modifier = Modifier.weight(1f),
                keyHeight = keyHeight,
                backgroundColor = keyBackground,
                contentColor = textColor
            )
        }

        // 第二行：4 5 6 再记/取消
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(keySpacing)
        ) {
            NumberButton(
                text = "4",
                onClick = { onNumberClick("4") },
                modifier = Modifier.weight(1f),
                keyHeight = keyHeight,
                backgroundColor = keyBackground,
                contentColor = textColor
            )
            NumberButton(
                text = "5",
                onClick = { onNumberClick("5") },
                modifier = Modifier.weight(1f),
                keyHeight = keyHeight,
                backgroundColor = keyBackground,
                contentColor = textColor
            )
            NumberButton(
                text = "6",
                onClick = { onNumberClick("6") },
                modifier = Modifier.weight(1f),
                keyHeight = keyHeight,
                backgroundColor = keyBackground,
                contentColor = textColor
            )
            if (isEditingMode) {
                TextButton(
                    text = "取消",
                    onClick = onReRecordClick,
                    modifier = Modifier.weight(1f),
                    keyHeight = keyHeight,
                    backgroundColor = keyBackground,
                    contentColor = textColor
                )
            } else {
                TextButton(
                    text = "再记",
                    onClick = onReRecordClick,
                    modifier = Modifier.weight(1f),
                    keyHeight = keyHeight,
                    backgroundColor = keyBackground,
                    contentColor = textColor
                )
            }
        }

        // 第三行和第四行合并：7 8 9 / 0 . 保存（2*1）
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(keySpacing)
        ) {
            // 左侧3x2数字区域
            Column(
                modifier = Modifier.weight(3f),
                verticalArrangement = Arrangement.spacedBy(keySpacing)
            ) {
                // 第三行：7 8 9
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(keySpacing)
                ) {
                    NumberButton(
                        text = "7",
                        onClick = { onNumberClick("7") },
                        modifier = Modifier.weight(1f),
                        keyHeight = keyHeight,
                        backgroundColor = keyBackground,
                        contentColor = textColor
                    )
                    NumberButton(
                        text = "8",
                        onClick = { onNumberClick("8") },
                        modifier = Modifier.weight(1f),
                        keyHeight = keyHeight,
                        backgroundColor = keyBackground,
                        contentColor = textColor
                    )
                    NumberButton(
                        text = "9",
                        onClick = { onNumberClick("9") },
                        modifier = Modifier.weight(1f),
                        keyHeight = keyHeight,
                        backgroundColor = keyBackground,
                        contentColor = textColor
                    )
                }

                // 第四行：0 .
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(keySpacing)
                ) {
                    // 占位保持3列对齐
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(keyHeight)
                    )
                    NumberButton(
                        text = "0",
                        onClick = { onNumberClick("0") },
                        modifier = Modifier.weight(1f),
                        keyHeight = keyHeight,
                        backgroundColor = keyBackground,
                        contentColor = textColor
                    )
                    NumberButton(
                        text = ".",
                        onClick = { onNumberClick(".") },
                        modifier = Modifier.weight(1f),
                        keyHeight = keyHeight,
                        backgroundColor = keyBackground,
                        contentColor = textColor
                    )
                }
            }

            // 右侧保存按钮（2*1高度）
            SaveButton2x1(
                onClick = onSaveClick,
                modifier = Modifier.weight(1f),
                height = saveButtonHeight,
                backgroundColor = saveButtonColor
            )
        }
    }
}

/**
 * 数字按钮
 */
@Composable
private fun NumberButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    keyHeight: Dp = 56.dp,
    backgroundColor: Color = Color.White,
    contentColor: Color = Color.Black
) {
    Box(
        modifier = modifier
            .height(keyHeight)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = contentColor
        )
    }
}

/**
 * 删除按钮
 */
@Composable
private fun DeleteButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    keyHeight: Dp = 56.dp,
    backgroundColor: Color = Color.White,
    contentColor: Color = Color.Black
) {
    Box(
        modifier = modifier
            .height(keyHeight)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "删除",
            tint = contentColor,
            modifier = Modifier.size(24.dp)
        )
    }
}

/**
 * 文字按钮（再记/取消）
 */
@Composable
private fun TextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    keyHeight: Dp = 56.dp,
    backgroundColor: Color = Color.White,
    contentColor: Color = Color.Black
) {
    Box(
        modifier = modifier
            .height(keyHeight)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = contentColor
        )
    }
}

/**
 * 保存按钮（2*1高度）
 */
@Composable
private fun SaveButton2x1(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 120.dp,
    backgroundColor: Color = Color(0xFFFF5252)
) {
    Box(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "保存",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}
