package com.herb.numi.ui

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.abs

/**
 * UI 公共 Modifier 扩展函数
 * 提取页面间重复使用的 Modifier 逻辑，遵循 DRY 原则
 */

/**
 * 消费顶部安全区域内边距
 * 用于全屏页面中手动处理顶部间距，避免内容被状态栏遮挡
 *
 * @param topInset 顶部安全区域高度
 */
fun Modifier.consumeTopInset(topInset: Dp): Modifier = this.then(
    Modifier.layout { measurable, constraints ->
        val insetPx = topInset.roundToPx()
        val placeable = measurable.measure(constraints)
        val height = maxOf(0, placeable.height - insetPx)
        layout(placeable.width, height) { placeable.placeRelative(0, -insetPx) }
    }
)

/**
 * 左侧边缘滑动返回手势
 * 从屏幕左边缘向右滑动超过阈值时触发返回操作
 *
 * @param onBack 返回回调
 */
fun Modifier.edgeSwipeBack(onBack: () -> Unit): Modifier = composed {
    val density = LocalDensity.current
    val edgeWidth = with(density) { 32.dp.toPx() }
    val triggerDistance = with(density) { 72.dp.toPx() }

    pointerInput(onBack) {
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false)
            if (down.position.x > edgeWidth) return@awaitEachGesture
            val start = down.position
            val pointerId = down.id
            while (true) {
                val event = awaitPointerEvent()
                val change = event.changes.firstOrNull { it.id == pointerId } ?: return@awaitEachGesture
                val deltaX = change.position.x - start.x
                val deltaY = change.position.y - start.y
                if (deltaX > triggerDistance && deltaX > abs(deltaY) * 1.5f) {
                    change.consume()
                    onBack()
                    return@awaitEachGesture
                }
                if (!change.pressed) return@awaitEachGesture
            }
        }
    }
}
