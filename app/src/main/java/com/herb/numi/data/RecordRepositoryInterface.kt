package com.herb.numi.data

import kotlinx.coroutines.flow.Flow

/**
 * 记账数据仓库接口
 * 定义数据访问的抽象契约，遵循依赖倒置原则
 */
interface RecordRepositoryInterface {
    /**
     * 获取所有记录
     */
    val allRecords: Flow<List<Record>>

    /**
     * 插入新记录
     */
    suspend fun insertRecord(record: Record): Long

    /**
     * 更新记录
     */
    suspend fun updateRecord(record: Record)

    /**
     * 删除记录
     */
    suspend fun deleteRecord(record: Record)

    /**
     * 根据ID删除记录
     */
    suspend fun deleteRecordById(id: Long)

    /**
     * 批量删除记录
     */
    suspend fun deleteRecordsByIds(ids: List<Long>)

    /**
     * 获取指定时间范围内的记录
     */
    fun getRecordsInRange(startTime: Long, endTime: Long): Flow<List<Record>>

    /**
     * 获取当月支出总额
     */
    fun getMonthExpense(startTime: Long, endTime: Long): Flow<Double>

    /**
     * 获取当月收入总额
     */
    fun getMonthIncome(startTime: Long, endTime: Long): Flow<Double>

    /**
     * 获取所有记录用于数据备份
     */
    suspend fun getAllRecordsForBackup(): List<Record>

    /**
     * 批量更新记录的报销状态
     */
    suspend fun updateRecordsReimburseStatus(ids: List<Long>, status: String)
}
