package com.herb.numi.data.export

/**
 * 导出结果密封类
 * 用于封装导出操作的结果状态
 */
sealed class ExportResult {
    /**
     * 导出成功
     * @param fileName 导出的文件名
     * @param recordCount 导出的记录数
     */
    data class Success(val fileName: String, val recordCount: Int) : ExportResult()

    /**
     * 导出失败
     * @param message 错误信息
     * @param throwable 异常对象（可选）
     */
    data class Error(val message: String, val throwable: Throwable? = null) : ExportResult()

    /**
     * 数据为空
     */
    data object Empty : ExportResult()
}
