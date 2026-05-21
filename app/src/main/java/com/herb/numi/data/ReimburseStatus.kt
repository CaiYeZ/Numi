package com.herb.numi.data

/**
 * 报销状态枚举
 * 用于标识一条记录的报销状态
 *
 * @property value 数据库存储的字符串值
 * @property label 界面显示的文本
 */
enum class ReimburseStatus(
    val value: String,
    val label: String
) {
    NONE("none", "非报销"),
    PENDING("pending", "待报销"),
    REIMBURSED("reimbursed", "已报销");

    companion object {
        /**
         * 根据字符串值获取对应的枚举实例
         *
         * @param value 数据库存储的字符串值
         * @return 对应的 ReimburseStatus 枚举，若未找到则返回 NONE
         */
        fun fromValue(value: String?): ReimburseStatus {
            return entries.find { it.value == value } ?: NONE
        }
    }
}
