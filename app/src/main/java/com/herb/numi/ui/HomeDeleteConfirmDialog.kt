package com.herb.numi.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import com.herb.numi.data.Record
import com.herb.numi.ui.common.LoadingDialogButton
import java.text.SimpleDateFormat
import java.util.*

/**
 * 单条记录删除确认对话框（基础版）
 *
 * @param onConfirm 确认删除回调
 * @param onDismiss 取消/关闭回调
 * @param isLoading 是否处于加载状态（删除进行中）
 */
@Composable
fun DeleteConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isLoading: Boolean = false
) {
    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        title = {
            Text(
                text = "确认删除",
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Text(
                text = "确定要删除这条记录吗？此操作不可恢复。",
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        confirmButton = {
            LoadingDialogButton(
                text = "删除",
                onClick = onConfirm,
                isLoading = isLoading,
                enabled = !isLoading,
                contentColor = MaterialTheme.colorScheme.error
            )
        },
        dismissButton = {
            LoadingDialogButton(
                text = "取消",
                onClick = onDismiss,
                isLoading = false,
                enabled = !isLoading
            )
        }
    )
}

/**
 * 单条记录删除确认对话框（显示记录信息版）
 * 推荐优先使用此版本，可明确展示待删除记录的信息
 *
 * @param record 待删除的记录
 * @param onConfirm 确认删除回调
 * @param onDismiss 取消/关闭回调
 * @param isLoading 是否处于加载状态（删除进行中）
 */
@Composable
fun SingleDeleteConfirmDialog(
    record: Record,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isLoading: Boolean = false
) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    val recordInfo = "${record.category} · ${if (record.type == "expense") "-" else "+"}¥${String.format("%.2f", record.amount)}"

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        title = {
            Text(
                text = "确认删除",
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Text(
                text = "确定要删除这条记录吗？\n\n$recordInfo\n此操作不可恢复。",
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        confirmButton = {
            LoadingDialogButton(
                text = "删除",
                onClick = onConfirm,
                isLoading = isLoading,
                enabled = !isLoading,
                contentColor = MaterialTheme.colorScheme.error
            )
        },
        dismissButton = {
            LoadingDialogButton(
                text = "取消",
                onClick = onDismiss,
                isLoading = false,
                enabled = !isLoading
            )
        }
    )
}

/**
 * 批量删除确认对话框
 *
 * @param selectedCount 选中的记录数量
 * @param onConfirm 确认删除回调
 * @param onDismiss 取消/关闭回调
 * @param isLoading 是否处于加载状态（删除进行中）
 */
@Composable
fun BatchDeleteConfirmDialog(
    selectedCount: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isLoading: Boolean = false
) {
    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        title = {
            Text(
                text = "确认删除",
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Text(
                text = "确定要删除选中的 $selectedCount 条记录吗？此操作不可恢复。",
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        confirmButton = {
            LoadingDialogButton(
                text = "删除",
                onClick = onConfirm,
                isLoading = isLoading,
                enabled = !isLoading,
                contentColor = MaterialTheme.colorScheme.error
            )
        },
        dismissButton = {
            LoadingDialogButton(
                text = "取消",
                onClick = onDismiss,
                isLoading = false,
                enabled = !isLoading
            )
        }
    )
}
