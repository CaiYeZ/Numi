package com.herb.numi.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * 记账记录的数据访问对象
 * 提供数据库操作的接口
 */
@Dao
interface RecordDao {
    /**
     * 插入一条新记录
     */
    @Insert
    suspend fun insert(record: Record): Long

    /**
     * 更新一条记录
     */
    @Update
    suspend fun update(record: Record)

    /**
     * 删除指定记录
     */
    @Delete
    suspend fun delete(record: Record)

    /**
     * 根据ID删除记录
     */
    @Query("DELETE FROM records WHERE id = :id")
    suspend fun deleteById(id: Long)

    /**
     * 批量删除记录
     */
    @Query("DELETE FROM records WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)

    /**
     * 获取所有记录，按时间倒序排列
     */
    @Query("SELECT * FROM records ORDER BY createdAt DESC")
    fun getAllRecords(): Flow<List<Record>>

    /**
     * 获取指定时间范围内的记录
     */
    @Query("SELECT * FROM records WHERE createdAt >= :startTime AND createdAt < :endTime ORDER BY createdAt DESC")
    fun getRecordsInRange(startTime: Long, endTime: Long): Flow<List<Record>>

    /**
     * 获取当月支出总额
     */
    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM records WHERE type = 'expense' AND createdAt >= :startTime AND createdAt < :endTime")
    fun getMonthExpense(startTime: Long, endTime: Long): Flow<Double>

    /**
     * 获取当月收入总额
     */
    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM records WHERE type = 'income' AND createdAt >= :startTime AND createdAt < :endTime")
    fun getMonthIncome(startTime: Long, endTime: Long): Flow<Double>

    /**
     * 获取所有记录用于备份（按时间正序）
     */
    @Query("SELECT * FROM records ORDER BY createdAt ASC")
    suspend fun getAllRecordsForBackup(): List<Record>

    /**
     * 批量更新记录的报销状态
     */
    @Query("UPDATE records SET reimburseStatus = :status WHERE id IN (:ids)")
    suspend fun updateReimburseStatusByIds(ids: List<Long>, status: String)
}
