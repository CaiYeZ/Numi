package com.herb.numi

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.herb.numi.data.Record
import com.herb.numi.data.export.BillExportManager
import com.herb.numi.data.export.ExportResult
import com.herb.numi.ui.BillsScreen
import com.herb.numi.ui.HomeScreen
import com.herb.numi.ui.RecordScreen
import com.herb.numi.ui.RecordViewModel
import com.herb.numi.ui.SettingsScreen
import com.herb.numi.ui.navigation.BottomNavigationBar
import com.herb.numi.ui.navigation.Screen
import com.herb.numi.ui.theme.NumiTheme
import kotlinx.coroutines.launch

/**
 * 主活动
 * 应用入口，包含底部导航栏和页面路由
 */
class MainActivity : ComponentActivity() {

    private lateinit var billExportManager: BillExportManager

    /**
     * 待导出的记录列表缓存
     * 用于在SAF选择器返回后执行导出
     */
    private var pendingExportRecords: List<Record>? = null

    /**
     * 系统文件保存器（SAF）回调
     * 用户选择保存位置后触发实际导出
     */
    private val createDocumentLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri: Uri? ->
        val records = pendingExportRecords
        pendingExportRecords = null

        if (uri == null || records == null) {
            return@registerForActivityResult
        }

        lifecycleScope.launch {
            val result = billExportManager.exportToCsv(
                records = records,
                uri = uri,
                customFileName = billExportManager.generateDefaultFileName()
            )

            when (result) {
                is ExportResult.Success -> {
                    Toast.makeText(
                        this@MainActivity,
                        "成功导出 ${result.recordCount} 条账单到 ${result.fileName}",
                        Toast.LENGTH_LONG
                    ).show()
                }
                is ExportResult.Error -> {
                    Toast.makeText(
                        this@MainActivity,
                        result.message,
                        Toast.LENGTH_LONG
                    ).show()
                }
                is ExportResult.Empty -> {
                    Toast.makeText(
                        this@MainActivity,
                        "暂无账单数据可导出",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        billExportManager = BillExportManager(this)

        setContent {
            val viewModel: RecordViewModel = viewModel()
            val themeMode by viewModel.themeMode.collectAsState()

            val isDarkTheme = when (themeMode) {
                "light" -> false
                "dark" -> true
                else -> null
            }

            NumiTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainApp(viewModel = viewModel)
                }
            }
        }
    }

    /**
     * 主应用组件
     * 包含底部导航栏、首页FAB和页面路由
     */
    @OptIn(ExperimentalAnimationApi::class)
    @Composable
    private fun MainApp(viewModel: RecordViewModel) {
        // 默认显示首页
        var currentRoute by remember { mutableStateOf(Screen.Home.route) }
        // 标记是否正在记账（隐藏底部导航）
        var isRecording by remember { mutableStateOf(false) }
        // 记录前一个非记账页面的路由，用于判断动画方向
        var previousRoute by remember { mutableStateOf(currentRoute) }

        // 根据是否在记账状态决定是否显示底部导航
        val showBottomBar = !isRecording

        // 处理返回手势：在记账界面时，返回到 HomeScreen 而不是退出应用
        BackHandler(enabled = isRecording) {
            isRecording = false
        }

        Scaffold(
            bottomBar = {
                if (showBottomBar) {
                    BottomNavigationBar(
                        currentRoute = currentRoute,
                        onNavigate = { route ->
                            previousRoute = currentRoute
                            currentRoute = route
                        },
                        onNavigateToRecord = {
                            viewModel.resetInput()
                            viewModel.cancelEdit()
                            isRecording = true
                        }
                    )
                }
            }
        ) { paddingValues ->
            // 使用 AnimatedContent 实现平滑的页面切换
            AnimatedContent(
                targetState = if (isRecording) Screen.Record.route else currentRoute,
                transitionSpec = {
                    // 判断是否为记账页面
                    val isEnteringRecord = targetState == Screen.Record.route
                    val isFromRecord = initialState == Screen.Record.route

                    // 根据进入和离开的页面确定动画方向
                    val direction = when {
                        isEnteringRecord -> 1 // 进入记账：从右向左滑入
                        isFromRecord -> -1    // 离开记账：从左向右滑出
                        else -> {
                            // 普通页面切换：根据路由顺序判断方向
                            val targetIndex = Screen.navigationItems.indexOf(
                                Screen.navigationItems.find { it.route == targetState }
                            )
                            val initialIndex = Screen.navigationItems.indexOf(
                                Screen.navigationItems.find { it.route == initialState }
                            )
                            if (targetIndex >= initialIndex) 1 else -1
                        }
                    }

                    // 使用贝塞尔曲线实现平滑的缓动动画
                    val easing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)

                    if (isEnteringRecord) {
                        // 进入记账页面：从右侧滑入
                        slideInHorizontally(
                            initialOffsetX = { it },
                            animationSpec = tween(
                                durationMillis = 350,
                                easing = easing
                            )
                        ) + fadeIn(
                            animationSpec = tween(
                                durationMillis = 350,
                                easing = easing
                            )
                        ) togetherWith
                        slideOutHorizontally(
                            targetOffsetX = { -it / 3 },
                            animationSpec = tween(
                                durationMillis = 350,
                                easing = easing
                            )
                        ) + fadeOut(
                            animationSpec = tween(
                                durationMillis = 350,
                                easing = easing
                            )
                        )
                    } else if (isFromRecord) {
                        // 从记账页面返回：向右侧滑出
                        slideInHorizontally(
                            initialOffsetX = { -it / 3 },
                            animationSpec = tween(
                                durationMillis = 350,
                                easing = easing
                            )
                        ) + fadeIn(
                            animationSpec = tween(
                                durationMillis = 350,
                                easing = easing
                            )
                        ) togetherWith
                        slideOutHorizontally(
                            targetOffsetX = { it },
                            animationSpec = tween(
                                durationMillis = 350,
                                easing = easing
                            )
                        ) + fadeOut(
                            animationSpec = tween(
                                durationMillis = 350,
                                easing = easing
                            )
                        )
                    } else {
                        // 普通页面切换：左右滑动
                        (if (direction > 0) {
                            // 向右滑动（进入右侧页面）
                            slideInHorizontally(
                                initialOffsetX = { it },
                                animationSpec = tween(
                                    durationMillis = 300,
                                    easing = easing
                                )
                            ) + fadeIn(
                                animationSpec = tween(
                                    durationMillis = 300,
                                    easing = easing
                                )
                            )
                        } else {
                            // 向左滑动（进入左侧页面）
                            slideInHorizontally(
                                initialOffsetX = { -it },
                                animationSpec = tween(
                                    durationMillis = 300,
                                    easing = easing
                                )
                            ) + fadeIn(
                                animationSpec = tween(
                                    durationMillis = 300,
                                    easing = easing
                                )
                            )
                        }) togetherWith
                        (if (direction > 0) {
                            slideOutHorizontally(
                                targetOffsetX = { -it },
                                animationSpec = tween(
                                    durationMillis = 300,
                                    easing = easing
                                )
                            ) + fadeOut(
                                animationSpec = tween(
                                    durationMillis = 300,
                                    easing = easing
                                )
                            )
                        } else {
                            slideOutHorizontally(
                                targetOffsetX = { it },
                                animationSpec = tween(
                                    durationMillis = 300,
                                    easing = easing
                                )
                            ) + fadeOut(
                                animationSpec = tween(
                                    durationMillis = 300,
                                    easing = easing
                                )
                            )
                        })
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                label = "page_transition"
            ) { targetRoute ->
                when (targetRoute) {
                    Screen.Home.route -> HomeScreen(
                        viewModel = viewModel,
                        onNavigateToRecord = {
                            viewModel.resetInput()
                            viewModel.cancelEdit()
                            isRecording = true
                        },
                        onEditRecord = { record ->
                            viewModel.loadRecordForEdit(record)
                            isRecording = true
                        }
                    )
                    Screen.Bills.route -> BillsScreen(
                        viewModel = viewModel,
                        onNavigateToRecord = {
                            viewModel.resetInput()
                            viewModel.cancelEdit()
                            isRecording = true
                        },
                        onEditRecord = { record ->
                            viewModel.loadRecordForEdit(record)
                            isRecording = true
                        }
                    )
                    Screen.Record.route -> RecordScreen(
                        viewModel = viewModel,
                        onRecordingComplete = {
                            isRecording = false
                        },
                        onNavigateBack = {
                            isRecording = false
                        }
                    )
                    Screen.Settings.route -> SettingsScreen(
                        viewModel = viewModel,
                        onExportBills = { records -> launchExportFlow(records) },
                        themeMode = viewModel.themeMode.value,
                        onThemeChange = { viewModel.setThemeMode(it) }
                    )
                }
            }
        }
    }

    /**
     * 启动导出流程
     * 1. 校验数据非空
     * 2. 打开SAF文件保存器让用户选择保存位置
     * 3. 在回调中执行实际导出
     *
     * @param records 待导出的账单记录列表
     */
    private fun launchExportFlow(records: List<Record>) {
        if (records.isEmpty()) {
            Toast.makeText(this, "暂无账单数据可导出", Toast.LENGTH_SHORT).show()
            return
        }

        pendingExportRecords = records

        val defaultFileName = billExportManager.generateDefaultFileName()
        createDocumentLauncher.launch(defaultFileName)
    }
}
