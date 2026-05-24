package com.herb.numi.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed

/**
 * 点击节流器
 * 防止按钮在短时间内被重复点击
 *
 * @param throttleTime 节流时间（毫秒），默认800ms
 */
class ClickThrottler(private val throttleTime: Long = 800L) {
    private var lastClickTime by mutableLongStateOf(0L)

    /**
     * 尝试执行点击操作
     * @param action 点击时要执行的操作
     * @return 是否成功执行（未被节流）
     */
    fun tryClick(action: () -> Unit): Boolean {
        val currentTime = System.currentTimeMillis()
        return if (currentTime - lastClickTime >= throttleTime) {
            lastClickTime = currentTime
            action()
            true
        } else {
            false
        }
    }
}

/**
 * 创建并记住一个点击节流器
 *
 * @param throttleTime 节流时间（毫秒），默认800ms
 */
@Composable
fun rememberClickThrottler(throttleTime: Long = 800L): ClickThrottler {
    return remember { ClickThrottler(throttleTime) }
}

/**
 * Modifier 扩展：添加点击节流功能
 *
 * @param throttleTime 节流时间（毫秒），默认800ms
 * @param enabled 是否启用点击
 * @param onClick 点击回调
 */
fun Modifier.throttleClick(
    throttleTime: Long = 800L,
    enabled: Boolean = true,
    onClick: () -> Unit
): Modifier = composed {
    val throttler = rememberClickThrottler(throttleTime)
    this.then(
        Modifier.clickable(enabled = enabled) {
            throttler.tryClick(onClick)
        }
    )
}
