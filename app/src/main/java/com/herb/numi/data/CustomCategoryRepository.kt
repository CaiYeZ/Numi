package com.herb.numi.data

import kotlinx.coroutines.flow.Flow

/**
 * 自定义分类仓库实现
 */
class CustomCategoryRepository(
    private val customCategoryDao: CustomCategoryDao
) : CustomCategoryRepositoryInterface {

    override fun getCategoriesByType(type: String): Flow<List<CustomCategory>> =
        customCategoryDao.getCategoriesByType(type)

    override fun getAllCategories(): Flow<List<CustomCategory>> =
        customCategoryDao.getAllCategories()

    override suspend fun insertCategory(category: CustomCategory): Long =
        customCategoryDao.insert(category)

    override suspend fun deleteCategory(category: CustomCategory) =
        customCategoryDao.delete(category)

    override suspend fun deleteCategoryById(id: Long) =
        customCategoryDao.deleteById(id)

    /**
     * 批量插入自定义分类（用于数据导入）
     */
    override suspend fun importCategories(categories: List<CustomCategory>) =
        customCategoryDao.insertAll(categories)

    /**
     * 批量插入自定义分类（用于数据导入），自动跳过已存在的分类
     * 基于名称和类型判断重复
     */
    override suspend fun importCategoriesWithDeduplication(categories: List<CustomCategory>): Int {
        var insertedCount = 0
        for (category in categories) {
            val exists = customCategoryDao.categoryExists(
                name = category.name,
                type = category.type
            )
            if (exists == 0) {
                customCategoryDao.insert(category)
                insertedCount++
            }
        }
        return insertedCount
    }
}