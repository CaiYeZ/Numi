package com.herb.numi.data

import kotlinx.coroutines.flow.Flow

/**
 * 记账数据仓库实现
 * 封装数据访问逻辑，提供统一的数据操作接口
 *
 * 采用依赖倒置原则，实现 RecordRepositoryInterface 接口
 */
class RecordRepository(
    private val recordDao: RecordDao,
    private val customCategoryDao: CustomCategoryDao? = null
) : RecordRepositoryInterface {

    /**
     * 获取所有记录
     */
    override val allRecords: Flow<List<Record>> = recordDao.getAllRecords()

    /**
     * 插入新记录
     */
    override suspend fun insertRecord(record: Record): Long {
        return recordDao.insert(record)
    }

    /**
     * 更新记录
     */
    override suspend fun updateRecord(record: Record) {
        recordDao.update(record)
    }

    /**
     * 删除记录
     */
    override suspend fun deleteRecord(record: Record) {
        recordDao.delete(record)
    }

    /**
     * 根据ID删除记录
     */
    override suspend fun deleteRecordById(id: Long) {
        recordDao.deleteById(id)
    }

    /**
     * 批量删除记录
     */
    override suspend fun deleteRecordsByIds(ids: List<Long>) {
        recordDao.deleteByIds(ids)
    }

    /**
     * 获取指定时间范围内的记录
     */
    override fun getRecordsInRange(startTime: Long, endTime: Long): Flow<List<Record>> {
        return recordDao.getRecordsInRange(startTime, endTime)
    }

    /**
     * 获取当月支出总额
     */
    override fun getMonthExpense(startTime: Long, endTime: Long): Flow<Double> {
        return recordDao.getMonthExpense(startTime, endTime)
    }

    /**
     * 获取当月收入总额
     */
    override fun getMonthIncome(startTime: Long, endTime: Long): Flow<Double> {
        return recordDao.getMonthIncome(startTime, endTime)
    }

    /**
     * 获取所有记录用于数据备份
     */
    override suspend fun getAllRecordsForBackup(): List<Record> {
        return recordDao.getAllRecordsForBackup()
    }

    /**
     * 批量更新记录的报销状态
     */
    override suspend fun updateRecordsReimburseStatus(ids: List<Long>, status: String) {
        recordDao.updateReimburseStatusByIds(ids, status)
    }

    /**
     * 批量插入记录（用于数据导入）
     */
    override suspend fun importRecords(records: List<Record>) {
        recordDao.insertAll(records)
    }

    /**
     * 清空所有数据（记录和自定义分类）
     */
    override suspend fun clearAllData() {
        recordDao.deleteAll()
        customCategoryDao?.deleteAll()
    }

    /**
     * 批量插入记录（用于数据导入），自动跳过已存在的记录
     * 基于金额、类型、分类、创建时间判断重复
     */
    override suspend fun importRecordsWithDeduplication(records: List<Record>): Int {
        var insertedCount = 0
        for (record in records) {
            val exists = recordDao.recordExists(
                amount = record.amount,
                type = record.type,
                category = record.category,
                createdAt = record.createdAt
            )
            if (exists == 0) {
                recordDao.insert(record)
                insertedCount++
            }
        }
        return insertedCount
    }
}
