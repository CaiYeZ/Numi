package com.herb.numi.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/**
 * 导航页面枚举
 * 定义应用页面路由
 */
enum class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    Home("home", "首页", Icons.Default.Home),
    Bills("bills", "账单", Icons.Default.List),
    Record("record", "记账", Icons.Default.Home),
    Reimbursement("reimbursement", "报销统计", Icons.Default.Home),
    Settings("settings", "设置", Icons.Default.Settings);

    companion object {
        /**
         * 获取底部导航项列表（顺序：账单 - 首页 - 设置）
         */
        val navigationItems = listOf(Bills, Home, Settings)
    }
}

/**
 * 悬浮式药丸导航栏组件
 *
 * 采用药丸型设计风格，白色圆角背景悬浮在底部
 * 选中项显示灰色药丸形背景
 * 右侧附带蓝色圆形FAB用于记账
 *
 * @param currentRoute 当前选中的路由
 * @param onNavigate 导航回调
 * @param onNavigateToRecord 记账导航回调
 */
@Composable
fun BottomNavigationBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onNavigateToRecord: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 20.dp), // 与底栏的距离
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 药丸型导航栏容器
            Surface(
//                modifier = Modifier.fillMaxWidth(0.8f),
                modifier = Modifier.width(270.dp),
                shape = RoundedCornerShape(32.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 0.dp, // 阴影
                tonalElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Screen.navigationItems.forEach { screen ->
                        val isSelected = currentRoute == screen.route

                        // 导航项容器，选中时显示灰色药丸背景
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(32.dp)) // 圆角
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.surfaceContainerHighest else Color.Transparent
                                )
                                .clickable {
                                    if (currentRoute != screen.route) {
                                        onNavigate(screen.route)
                                    }
                                }
                                .padding(horizontal = 28.dp, vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.title,
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // 右侧蓝色圆形FAB
            FloatingActionButton(
                onClick = onNavigateToRecord,
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier.size(56.dp),
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 0.dp, // 默认阴影
                    pressedElevation = 0.dp // 按下阴影
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "记账",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}