package com.herb.numi.ui

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import com.herb.numi.ui.navigation.Screen

/**
 * 页面切换动画配置
 * 提取 MainActivity 中的动画逻辑，遵循单一职责原则
 *
 * 动画规则：
 * - 进入特殊页面（记账/报销/明细）：从右侧滑入
 * - 离开特殊页面：向右侧滑出
 * - 普通页面切换：根据路由顺序左右滑动
 */
@OptIn(ExperimentalAnimationApi::class)
fun AnimatedContentTransitionScope<String>.pageTransitionSpec(): ContentTransform {
    val isEnteringSpecial = targetState == Screen.Record.route ||
            targetState == Screen.Reimbursement.route ||
            targetState == Screen.BillsDetail.route
    val isFromSpecial = initialState == Screen.Record.route ||
            initialState == Screen.Reimbursement.route ||
            initialState == Screen.BillsDetail.route

    val direction = when {
        isEnteringSpecial -> 1
        isFromSpecial -> -1
        else -> {
            val targetIndex = Screen.navigationItems.indexOf(
                Screen.navigationItems.find { it.route == targetState }
            )
            val initialIndex = Screen.navigationItems.indexOf(
                Screen.navigationItems.find { it.route == initialState }
            )
            if (targetIndex >= initialIndex) 1 else -1
        }
    }

    val easing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)

    return if (isEnteringSpecial) {
        ContentTransform(
            targetContentEnter = slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = tween(durationMillis = 350, easing = easing)
            ) + fadeIn(animationSpec = tween(durationMillis = 350, easing = easing)),
            initialContentExit = slideOutHorizontally(
                targetOffsetX = { -it / 3 },
                animationSpec = tween(durationMillis = 350, easing = easing)
            ) + fadeOut(animationSpec = tween(durationMillis = 350, easing = easing))
        )
    } else if (isFromSpecial) {
        ContentTransform(
            targetContentEnter = slideInHorizontally(
                initialOffsetX = { -it / 3 },
                animationSpec = tween(durationMillis = 350, easing = easing)
            ) + fadeIn(animationSpec = tween(durationMillis = 350, easing = easing)),
            initialContentExit = slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = tween(durationMillis = 350, easing = easing)
            ) + fadeOut(animationSpec = tween(durationMillis = 350, easing = easing))
        )
    } else {
        ContentTransform(
            targetContentEnter = if (direction > 0) {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(durationMillis = 300, easing = easing)
                ) + fadeIn(animationSpec = tween(durationMillis = 300, easing = easing))
            } else {
                slideInHorizontally(
                    initialOffsetX = { -it },
                    animationSpec = tween(durationMillis = 300, easing = easing)
                ) + fadeIn(animationSpec = tween(durationMillis = 300, easing = easing))
            },
            initialContentExit = if (direction > 0) {
                slideOutHorizontally(
                    targetOffsetX = { -it },
                    animationSpec = tween(durationMillis = 300, easing = easing)
                ) + fadeOut(animationSpec = tween(durationMillis = 300, easing = easing))
            } else {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(durationMillis = 300, easing = easing)
                ) + fadeOut(animationSpec = tween(durationMillis = 300, easing = easing))
            }
        )
    }
}
