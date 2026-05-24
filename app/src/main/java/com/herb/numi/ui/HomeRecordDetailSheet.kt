package com.herb.numi.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.herb.numi.data.Record
import com.herb.numi.data.ReimburseStatus
import com.herb.numi.ui.common.LoadingTextButton
import com.herb.numi.ui.common.rememberClickThrottler
import java.text.SimpleDateFormat
import java.util.*

/**
 * 记录详情底部弹窗
 * 样式：surface背景 + surfaceVariant背景的详情区域
 *
 * 功能特性：
 * - 显示记录完整信息（金额、分类、时间、报销状态、备注）
 * - 支持修改和删除操作，带防重复点击机制
 * - 加载状态显示，操作进行中按钮禁用并显示进度指示器
 * - 适配不同屏幕尺寸，底部安全区域处理
 *
 * @param record 要显示详情的记录
 * @param onDismiss 关闭弹窗回调
 * @param onEdit 点击修改回调
 * @param onDelete 点击删除回调
 * @param isLoading 是否处于加载状态（操作进行中）
 * @param modifier 修饰符
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordDetailBottomSheet(
    record: Record,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    val modalBottomSheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = { if (!isLoading) onDismiss() },
        sheetState = modalBottomSheetState,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = null,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 0.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            DetailSheetHeader(
                onEdit = onEdit,
                onDelete = onDelete,
                isLoading = isLoading
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    DetailAmountRow(record = record)
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.1f))
                    DetailCategoryRow(category = record.category)
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.1f))
                    DetailTimeRow(record = record)
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.1f))
                    DetailReimburseStatusRow(reimburseStatus = record.reimburseStatus)
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.1f))
                    DetailNoteRow(note = record.note)
                }
            }
        }
    }
}

/**
 * 详情弹窗头部（标题 + 操作按钮）
 *
 * @param onEdit 点击修改回调
 * @param onDelete 点击删除回调
 * @param isLoading 是否处于加载状态
 * @param modifier 修饰符
 */
@Composable
private fun DetailSheetHeader(
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "详细",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LoadingTextButton(
                text = "修改",
                onClick = onEdit,
                isLoading = false,
                enabled = !isLoading,
                textColor = MaterialTheme.colorScheme.onSurface
            )
            LoadingTextButton(
                text = "删除",
                onClick = onDelete,
                isLoading = false,
                enabled = !isLoading,
                textColor = MaterialTheme.colorScheme.error
            )
        }
    }
}

/**
 * 金额行
 *
 * @param record 记录数据
 * @param modifier 修饰符
 */
@Composable
private fun DetailAmountRow(
    record: Record,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "金额",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "${if (record.type == "expense") "-" else "+"}¥${String.format("%.2f", record.amount)}",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (record.type == "expense") Color(0xFFF44336) else Color(0xFF4CAF50)
        )
    }
}

/**
 * 分类行
 *
 * @param category 分类名称
 * @param modifier 修饰符
 */
@Composable
private fun DetailCategoryRow(
    category: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "分类",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = category,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * 时间行
 * 第一行显示最新更新时间，第二行显示创建时间
 * 若更新时间等于创建时间，则不显示第二行
 *
 * @param record 记录数据
 * @param modifier 修饰符
 */
@Composable
private fun DetailTimeRow(
    record: Record,
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    val updatedTime = dateFormat.format(Date(record.updatedAt))
    val createdTime = dateFormat.format(Date(record.createdAt))
    val hasBeenUpdated = record.updatedAt != record.createdAt

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "时间",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = updatedTime,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (hasBeenUpdated) {
                Text(
                    text = "记录于$createdTime",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

/**
 * 报销状态行
 *
 * @param reimburseStatus 报销状态值
 * @param modifier 修饰符
 */
@Composable
private fun DetailReimburseStatusRow(
    reimburseStatus: String,
    modifier: Modifier = Modifier
) {
    val status = ReimburseStatus.fromValue(reimburseStatus)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "报销",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = status.label,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.End
        )
    }
}

/**
 * 备注行
 *
 * @param note 备注内容
 * @param modifier 修饰符
 */
@Composable
private fun DetailNoteRow(
    note: String?,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "备注",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = note ?: "",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.End
        )
    }
}
