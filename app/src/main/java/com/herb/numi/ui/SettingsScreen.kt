package com.herb.numi.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.herb.numi.data.Record

/**
 * 设置页面
 * 整合应用设置和数据管理功能，包括导出/导入账单和清空数据
 */
@Composable
fun SettingsScreen(
    viewModel: RecordViewModel = viewModel(),
    onExportBills: (List<Record>) -> Unit,
    onImportBills: () -> Unit,
    themeMode: String = "system",
    onThemeChange: (String) -> Unit = {}
) {
    val allRecords by viewModel.allRecords.collectAsState()
    val customCategories by viewModel.customCategories.collectAsState()
    val monthExpense by viewModel.monthExpense.collectAsState()
    val monthIncome by viewModel.monthIncome.collectAsState()

    // 清空数据确认对话框状态
    var showClearDataDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(TopContentSpacing)
            .padding(horizontal = 16.dp),
    ) {
        Text(
            text = "设置",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 数据管理
        Text(
            text = "数据管理",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 导出账单
        DataManagementItem(
            icon = Icons.Default.Download,
            title = "导出账单",
            description = if (allRecords.isNotEmpty()) "将账单和分类数据导出为 CSV 格式" else "暂无账单数据可导出",
            isEnabled = allRecords.isNotEmpty(),
            onClick = { onExportBills(allRecords) }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 导入账单
        DataManagementItem(
            icon = Icons.Default.Upload,
            title = "导入账单",
            description = "从 CSV 文件导入账单和分类数据",
            isEnabled = true,
            onClick = onImportBills
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 清空数据
        DataManagementItem(
            icon = Icons.Default.DeleteForever,
            title = "清空数据",
            description = if (allRecords.isNotEmpty()) "删除所有账单记录和自定义分类" else "暂无数据可清空",
            isEnabled = allRecords.isNotEmpty(),
            onClick = { showClearDataDialog = true }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 主题设置
        Text(
            text = "主题设置",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(12.dp))

        ThemeSelector(
            themeMode = themeMode,
            onThemeChange = onThemeChange
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 统计信息
        Text(
            text = "统计信息",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatItem(label = "总记录数", value = "${allRecords.size} 条")
                StatItem(label = "自定义分类", value = "${customCategories.size} 个")
                StatItem(label = "本月支出", value = "¥${String.format("%.2f", monthExpense)}")
                StatItem(label = "本月收入", value = "¥${String.format("%.2f", monthIncome)}")
                StatItem(
                    label = "本月结余",
                    value = "¥${String.format("%.2f", monthIncome - monthExpense)}"
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 关于
        Text(
            text = "关于",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                InfoItem(label = "应用名称", value = "数笔")
                InfoItem(label = "版本", value = "1.1.6")
                InfoItem(label = "开发者", value = "Herb")
            }
        }
    }

    // 清空数据确认对话框
    if (showClearDataDialog) {
        ClearDataConfirmDialog(
            onConfirm = {
                viewModel.clearAllData()
                showClearDataDialog = false
            },
            onDismiss = {
                showClearDataDialog = false
            }
        )
    }
}

/**
 * 数据管理项组件
 * 通用的数据操作卡片，包含图标、标题和描述
 */
@Composable
private fun DataManagementItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    isEnabled: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = isEnabled) { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isEnabled) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isEnabled) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                },
                modifier = Modifier.size(24.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = if (isEnabled) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    }
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * 清空数据确认对话框
 * 二次确认防止误操作
 */
@Composable
private fun ClearDataConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "确认清空数据")
        },
        text = {
            Text(text = "此操作将删除所有账单记录和自定义分类，且不可恢复。确定要继续吗？")
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("确认清空", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

/**
 * 主题选择器
 * 提供浅色、深色、跟随系统三个选项
 */
@Composable
private fun ThemeSelector(
    themeMode: String,
    onThemeChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ThemeOption(
            label = "浅色",
            isSelected = themeMode == "light",
            onClick = { onThemeChange("light") },
            modifier = Modifier.weight(1f)
        )
        ThemeOption(
            label = "深色",
            isSelected = themeMode == "dark",
            onClick = { onThemeChange("dark") },
            modifier = Modifier.weight(1f)
        )
        ThemeOption(
            label = "跟随系统",
            isSelected = themeMode == "system",
            onClick = { onThemeChange("system") },
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * 主题选项卡片
 */
@Composable
private fun ThemeOption(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface

    Card(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * 统计项
 */
@Composable
private fun StatItem(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * 信息项
 */
@Composable
private fun InfoItem(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}
