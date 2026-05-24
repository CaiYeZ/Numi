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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.herb.numi.data.ReimburseStatus
import com.herb.numi.data.Record as NumiRecord
import com.herb.numi.data.`import`.CsvImporter
import com.herb.numi.data.`import`.ImportResult
import com.herb.numi.data.export.BillExportManager
import com.herb.numi.data.export.ExportResult
import com.herb.numi.ui.BillsDetailScreen
import com.herb.numi.ui.BillsScreen
import com.herb.numi.ui.HomeScreen
import com.herb.numi.ui.RecordScreen
import com.herb.numi.ui.RecordViewModel
import com.herb.numi.ui.RecordViewModelFactory
import com.herb.numi.ui.ReimbursementScreen
import com.herb.numi.ui.SettingsScreen
import com.herb.numi.ui.navigation.BottomNavigationBar
import com.herb.numi.ui.navigation.Screen
import com.herb.numi.ui.pageTransitionSpec
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
    private var pendingExportRecords: List<NumiRecord>? = null

    /**
     * 待导出的自定义分类列表缓存
     */
    private var pendingExportCategories: List<com.herb.numi.data.CustomCategory>? = null

    /**
     * 系统文件保存器（SAF）回调
     * 用户选择保存位置后触发实际导出
     */
    private val createDocumentLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri: Uri? ->
        val records = pendingExportRecords
        val categories = pendingExportCategories
        pendingExportRecords = null
        pendingExportCategories = null

        if (uri == null || records == null) {
            return@registerForActivityResult
        }

        lifecycleScope.launch {
            val result = billExportManager.exportToCsvWithCategories(
                records = records,
                customCategories = categories ?: emptyList(),
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

    /**
     * 系统文件选择器（SAF）回调
     * 用户选择CSV文件后触发导入
     */
    private val openDocumentLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri == null) return@registerForActivityResult

        lifecycleScope.launch {
            try {
                val csvContent = contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                    ?: return@launch

                val numiApp = application as NumiApplication
                val viewModelFactory = RecordViewModelFactory(numiApp, numiApp.repository, numiApp.customCategoryRepository)
                val viewModel = androidx.lifecycle.ViewModelProvider(this@MainActivity, viewModelFactory)[RecordViewModel::class.java]

                lifecycleScope.launch {
                    val result = viewModel.importFromCsv(csvContent)

                    when (result) {
                        is ImportResult.Success -> {
                            val msg = buildString {
                                if (result.duplicateCount > 0) {
                                    append("导入完成：新增 ${result.newRecordCount} 条记录")
                                    append("，跳过 ${result.duplicateCount} 条重复记录")
                                } else {
                                    append("成功导入 ${result.newRecordCount} 条记录")
                                }
                                if (result.categoryCount > 0) {
                                    append("和 ${result.categoryCount} 个分类")
                                }
                            }
                            Toast.makeText(this@MainActivity, msg, Toast.LENGTH_LONG).show()
                        }
                        is ImportResult.Partial -> {
                            val msg = buildString {
                                append("部分导入：${result.newRecordCount} 条记录")
                                if (result.categoryCount > 0) append("，${result.categoryCount} 个分类")
                                if (result.skippedRows > 0) append("，跳过 ${result.skippedRows} 行")
                            }
                            Toast.makeText(this@MainActivity, msg, Toast.LENGTH_LONG).show()
                        }
                        is ImportResult.Error -> {
                            Toast.makeText(this@MainActivity, "导入失败：${result.message}", Toast.LENGTH_LONG).show()
                        }
                        is ImportResult.Empty -> {
                            Toast.makeText(this@MainActivity, "导入文件为空", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "导入失败：${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        billExportManager = BillExportManager(this)

        val numiApp = application as NumiApplication
        val viewModelFactory = RecordViewModelFactory(numiApp, numiApp.repository, numiApp.customCategoryRepository)

        setContent {
            val viewModel: RecordViewModel = viewModel(factory = viewModelFactory)
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

        // 标记是否显示报销统计界面（保留底部导航，支持返回手势）
        var isReimbursement by remember { mutableStateOf(false) }
        // 记录进入报销统计页前所在的底部页面，返回时恢复到该页面
        var reimbursementReturnRoute by remember { mutableStateOf(Screen.Home.route) }
        // 从报销页进入记账页时，记账完成/返回后应回到报销页
        var shouldReturnToReimbursementAfterRecord by remember { mutableStateOf(false) }
        // 报销页底部 FAB 记账时使用当前 Tab 对应的报销状态
        var reimbursementRecordStatus by remember { mutableStateOf(ReimburseStatus.PENDING.value) }
        // 报销页批量管理模式状态
        var isReimbursementBatchMode by remember { mutableStateOf(false) }

        // 账单明细界面状态
        var isBillsDetail by remember { mutableStateOf(false) }
        var billsDetailReturnRoute by remember { mutableStateOf(Screen.Home.route) }

        // 记账页全屏展示；报销统计页批量模式或账单明细批量模式下隐藏底部导航和 FAB
        val showBottomBar = !isRecording && !((isReimbursement || isBillsDetail) && isReimbursementBatchMode)

        fun leaveReimbursement() {
            isReimbursement = false
            currentRoute = reimbursementReturnRoute
        }

        fun finishRecord() {
            isRecording = false
            if (shouldReturnToReimbursementAfterRecord) {
                isReimbursement = true
                shouldReturnToReimbursementAfterRecord = false
            }
        }

        // 处理外层返回：记账页优先回到来源页，报销页/账单明细页回到进入前的底部页面
        BackHandler(enabled = isRecording || isReimbursement || isBillsDetail) {
            if (isRecording) {
                finishRecord()
            } else if (isReimbursement) {
                leaveReimbursement()
            } else if (isBillsDetail) {
                isBillsDetail = false
                currentRoute = billsDetailReturnRoute
            }
        }

        Scaffold(
            bottomBar = {
                if (showBottomBar) {
                    BottomNavigationBar(
                        currentRoute = currentRoute,
                        onNavigate = { route ->
                            if (isReimbursement) {
                                isReimbursement = false
                            }
                            if (isBillsDetail) {
                                isBillsDetail = false
                            }
                            previousRoute = currentRoute
                            currentRoute = route
                        },
                        onNavigateToRecord = {
                            viewModel.resetInput()
                            viewModel.cancelEdit()
                            shouldReturnToReimbursementAfterRecord = isReimbursement
                            if (isReimbursement) {
                                viewModel.setReimburseStatus(reimbursementRecordStatus)
                            }
                            isRecording = true
                        }
                    )
                }
            }
        ) { paddingValues ->
            // 使用 AnimatedContent 实现平滑的页面切换
            AnimatedContent(
                targetState = when {
                    isRecording -> Screen.Record.route
                    isReimbursement -> Screen.Reimbursement.route
                    isBillsDetail -> Screen.BillsDetail.route
                    else -> currentRoute
                },
                transitionSpec = { pageTransitionSpec() },
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
                            shouldReturnToReimbursementAfterRecord = false
                            isRecording = true
                        },
                        onEditRecord = { record: NumiRecord ->
                            viewModel.loadRecordForEdit(record)
                            shouldReturnToReimbursementAfterRecord = false
                            isRecording = true
                        },
                        onNavigateToReimbursement = {
                            reimbursementReturnRoute = currentRoute
                            currentRoute = "" // 清空路由，使底部导航栏无选中状态
                            isReimbursement = true
                        },
                        onNavigateToBillsDetail = {
                            billsDetailReturnRoute = currentRoute
                            currentRoute = "" // 清空路由，使底部导航栏无选中状态
                            isBillsDetail = true
                        }
                    )
                    Screen.Bills.route -> BillsScreen(
                        viewModel = viewModel,
                        onNavigateToRecord = {
                            viewModel.resetInput()
                            viewModel.cancelEdit()
                            shouldReturnToReimbursementAfterRecord = false
                            isRecording = true
                        },
                        onEditRecord = { record: NumiRecord ->
                            viewModel.loadRecordForEdit(record)
                            shouldReturnToReimbursementAfterRecord = false
                            isRecording = true
                        }
                    )
                    Screen.Record.route -> RecordScreen(
                        viewModel = viewModel,
                        onRecordingComplete = {
                            finishRecord()
                        },
                        onNavigateBack = {
                            finishRecord()
                        }
                    )
                    Screen.Reimbursement.route -> ReimbursementScreen(
                        viewModel = viewModel,
                        onNavigateBack = {
                            leaveReimbursement()
                        },
                        onRecordStatusPresetChange = { status ->
                            reimbursementRecordStatus = status
                        },
                        onEditRecord = { record ->
                            viewModel.loadRecordForEdit(record)
                            isReimbursement = false
                            shouldReturnToReimbursementAfterRecord = true
                            isRecording = true
                        },
                        onBatchModeChange = { isBatchMode ->
                            isReimbursementBatchMode = isBatchMode
                        }
                    )
                    Screen.BillsDetail.route -> BillsDetailScreen(
                        viewModel = viewModel,
                        onNavigateBack = {
                            isBillsDetail = false
                            currentRoute = billsDetailReturnRoute
                        },
                        onEditRecord = { record ->
                            viewModel.loadRecordForEdit(record)
                            isBillsDetail = false
                            isRecording = true
                        }
                    )
                    Screen.Settings.route -> SettingsScreen(
                        viewModel = viewModel,
                        onExportBills = { records -> launchExportFlow(records, viewModel.customCategories.value) },
                        onImportBills = { launchImportFlow() },
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
     * @param customCategories 待导出的自定义分类列表
     */
    private fun launchExportFlow(records: List<NumiRecord>, customCategories: List<com.herb.numi.data.CustomCategory> = emptyList()) {
        if (records.isEmpty()) {
            Toast.makeText(this, "暂无账单数据可导出", Toast.LENGTH_SHORT).show()
            return
        }

        pendingExportRecords = records
        pendingExportCategories = customCategories

        val defaultFileName = billExportManager.generateDefaultFileName()
        createDocumentLauncher.launch(defaultFileName)
    }

    /**
     * 启动导入流程
     * 打开SAF文件选择器让用户选择CSV文件
     */
    private fun launchImportFlow() {
        openDocumentLauncher.launch(arrayOf("text/csv", "text/comma-separated-values", "*/*"))
    }
}
