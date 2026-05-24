package com.herb.numi.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * 自定义分类 DAO 接口
 */
@Dao
interface CustomCategoryDao {
    @Query("SELECT * FROM custom_categories WHERE type = :type ORDER BY createdAt DESC")
    fun getCategoriesByType(type: String): Flow<List<CustomCategory>>

    @Query("SELECT * FROM custom_categories ORDER BY createdAt DESC")
    fun getAllCategories(): Flow<List<CustomCategory>>

    @Insert
    suspend fun insert(category: CustomCategory): Long

    /**
     * 批量插入自定义分类
     */
    @Insert
    suspend fun insertAll(categories: List<CustomCategory>)

    @Delete
    suspend fun delete(category: CustomCategory)

    @Query("DELETE FROM custom_categories WHERE id = :id")
    suspend fun deleteById(id: Long)

    /**
     * 删除所有自定义分类
     */
    @Query("DELETE FROM custom_categories")
    suspend fun deleteAll()

    /**
     * 检查分类是否已存在（基于名称和类型）
     */
    @Query("SELECT COUNT(*) FROM custom_categories WHERE name = :name AND type = :type")
    suspend fun categoryExists(name: String, type: String): Int
}