package com.herb.numi

import android.app.Application
import android.content.Context
import com.herb.numi.data.AppDatabase
import com.herb.numi.data.CustomCategoryRepository
import com.herb.numi.data.RecordRepository

/**
 * 应用类
 * 负责初始化全局单例对象
 */
class NumiApplication : Application() {

    // 数据库实例，通过 lazy 延迟初始化
    val database: AppDatabase by lazy {
        AppDatabase.getDatabase(this)
    }

    // 数据仓库实例
    val repository: RecordRepository by lazy {
        RecordRepository(database.recordDao(), database.customCategoryDao())
    }

    // 自定义分类仓库实例
    val customCategoryRepository: CustomCategoryRepository by lazy {
        CustomCategoryRepository(database.customCategoryDao())
    }

    // 主题模式偏好
    private val prefs by lazy {
        getSharedPreferences("numi_prefs", Context.MODE_PRIVATE)
    }

    fun getThemeMode(): String {
        return prefs.getString("theme_mode", "system") ?: "system"
    }

    fun setThemeMode(mode: String) {
        prefs.edit().putString("theme_mode", mode).apply()
    }

    // ========== 报销统计分组展开状态 ==========

    /**
     * 获取分组展开状态
     * @param groupKey 分组键（日期键，如 "2026-05-20" 或 "2026-05"）
     * @return true 表示展开，false 表示折叠
     */
    fun isGroupExpanded(groupKey: String): Boolean {
        return prefs.getBoolean("expanded_$groupKey", true)
    }

    /**
     * 保存分组展开状态
     * @param groupKey 分组键
     * @param expanded 是否展开
     */
    fun setGroupExpanded(groupKey: String, expanded: Boolean) {
        prefs.edit().putBoolean("expanded_$groupKey", expanded).apply()
    }

    /**
     * 批量保存分组展开状态
     * @param expandedGroups 展开的分组键集合
     * @param collapsedGroups 折叠的分组键集合
     */
    fun saveGroupExpansionStates(expandedGroups: Set<String>, collapsedGroups: Set<String>) {
        val editor = prefs.edit()
        expandedGroups.forEach { key ->
            editor.putBoolean("expanded_$key", true)
        }
        collapsedGroups.forEach { key ->
            editor.putBoolean("expanded_$key", false)
        }
        editor.apply()
    }

    /**
     * 清除所有分组展开状态
     */
    fun clearAllGroupExpansionStates() {
        val keysToRemove = prefs.all.keys.filter { it.startsWith("expanded_") }
        val editor = prefs.edit()
        keysToRemove.forEach { key ->
            editor.remove(key)
        }
        editor.apply()
    }
}
