package com.herb.numi

import android.app.Application
import android.content.Context
import com.herb.numi.data.AppDatabase
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
        RecordRepository(database.recordDao())
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
}
