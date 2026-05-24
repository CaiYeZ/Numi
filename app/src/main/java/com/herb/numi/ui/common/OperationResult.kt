package com.herb.numi.ui.common

/**
 * 操作结果封装类
 * 用于表示异步操作的状态和结果
 */
sealed class OperationResult<out T> {
    /**
     * 空闲状态，未执行任何操作
     */
    data object Idle : OperationResult<Nothing>()

    /**
     * 操作进行中
     */
    data object Loading : OperationResult<Nothing>()

    /**
     * 操作成功
     * @param data 成功返回的数据
     * @param message 成功提示信息
     */
    data class Success<out T>(
        val data: T? = null,
        val message: String = ""
    ) : OperationResult<T>()

    /**
     * 操作失败
     * @param message 错误提示信息
     */
    data class Error(
        val message: String = ""
    ) : OperationResult<Nothing>()

    /**
     * 是否处于加载中状态
     */
    val isLoading: Boolean get() = this is Loading

    /**
     * 是否是成功状态
     */
    val isSuccess: Boolean get() = this is Success

    /**
     * 是否是错误状态
     */
    val isError: Boolean get() = this is Error

    /**
     * 是否是空闲状态
     */
    val isIdle: Boolean get() = this is Idle
}
