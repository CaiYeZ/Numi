package com.herb.numi.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * 类别图标枚举的扩展属性：获取对应的 Material Icons 向量
 * 将分散在各 UI 文件中的重复映射逻辑统一到这里，避免多处维护
 */
val CategoryIcon.imageVector: ImageVector
    get() = when (this) {
        // 餐饮
        CategoryIcon.RESTAURANT -> Icons.Filled.Restaurant
        CategoryIcon.LOCAL_CAFE -> Icons.Filled.LocalCafe
        CategoryIcon.LOCAL_BAR -> Icons.Filled.LocalBar
        CategoryIcon.DELIVERY_DINING -> Icons.Filled.DeliveryDining
        // 交通
        CategoryIcon.DIRECTIONS_CAR -> Icons.Filled.DirectionsCar
        CategoryIcon.DIRECTIONS_BUS -> Icons.Filled.DirectionsBus
        CategoryIcon.TRAIN -> Icons.Filled.Train
        CategoryIcon.FLIGHT -> Icons.Filled.Flight
        CategoryIcon.PEDAL_BIKE -> Icons.Filled.PedalBike
        // 购物
        CategoryIcon.SHOPPING_BAG -> Icons.Filled.ShoppingBag
        CategoryIcon.SHOPPING_CART -> Icons.Filled.ShoppingCart
        CategoryIcon.STORE -> Icons.Filled.Store
        // 娱乐
        CategoryIcon.SPORTS_ESPORTS -> Icons.Filled.SportsEsports
        CategoryIcon.MOVIE -> Icons.Filled.Movie
        CategoryIcon.MUSIC_NOTE -> Icons.Filled.MusicNote
        CategoryIcon.MIC -> Icons.Filled.Mic
        CategoryIcon.CELEBRATION -> Icons.Filled.Celebration
        // 居家
        CategoryIcon.HOME -> Icons.Filled.Home
        CategoryIcon.LIGHTBULB -> Icons.Filled.Lightbulb
        CategoryIcon.WIFI -> Icons.Filled.Wifi
        CategoryIcon.SMARTPHONE -> Icons.Filled.Smartphone
        CategoryIcon.CLEANING_SERVICES -> Icons.Filled.CleaningServices
        CategoryIcon.BUILD -> Icons.Filled.Build
        // 医疗
        CategoryIcon.LOCAL_HOSPITAL -> Icons.Filled.LocalHospital
        CategoryIcon.MEDICATION -> Icons.Filled.Medication
        CategoryIcon.HEALTH_AND_SAFETY -> Icons.Filled.HealthAndSafety
        // 学习
        CategoryIcon.SCHOOL -> Icons.Filled.School
        CategoryIcon.MENU_BOOK -> Icons.AutoMirrored.Filled.MenuBook
        CategoryIcon.WORK -> Icons.Filled.Work
        // 金融
        CategoryIcon.PAYMENTS -> Icons.Filled.Payments
        CategoryIcon.ACCOUNT_BALANCE_WALLET -> Icons.Filled.AccountBalanceWallet
        CategoryIcon.CREDIT_CARD -> Icons.Filled.CreditCard
        CategoryIcon.SAVINGS -> Icons.Filled.Savings
        CategoryIcon.TRENDING_UP -> Icons.AutoMirrored.Filled.TrendingUp
        // 旅行
        CategoryIcon.LUGGAGE -> Icons.Filled.Luggage
        CategoryIcon.PHOTO_CAMERA -> Icons.Filled.PhotoCamera
        // 其他
        CategoryIcon.MORE_HORIZ -> Icons.Filled.MoreHoriz
        CategoryIcon.PET -> Icons.Filled.Pets
        CategoryIcon.CARD_GIFTCARD -> Icons.Filled.CardGiftcard
        CategoryIcon.FAVORITE -> Icons.Filled.Favorite
        CategoryIcon.SYNC_ALT -> Icons.Filled.SyncAlt
        CategoryIcon.ANALYTICS -> Icons.Filled.Analytics
        CategoryIcon.SETTINGS -> Icons.Filled.Settings
        CategoryIcon.CATEGORY -> Icons.Filled.Category
        CategoryIcon.MONITORING -> Icons.AutoMirrored.Filled.TrendingUp
    }

/**
 * 支出分类常量及图标映射
 */
object ExpenseCategory {
    val categories = listOf(
        "餐饮",
        "交通",
        "购物",
        "娱乐",
        "日用",
        "其他"
    )

    val icons = mapOf(
        "餐饮" to CategoryIcon.RESTAURANT,
        "交通" to CategoryIcon.DIRECTIONS_CAR,
        "购物" to CategoryIcon.SHOPPING_BAG,
        "娱乐" to CategoryIcon.SPORTS_ESPORTS,
        "日用" to CategoryIcon.HOME,
        "其他" to CategoryIcon.MORE_HORIZ
    )
}

/**
 * 收入分类常量及图标映射
 */
object IncomeCategory {
    val categories = listOf(
        "工资",
        "生活费",
        "零花",
        "转账",
        "其他"
    )

    val icons = mapOf(
        "工资" to CategoryIcon.PAYMENTS,
        "生活费" to CategoryIcon.HOME,
        "零花" to CategoryIcon.CARD_GIFTCARD,
        "转账" to CategoryIcon.SYNC_ALT,
        "其他" to CategoryIcon.MORE_HORIZ
    )
}

/**
 * 类别图标枚举
 * 按用途分类：餐饮、交通、购物、娱乐、居家、医疗、学习、金融、旅行、其他
 */
enum class CategoryIcon(
    val category: String  // 图标所属分类
) {
    // ========== 餐饮 ==========
    RESTAURANT("餐饮"),      // 餐饮/餐厅
    LOCAL_CAFE("餐饮"),       // 咖啡
    LOCAL_BAR("餐饮"),        // 酒吧
    DELIVERY_DINING("餐饮"), // 外卖

    // ========== 交通 ==========
    DIRECTIONS_CAR("交通"),   // 汽车/打车
    DIRECTIONS_BUS("交通"),   // 公交
    TRAIN("交通"),            // 地铁/火车
    FLIGHT("交通"),           // 飞机
    PEDAL_BIKE("交通"),       // 自行车

    // ========== 购物 ==========
    SHOPPING_BAG("购物"),     // 购物
    SHOPPING_CART("购物"),    // 网购
    STORE("购物"),            // 商店

    // ========== 娱乐 ==========
    SPORTS_ESPORTS("娱乐"),   // 游戏
    MOVIE("娱乐"),            // 电影
    MUSIC_NOTE("娱乐"),       // 音乐
    MIC("娱乐"),              // KTV/演出
    CELEBRATION("娱乐"),      // 聚会

    // ========== 居家 ==========
    HOME("居家"),             // 房租/住房
    LIGHTBULB("居家"),        // 水电
    WIFI("居家"),             // 网络
    SMARTPHONE("居家"),       // 通讯
    CLEANING_SERVICES("居家"), // 清洁
    BUILD("居家"),            // 维修

    // ========== 医疗 ==========
    LOCAL_HOSPITAL("医疗"),   // 医院
    MEDICATION("医疗"),        // 药品
    HEALTH_AND_SAFETY("医疗"), // 医疗/健康

    // ========== 学习 ==========
    SCHOOL("学习"),           // 学校
    MENU_BOOK("学习"),        // 书籍
    WORK("学习"),             // 工作/学习

    // ========== 金融 ==========
    PAYMENTS("金融"),         // 工资/支付
    ACCOUNT_BALANCE_WALLET("金融"), // 钱包
    CREDIT_CARD("金融"),      // 银行卡
    SAVINGS("金融"),          // 储蓄
    TRENDING_UP("金融"),      // 投资

    // ========== 旅行 ==========
    LUGGAGE("旅行"),          // 旅行/行李
    PHOTO_CAMERA("旅行"),     // 摄影

    // ========== 其他 ==========
    MORE_HORIZ("其他"),       // 更多
    PET("其他"),              // 宠物
    CARD_GIFTCARD("其他"),    // 礼物
    FAVORITE("其他"),         // 恋爱/心情
    SYNC_ALT("其他"),         // 转账
    ANALYTICS("其他"),        // 统计
    SETTINGS("其他"),         // 设置
    CATEGORY("其他"),         // 分类
    MONITORING("其他")        // 股票/监控
}