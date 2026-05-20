package com.herb.numi.data.export

import android.content.Context
import android.net.Uri
import com.herb.numi.data.Record
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStreamWriter

/**
 * 账单导出管理器
 * 负责协调CSV导出和文件保存流程
 *
 * 遵循单一职责原则：负责导出流程编排，具体格式转换委托给CsvExporter
 */
class BillExportManager(private val context: Context) {

    companion object {
        private const val CSV_MIME_TYPE = "text/csv"
        private const val CSV_FILE_EXTENSION = ".csv"
        private const val DEFAULT_FILE_NAME = "Numi_账单导出"
    }

    /**
     * 导出账单为CSV文件并写入指定URI
     *
     * @param records 账单记录列表
     * @param uri 目标文件URI（通过SAF获取）
     * @param customFileName 自定义文件名（不含扩展名）
     * @return 导出结果
     */
    suspend fun exportToCsv(
        records: List<Record>,
        uri: Uri,
        customFileName: String = DEFAULT_FILE_NAME
    ): ExportResult = withContext(Dispatchers.IO) {
        try {
            if (records.isEmpty()) {
                return@withContext ExportResult.Empty
            }

            // 生成CSV内容
            val csvContent = CsvExporter.export(records)

            // 写入文件
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                OutputStreamWriter(outputStream, Charsets.UTF_8).use { writer ->
                    // 写入BOM以支持Excel正确识别UTF-8编码
                    writer.write("\uFEFF")
                    writer.write(csvContent)
                }
            } ?: return@withContext ExportResult.Error("无法打开输出流")

            val fileName = "$customFileName$CSV_FILE_EXTENSION"
            ExportResult.Success(fileName, records.size)
        } catch (e: IllegalArgumentException) {
            ExportResult.Error(e.message ?: "数据验证失败", e)
        } catch (e: Exception) {
            ExportResult.Error("导出失败: ${e.message}", e)
        }
    }

    /**
     * 生成带时间戳的默认文件名
     *
     * @return 格式为"账单导出_YYYYMMdd_HHmmss"的文件名
     */
    fun generateDefaultFileName(): String {
        val timestamp = java.text.SimpleDateFormat(
            "yyyyMMdd_HHmmss",
            java.util.Locale.getDefault()
        ).format(java.util.Date())
        return "${DEFAULT_FILE_NAME}_$timestamp"
    }

    /**
     * 获取CSV文件的MIME类型
     */
    fun getCsvMimeType(): String = CSV_MIME_TYPE

    /**
     * 获取CSV文件扩展名
     */
    fun getCsvExtension(): String = CSV_FILE_EXTENSION
}
