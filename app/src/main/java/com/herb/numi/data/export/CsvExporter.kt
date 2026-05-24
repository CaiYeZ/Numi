package com.herb.numi.data.export

import com.herb.numi.data.CustomCategory
import com.herb.numi.data.Record
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * CSV导出器
 * 负责将账单记录和自定义分类导出为CSV格式字符串
 *
 * 遵循单一职责原则：仅负责CSV格式转换，不涉及文件IO或UI逻辑
 *
 * 导出格式：
 * - 第一区块：账单记录（交易时间,交易类型,分类,交易金额,备注）
 * - 空行分隔
 * - 第二区块：自定义分类（分类名称,图标,类型）
 */
object CsvExporter {

    private const val RECORDS_HEADER = "交易时间,交易类型,分类,交易金额,备注"
    private const val CATEGORIES_HEADER = "分类名称,图标,类型"

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    /**
     * 将记录列表导出为CSV格式字符串
     *
     * @param records 账单记录列表
     * @return CSV格式字符串
     * @throws IllegalArgumentException 当记录列表为空时抛出
     */
    fun export(records: List<Record>): String {
        if (records.isEmpty()) {
            throw IllegalArgumentException("没有可导出的账单数据")
        }

        val sb = StringBuilder()
        sb.appendLine(RECORDS_HEADER)

        records.forEach { record ->
            sb.appendLine(formatRecord(record))
        }

        return sb.toString()
    }

    /**
     * 将记录列表和自定义分类导出为CSV格式字符串
     * 包含两个区块：账单记录和自定义分类
     *
     * @param records 账单记录列表
     * @param customCategories 自定义分类列表
     * @return CSV格式字符串
     * @throws IllegalArgumentException 当记录列表和分类列表都为空时抛出
     */
    fun exportWithCategories(records: List<Record>, customCategories: List<CustomCategory>): String {
        if (records.isEmpty() && customCategories.isEmpty()) {
            throw IllegalArgumentException("没有可导出的数据")
        }

        val sb = StringBuilder()

        // 账单记录区块
        if (records.isNotEmpty()) {
            sb.appendLine(RECORDS_HEADER)
            records.forEach { record ->
                sb.appendLine(formatRecord(record))
            }
        }

        // 自定义分类区块（与记录区块用空行分隔）
        if (customCategories.isNotEmpty()) {
            if (records.isNotEmpty()) {
                sb.appendLine()
            }
            sb.appendLine(CATEGORIES_HEADER)
            customCategories.forEach { category ->
                sb.appendLine(formatCustomCategory(category))
            }
        }

        return sb.toString()
    }

    /**
     * 格式化单条记录为CSV行
     */
    private fun formatRecord(record: Record): String {
        val time = formatTime(record.createdAt)
        val type = formatType(record.type)
        val category = escapeCsvField(record.category)
        val amount = formatAmount(record.amount, record.type)
        val note = escapeCsvField(record.note ?: "")

        return "$time,$type,$category,$amount,$note"
    }

    /**
     * 格式化自定义分类为CSV行
     */
    private fun formatCustomCategory(category: CustomCategory): String {
        val name = escapeCsvField(category.name)
        val icon = category.icon.name
        val type = formatCategoryType(category.type)

        return "$name,$icon,$type"
    }

    /**
     * 格式化时间戳为指定格式
     */
    private fun formatTime(timestamp: Long): String {
        return dateFormat.format(Date(timestamp))
    }

    /**
     * 格式化交易类型
     */
    private fun formatType(type: String): String {
        return when (type.lowercase()) {
            "income" -> "收入"
            "expense" -> "支出"
            else -> type
        }
    }

    /**
     * 格式化分类类型
     */
    private fun formatCategoryType(type: String): String {
        return when (type.lowercase()) {
            "income" -> "收入"
            "expense" -> "支出"
            else -> type
        }
    }

    /**
     * 格式化交易金额
     */
    private fun formatAmount(amount: Double, type: String): String {
        require(amount >= 0) { "交易金额不能为负数: $amount" }
        val prefix = if (type == "income") "¥" else "-¥"
        return "$prefix${String.format(Locale.getDefault(), "%.2f", amount)}"
    }

    /**
     * 转义CSV字段中的特殊字符
     */
    private fun escapeCsvField(field: String): String {
        return when {
            field.contains(",") || field.contains("\n") || field.contains("\"") -> {
                "\"${field.replace("\"", "\"\"")}\""
            }
            else -> field
        }
    }
}
