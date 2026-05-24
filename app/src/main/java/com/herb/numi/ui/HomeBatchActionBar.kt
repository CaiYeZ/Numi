package com.herb.numi.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * 批量操作栏组件
 * 固定在页面底部，提供退出、全选、删除功能
 */
@Composable
fun HomeBatchActionBar(
    selectedCount: Int,
    totalCount: Int,
    onCancel: () -> Unit,
    onSelectAll: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LeftSection(
                selectedCount = selectedCount,
                totalCount = totalCount,
                onCancel = onCancel,
                onSelectAll = onSelectAll
            )
            RightSection(
                selectedCount = selectedCount,
                onDelete = onDelete
            )
        }
    }
}

/**
 * 左侧区域：退出图标 + 全选按钮
 */
@Composable
private fun LeftSection(
    selectedCount: Int,
    totalCount: Int,
    onCancel: () -> Unit,
    onSelectAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CancelButton(onClick = onCancel)
        SelectAllButton(
            selectedCount = selectedCount,
            totalCount = totalCount,
            onClick = onSelectAll
        )
    }
}

/**
 * 取消按钮
 */
@Composable
private fun CancelButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "退出批量选择",
            tint = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * 全选按钮（带计数器）
 */
@Composable
private fun SelectAllButton(
    selectedCount: Int,
    totalCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TextButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Text(
            text = if (selectedCount == totalCount && totalCount > 0) "取消全选($selectedCount)" else "全选($selectedCount)",
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * 右侧区域：删除按钮
 */
@Composable
private fun RightSection(
    selectedCount: Int,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    TextButton(
        onClick = onDelete,
        enabled = selectedCount > 0,
        colors = ButtonDefaults.textButtonColors(
            contentColor = MaterialTheme.colorScheme.error,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
        ),
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = "删除选中",
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "删除",
            fontWeight = FontWeight.Medium
        )
    }
}
