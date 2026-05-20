package com.herb.numi.data.export

import com.herb.numi.data.Record
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * CSV导出器
 * 负责将账单记录导出为CSV格式字符串
 *
 * 遵循单一职责原则：仅负责CSV格式转换，不涉及文件IO或UI逻辑
 */
object CsvExporter {

    private const val HEADER = "交易时间,交易类型,分类,交易金额,备注"

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
        sb.appendLine(HEADER)

        records.forEach { record ->
            sb.appendLine(formatRecord(record))
        }

        return sb.toString()
    }

    /**
     * 格式化单条记录为CSV行
     *
     * @param record 账单记录
     * @return CSV格式的单行字符串
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
     * 格式化时间戳为指定格式
     *
     * @param timestamp 毫秒时间戳
     * @return "YYYY-MM-DD HH:MM:SS"格式的时间字符串
     */
    private fun formatTime(timestamp: Long): String {
        return dateFormat.format(Date(timestamp))
    }

    /**
     * 格式化交易类型
     *
     * @param type 原始类型字符串（"income"或"expense"）
     * @return 中文类型（"收入"或"支出"）
     */
    private fun formatType(type: String): String {
        return when (type.lowercase()) {
            "income" -> "收入"
            "expense" -> "支出"
            else -> type
        }
    }

    /**
     * 格式化交易金额
     *
     * @param amount 金额数值
     * @param type 交易类型（用于校验）
     * @return 带货币符号的格式化金额字符串（如"¥100.00"）
     * @throws IllegalArgumentException 当金额为负数或零时抛出
     */
    private fun formatAmount(amount: Double, type: String): String {
        require(amount >= 0) { "交易金额不能为负数: $amount" }
        val prefix = if (type == "income") "¥" else "-¥"
        return "$prefix${String.format(Locale.getDefault(), "%.2f", amount)}"
    }

    /**
     * 转义CSV字段中的特殊字符
     * 如果字段中包含逗号、换行或双引号，则使用双引号包裹并转义内部双引号
     *
     * @param field 原始字段值
     * @return 转义后的字段值
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
