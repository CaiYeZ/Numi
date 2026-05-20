package com.herb.numi.data

import kotlinx.coroutines.flow.Flow

/**
 * 记账数据仓库
 * 封装数据访问逻辑，提供统一的数据操作接口
 *
 * 采用依赖倒置原则，通过接口抽象数据层
 */
class RecordRepository(private val recordDao: RecordDao) {

    /**
     * 获取所有记录
     */
    val allRecords: Flow<List<Record>> = recordDao.getAllRecords()

    /**
     * 插入新记录
     */
    suspend fun insertRecord(record: Record): Long {
        return recordDao.insert(record)
    }

    /**
     * 更新记录
     */
    suspend fun updateRecord(record: Record) {
        recordDao.update(record)
    }

    /**
     * 删除记录
     */
    suspend fun deleteRecord(record: Record) {
        recordDao.delete(record)
    }

    /**
     * 根据ID删除记录
     */
    suspend fun deleteRecordById(id: Long) {
        recordDao.deleteById(id)
    }

    /**
     * 批量删除记录
     */
    suspend fun deleteRecordsByIds(ids: List<Long>) {
        recordDao.deleteByIds(ids)
    }

    /**
     * 获取指定时间范围内的记录
     */
    fun getRecordsInRange(startTime: Long, endTime: Long): Flow<List<Record>> {
        return recordDao.getRecordsInRange(startTime, endTime)
    }

    /**
     * 获取当月支出总额
     */
    fun getMonthExpense(startTime: Long, endTime: Long): Flow<Double> {
        return recordDao.getMonthExpense(startTime, endTime)
    }

    /**
     * 获取当月收入总额
     */
    fun getMonthIncome(startTime: Long, endTime: Long): Flow<Double> {
        return recordDao.getMonthIncome(startTime, endTime)
    }

    /**
     * 获取所有记录用于数据备份
     */
    suspend fun getAllRecordsForBackup(): List<Record> {
        return recordDao.getAllRecordsForBackup()
    }
}
