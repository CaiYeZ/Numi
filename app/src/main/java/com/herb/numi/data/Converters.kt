package com.herb.numi.data

import androidx.room.TypeConverter

/**
 * Room TypeConverter for CategoryIcon enum
 */
class Converters {
    @TypeConverter
    fun fromCategoryIcon(icon: CategoryIcon): String = icon.name

    @TypeConverter
    fun toCategoryIcon(value: String): CategoryIcon = CategoryIcon.valueOf(value)
}