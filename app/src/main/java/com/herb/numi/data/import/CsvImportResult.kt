package com.herb.numi.data.import

/**
 * 导入结果密封类
 * 封装数据导入操作的结果状态
 */
sealed class ImportResult {
    /**
     * 导入成功
     * @param recordCount 导入的记录数（含新增和跳过重复）
     * @param newRecordCount 新增的记录数（不含重复）
     * @param duplicateCount 跳过的重复记录数
     * @param categoryCount 导入的自定义分类数
     */
    data class Success(
        val recordCount: Int,
        val newRecordCount: Int,
        val duplicateCount: Int = 0,
        val categoryCount: Int = 0
    ) : ImportResult()

    /**
     * 部分导入成功
     * @param newRecordCount 新增的记录数
     * @param categoryCount 导入的自定义分类数
     * @param skippedRows 跳过的行数（格式错误等）
     * @param errors 错误信息列表
     */
    data class Partial(
        val newRecordCount: Int,
        val categoryCount: Int = 0,
        val skippedRows: Int = 0,
        val errors: List<String> = emptyList()
    ) : ImportResult()

    /**
     * 导入失败
     * @param message 错误信息
     * @param throwable 异常对象（可选）
     */
    data class Error(
        val message: String,
        val throwable: Throwable? = null
    ) : ImportResult()

    /**
     * 导入文件为空
     */
    data object Empty : ImportResult()
}
