package com.herb.numi.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 记账记录实体类
 * 用于存储每一条收支记录
 *
 * @param id 记录ID，自增长
 * @param amount 金额
 * @param type 记录类型：income（收入）或 expense（支出）
 * @param category 分类名称
 * @param note 备注，可选
 * @param createdAt 创建时间戳
 * @param updatedAt 最新更新时间戳，默认与创建时间一致
 * @param reimburseStatus 报销状态：none（非报销）、pending（待报销）、reimbursed（已报销）
 */
@Entity(tableName = "records")
data class Record(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val type: String,      // "income" or "expense"
    val category: String,
    val note: String? = null,
    val createdAt: Long,   // 时间戳（支持映射为"今天"或"昨天"）
    val updatedAt: Long = createdAt,  // 最新更新时间戳
    val reimburseStatus: String = "none"  // none, pending, reimbursed
)
