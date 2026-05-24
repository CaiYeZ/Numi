package com.herb.numi.data.`import`

import com.herb.numi.data.CategoryIcon
import com.herb.numi.data.CustomCategory
import com.herb.numi.data.Record
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * CSV导入器
 * 负责将CSV格式字符串解析为Record和CustomCategory列表
 *
 * 遵循单一职责原则：仅负责CSV格式解析，不涉及文件IO或UI逻辑
 * 支持向后兼容：可解析旧版导出格式（不含自定义分类区块）
 */
object CsvImporter {

    private const val RECORDS_HEADER = "交易时间,交易类型,分类,交易金额,备注"
    private const val CATEGORIES_HEADER = "分类名称,图标,类型"

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    /**
     * 解析CSV字符串为记录列表和自定义分类列表
     *
     * CSV格式说明：
     * - 记录区块：以"交易时间,交易类型,分类,交易金额,备注"开头
     * - 自定义分类区块：以空行分隔，以"分类名称,图标,类型"开头
     * - 向后兼容：不含自定义分类区块的旧版CSV也能正常解析
     *
     * @param csvContent CSV格式字符串
     * @return 导入结果
     */
    fun import(csvContent: String): ImportResult {
        if (csvContent.isBlank()) {
            return ImportResult.Empty
        }

        // 移除BOM标记
        val content = csvContent.trimStart('\uFEFF')

        // 按空行分割为区块
        val sections = content.split("\n\n").map { it.trim() }.filter { it.isNotEmpty() }

        val records = mutableListOf<Record>()
        val customCategories = mutableListOf<CustomCategory>()
        val errors = mutableListOf<String>()
        var skippedRows = 0

        for (section in sections) {
            val lines = section.lines()
            if (lines.isEmpty()) continue

            val header = lines.first().trim()

            when {
                header == RECORDS_HEADER -> {
                    val result = parseRecords(lines.drop(1))
                    records.addAll(result.records)
                    skippedRows += result.skippedRows
                    errors.addAll(result.errors)
                }
                header == CATEGORIES_HEADER -> {
                    val result = parseCustomCategories(lines.drop(1))
                    customCategories.addAll(result.categories)
                    skippedRows += result.skippedRows
                    errors.addAll(result.errors)
                }
                else -> {
                    // 尝试作为记录解析（向后兼容旧版格式）
                    val result = parseRecords(lines)
                    if (result.records.isNotEmpty()) {
                        records.addAll(result.records)
                    } else {
                        skippedRows += lines.size
                    }
                }
            }
        }

        if (records.isEmpty() && customCategories.isEmpty()) {
            return if (errors.isNotEmpty()) {
                ImportResult.Error(errors.joinToString("; "))
            } else {
                ImportResult.Empty
            }
        }

        return if (errors.isEmpty() && skippedRows == 0) {
            ImportResult.Success(
                recordCount = records.size,
                newRecordCount = records.size,
                categoryCount = customCategories.size
            )
        } else {
            ImportResult.Partial(
                newRecordCount = records.size,
                categoryCount = customCategories.size,
                skippedRows = skippedRows,
                errors = errors
            )
        }.also {
            // 将解析结果存储在伴生对象中供外部获取
            _lastImportedRecords = records
            _lastImportedCategories = customCategories
        }
    }

    /**
     * 获取最近一次导入的记录列表
     */
    fun getLastImportedRecords(): List<Record> = _lastImportedRecords

    /**
     * 获取最近一次导入的自定义分类列表
     */
    fun getLastImportedCategories(): List<CustomCategory> = _lastImportedCategories

    private var _lastImportedRecords: List<Record> = emptyList()
    private var _lastImportedCategories: List<CustomCategory> = emptyList()

    /**
     * 解析记录行
     */
    private fun parseRecords(lines: List<String>): RecordParseResult {
        val records = mutableListOf<Record>()
        val errors = mutableListOf<String>()
        var skippedRows = 0

        for ((index, line) in lines.withIndex()) {
            if (line.isBlank()) continue

            try {
                val fields = parseCsvLine(line)
                if (fields.size < 4) {
                    skippedRows++
                    errors.add("第${index + 1}行：字段不足（需要至少4个字段）")
                    continue
                }

                val time = parseTime(fields[0])
                val type = parseType(fields[1])
                val category = fields[2]
                val amount = parseAmount(fields[3])
                val note = fields.getOrElse(4) { "" }

                if (time != null && type != null && amount != null) {
                    records.add(
                        Record(
                            amount = amount,
                            type = type,
                            category = category,
                            note = note.ifEmpty { null },
                            createdAt = time,
                            updatedAt = time,
                            reimburseStatus = "none"
                        )
                    )
                } else {
                    skippedRows++
                    errors.add("第${index + 1}行：数据格式错误")
                }
            } catch (e: Exception) {
                skippedRows++
                errors.add("第${index + 1}行：${e.message}")
            }
        }

        return RecordParseResult(records, skippedRows, errors)
    }

    /**
     * 解析自定义分类行
     */
    private fun parseCustomCategories(lines: List<String>): CategoryParseResult {
        val categories = mutableListOf<CustomCategory>()
        val errors = mutableListOf<String>()
        var skippedRows = 0

        for ((index, line) in lines.withIndex()) {
            if (line.isBlank()) continue

            try {
                val fields = parseCsvLine(line)
                if (fields.size < 3) {
                    skippedRows++
                    errors.add("第${index + 1}行：字段不足（需要3个字段）")
                    continue
                }

                val name = fields[0]
                val icon = parseCategoryIcon(fields[1])
                val type = parseCategoryType(fields[2])

                if (icon != null && type != null) {
                    categories.add(
                        CustomCategory(
                            name = name,
                            icon = icon,
                            type = type
                        )
                    )
                } else {
                    skippedRows++
                    errors.add("第${index + 1}行：图标或类型格式错误")
                }
            } catch (e: Exception) {
                skippedRows++
                errors.add("第${index + 1}行：${e.message}")
            }
        }

        return CategoryParseResult(categories, skippedRows, errors)
    }

    /**
     * 解析CSV行，处理引号内的逗号
     */
    private fun parseCsvLine(line: String): List<String> {
        val fields = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false

        for (char in line) {
            when {
                char == '"' && !inQuotes -> inQuotes = true
                char == '"' && inQuotes -> inQuotes = false
                char == ',' && !inQuotes -> {
                    fields.add(current.toString().trim())
                    current.clear()
                }
                else -> current.append(char)
            }
        }
        fields.add(current.toString().trim())

        return fields
    }

    /**
     * 解析时间字符串为时间戳
     */
    private fun parseTime(timeStr: String): Long? {
        return try {
            dateFormat.parse(timeStr)?.time
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 解析交易类型
     */
    private fun parseType(typeStr: String): String? {
        return when (typeStr.trim()) {
            "收入" -> "income"
            "支出" -> "expense"
            "income" -> "income"
            "expense" -> "expense"
            else -> null
        }
    }

    /**
     * 解析金额字符串
     * 支持格式："¥100.00"、"-¥100.00"、"100.00"
     */
    private fun parseAmount(amountStr: String): Double? {
        return try {
            val cleaned = amountStr
                .replace("¥", "")
                .replace("-", "")
                .replace(",", "")
                .trim()
            val amount = cleaned.toDouble()
            if (amount >= 0) amount else null
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 解析分类图标枚举
     */
    private fun parseCategoryIcon(iconStr: String): CategoryIcon? {
        return try {
            CategoryIcon.valueOf(iconStr.trim())
        } catch (e: Exception) {
            CategoryIcon.MORE_HORIZ
        }
    }

    /**
     * 解析分类类型
     */
    private fun parseCategoryType(typeStr: String): String? {
        return when (typeStr.trim()) {
            "支出" -> "expense"
            "收入" -> "income"
            "expense" -> "expense"
            "income" -> "income"
            else -> null
        }
    }

    private data class RecordParseResult(
        val records: List<Record>,
        val skippedRows: Int,
        val errors: List<String>
    )

    private data class CategoryParseResult(
        val categories: List<CustomCategory>,
        val skippedRows: Int,
        val errors: List<String>
    )
}
