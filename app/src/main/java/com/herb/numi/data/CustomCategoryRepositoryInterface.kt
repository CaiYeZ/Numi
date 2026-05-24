package com.herb.numi.data

import kotlinx.coroutines.flow.Flow

/**
 * 自定义分类仓库接口
 */
interface CustomCategoryRepositoryInterface {
    fun getCategoriesByType(type: String): Flow<List<CustomCategory>>
    fun getAllCategories(): Flow<List<CustomCategory>>
    suspend fun insertCategory(category: CustomCategory): Long
    suspend fun deleteCategory(category: CustomCategory)
    suspend fun deleteCategoryById(id: Long)

    /**
     * 批量插入自定义分类（用于数据导入）
     */
    suspend fun importCategories(categories: List<CustomCategory>)

    /**
     * 批量插入自定义分类（用于数据导入），自动跳过已存在的分类
     * 基于名称和类型判断重复
     * @return 实际插入的新分类数量
     */
    suspend fun importCategoriesWithDeduplication(categories: List<CustomCategory>): Int
}