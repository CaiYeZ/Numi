package com.herb.numi.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 自定义分类实体
 */
@Entity(tableName = "custom_categories")
data class CustomCategory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val icon: CategoryIcon,
    val type: String,  // "expense" or "income"
    val createdAt: Long = System.currentTimeMillis()
)