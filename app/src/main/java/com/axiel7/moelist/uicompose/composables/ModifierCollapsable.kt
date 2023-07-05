package com.axiel7.moelist.uicompose.composables

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import kotlinx.coroutines.launch
import kotlin.math.abs

fun Modifier.collapsable(
    state: ScrollableState,
    topBarHeightPx: Float,
    topBarOffsetY: Animatable<Float, AnimationVector1D>,
) = composed {
    val scope = rememberCoroutineScope()

    LaunchedEffect(key1 = state.isScrollInProgress) {
        if (!state.isScrollInProgress && topBarOffsetY.value != 0f && topBarOffsetY.value != -topBarHeightPx) {
            val half = topBarHeightPx / 2
            val oldOffsetY = topBarOffsetY.value

            val targetOffsetY = when {
                abs(topBarOffsetY.value) >= half -> -topBarHeightPx
                else -> 0f
            }

            launch {
                state.animateScrollBy(oldOffsetY - targetOffsetY)
            }

            launch {
                topBarOffsetY.animateTo(targetOffsetY)
            }
        }
    }

    nestedScroll(
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                scope.launch {
                    topBarOffsetY.snapTo(
                        targetValue = (topBarOffsetY.value + available.y).coerceIn(
                            minimumValue = -topBarHeightPx,
                            maximumValue = 0f,
                        )
                    )
                }

                return Offset.Zero
            }
        }
    )
}
