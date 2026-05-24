package com.herb.numi.ui.common

/**
 * 一次性 UI 事件封装类
 * 用于向 UI 层发送需要消费的事件（如 Snackbar、Toast、导航等）
 * 事件被消费后会自动重置，避免重复处理
 *
 * @param T 事件内容类型
 * @property content 事件内容
 */
open class UiEvent<out T>(private val content: T) {

    /**
     * 标记该事件是否已被消费
     */
    var hasBeenHandled = false
        private set

    /**
     * 获取事件内容，如果已被消费则返回 null
     * 用于确保事件只被处理一次
     */
    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }

    /**
     * 直接获取事件内容，不标记为已消费
     * 仅在需要查看内容但不确定是否要消费时使用
     */
    fun peekContent(): T = content
}

/**
 * 空内容的 UI 事件
 * 用于只需要触发一次但不需要携带数据的场景
 */
class UiEmptyEvent : UiEvent<Unit>(Unit)
