package com.herb.numi.data.export

import com.herb.numi.data.Record
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * CSV导出器单元测试
 * 验证导出功能的正确性和鲁棒性
 */
class CsvExporterTest {

    @Test
    fun `导出单条记录应包含正确表头和数据行`() {
        val records = listOf(
            Record(
                id = 1,
                amount = 100.0,
                type = "expense",
                category = "餐饮",
                note = "午餐",
                createdAt = 1704067200000 // 2024-01-01 00:00:00
            )
        )

        val result = CsvExporter.export(records)
        val lines = result.lines().filter { it.isNotBlank() }

        assertEquals(2, lines.size)
        assertEquals("交易时间,交易类型,分类,交易金额,备注", lines[0])
        assertTrue(lines[1].contains("餐饮"))
        assertTrue(lines[1].contains("支出"))
        assertTrue(lines[1].contains("¥100.00"))
    }

    @Test
    fun `导出收入记录应显示正金额`() {
        val records = listOf(
            Record(
                id = 1,
                amount = 5000.0,
                type = "income",
                category = "工资",
                note = "",
                createdAt = 1704067200000
            )
        )

        val result = CsvExporter.export(records)
        val dataLine = result.lines()[1]

        assertTrue(dataLine.contains("收入"))
        assertTrue(dataLine.contains("¥5000.00"))
    }

    @Test
    fun `导出支出记录应显示负金额`() {
        val records = listOf(
            Record(
                id = 1,
                amount = 50.5,
                type = "expense",
                category = "交通",
                note = null,
                createdAt = 1704067200000
            )
        )

        val result = CsvExporter.export(records)
        val dataLine = result.lines()[1]

        assertTrue(dataLine.contains("支出"))
        assertTrue(dataLine.contains("-¥50.50"))
    }

    @Test
    fun `空备注应显示为空字符串`() {
        val records = listOf(
            Record(
                id = 1,
                amount = 100.0,
                type = "expense",
                category = "餐饮",
                note = null,
                createdAt = 1704067200000
            )
        )

        val result = CsvExporter.export(records)
        val dataLine = result.lines()[1]
        val fields = dataLine.split(",")

        assertEquals("", fields[4])
    }

    @Test(expected = IllegalArgumentException::class)
    fun `空记录列表应抛出异常`() {
        CsvExporter.export(emptyList())
    }

    @Test
    fun `导出多条记录应按顺序排列`() {
        val records = listOf(
            Record(
                id = 1,
                amount = 100.0,
                type = "expense",
                category = "餐饮",
                note = "",
                createdAt = 1704067200000
            ),
            Record(
                id = 2,
                amount = 200.0,
                type = "income",
                category = "工资",
                note = "",
                createdAt = 1704153600000
            )
        )

        val result = CsvExporter.export(records)
        val lines = result.lines().filter { it.isNotBlank() }

        assertEquals(3, lines.size)
    }

    @Test
    fun `包含逗号的备注应正确转义`() {
        val records = listOf(
            Record(
                id = 1,
                amount = 100.0,
                type = "expense",
                category = "餐饮",
                note = "午餐,晚餐",
                createdAt = 1704067200000
            )
        )

        val result = CsvExporter.export(records)

        assertTrue(result.contains("\"午餐,晚餐\""))
    }

    @Test
    fun `包含双引号的备注应正确转义`() {
        val records = listOf(
            Record(
                id = 1,
                amount = 100.0,
                type = "expense",
                category = "餐饮",
                note = "他说\"好吃\"",
                createdAt = 1704067200000
            )
        )

        val result = CsvExporter.export(records)

        assertTrue(result.contains("\"他说\"\"好吃\"\"\""))
    }

    @Test
    fun `包含换行符的备注应正确转义`() {
        val records = listOf(
            Record(
                id = 1,
                amount = 100.0,
                type = "expense",
                category = "餐饮",
                note = "第一行\n第二行",
                createdAt = 1704067200000
            )
        )

        val result = CsvExporter.export(records)

        assertTrue(result.contains("\"第一行\n第二行\""))
    }

    @Test
    fun `时间格式应为YYYY-MM-DD HH-MM-SS`() {
        val records = listOf(
            Record(
                id = 1,
                amount = 100.0,
                type = "expense",
                category = "餐饮",
                note = "",
                createdAt = 1704067200000 // 2024-01-01 08:00:00 CST
            )
        )

        val result = CsvExporter.export(records)
        val dataLine = result.lines()[1]
        val fields = dataLine.split(",")

        // 验证时间格式符合 yyyy-MM-dd HH:mm:ss 模式
        val timePattern = Regex("""\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}""")
        assertTrue("时间格式应为 yyyy-MM-dd HH:mm:ss", timePattern.matches(fields[0]))
    }

    @Test
    fun `金额应保留两位小数`() {
        val records = listOf(
            Record(
                id = 1,
                amount = 99.9,
                type = "expense",
                category = "购物",
                note = "",
                createdAt = 1704067200000
            )
        )

        val result = CsvExporter.export(records)
        val dataLine = result.lines()[1]

        assertTrue(dataLine.contains("-¥99.90"))
    }

    @Test
    fun `大量数据导出应在合理时间内完成`() {
        val records = (1..1000).map { index ->
            Record(
                id = index.toLong(),
                amount = index.toDouble(),
                type = if (index % 2 == 0) "income" else "expense",
                category = "分类$index",
                note = "备注$index",
                createdAt = 1704067200000 + index * 1000
            )
        }

        val startTime = System.currentTimeMillis()
        val result = CsvExporter.export(records)
        val endTime = System.currentTimeMillis()

        assertTrue("导出1000条数据应少于5秒", endTime - startTime < 5000)
        assertTrue(result.lines().size > 1000)
    }
}
