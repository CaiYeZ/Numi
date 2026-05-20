package com.herb.numi.data

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
        "交通" to CategoryIcon.TRANSPORT,
        "购物" to CategoryIcon.SHOPPING,
        "娱乐" to CategoryIcon.ENTERTAINMENT,
        "日用" to CategoryIcon.DAILY,
        "其他" to CategoryIcon.OTHER
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
        "工资" to CategoryIcon.SALARY,
        "生活费" to CategoryIcon.LIVING,
        "零花" to CategoryIcon.ALLOWANCE,
        "转账" to CategoryIcon.TRANSFER,
        "其他" to CategoryIcon.OTHER
    )
}

/**
 * 类别图标枚举
 */
enum class CategoryIcon {
    RESTAURANT,      // 餐饮
    TRANSPORT,       // 交通
    SHOPPING,        // 购物
    ENTERTAINMENT,   // 娱乐
    DAILY,           // 日用
    SALARY,          // 工资
    LIVING,          // 生活费
    ALLOWANCE,       // 零花
    TRANSFER,        // 转账
    OTHER            // 其他
}
