# Numi

一款简洁优雅的 Android 记账应用，采用 Jetpack Compose 构建，帮助用户轻松记录日常收支。

## 技术栈

- **开发语言**：Kotlin
- **UI 框架**：Jetpack Compose (Material 3)
- **架构组件**：ViewModel + StateFlow
- **数据持久化**：Room 数据库
- **图表库**：Vico
- **最低支持版本**：Android 7.0 (API 24)
- **目标版本**：Android 16 (API 36)

## 功能特性

### 首页
- 月份收支概览卡片，展示当月总收入、总支出和结余
- 近期记录列表，按日期分组显示近3天的收支记录
- 支持长按进入批量操作模式（删除多条记录）
- 点击记录可快速编辑

### 记账
- 数字键盘输入金额
- 支持收入和支出两种类型切换
- 分类选择（支出：餐饮、交通、购物、娱乐、日用、其他；收入：工资、生活费、零花、转账、其他）
- 备注输入
- 时间选择（支持选择任意日期时间）
- 实时显示交易预览

### 报表
- 时间范围选择（月/年/自定义）
- 分类饼状图展示，带颜色渐变效果（金额越大颜色越深）
- 从圆心向外延伸的批注线，标注分类名称和百分比
- 分类明细列表
- 点击记录可快速编辑

### 设置
- 主题切换（跟随系统/浅色/深色模式）
- 数据备份导出（JSON 格式）

## 项目结构

```
app/src/main/java/com/herb/numi/
├── data/                          # 数据层
│   ├── AppDatabase.kt             # Room 数据库
│   ├── Record.kt                  # 记账记录实体
│   ├── RecordDao.kt               # 数据访问对象
│   ├── RecordRepository.kt        # 数据仓库
│   └── Category.kt                # 分类常量定义
├── ui/                            # 界面层
│   ├── components/                # 可复用组件
│   │   ├── CategorySelector.kt    # 分类选择器
│   │   └── NumberKeyboard.kt      # 数字键盘
│   ├── navigation/                # 导航
│   │   └── BottomNavigationBar.kt # 底部导航栏
│   ├── theme/                     # 主题配置
│   │   ├── Color.kt               # 颜色定义
│   │   ├── Theme.kt               # 主题配置
│   │   └── Type.kt                # 字体配置
│   ├── HomeScreen.kt              # 首页
│   ├── RecordScreen.kt            # 记账页面
│   ├── RecordViewModel.kt         # 记账视图模型
│   ├── BillsScreen.kt             # 报表页面
│   └── SettingsScreen.kt          # 设置页面
├── MainActivity.kt                # 主活动
└── NumiApplication.kt             # 应用入口
```

## 版本历史

详见 [CHANGELOG.md](./CHANGELOG.md)

## 构建

```bash
./gradlew assembleDebug      # 构建调试版本
./gradlew assembleRelease    # 构建发布版本
```

## 许可证

本项目为私有项目，保留所有权利。
