package com.axiel7.moelist.ui.composables

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
    topBarOffsetY: Float,
    animateTo: suspend (Float) -> Unit,
    snapTo: suspend (Float) -> Unit,
) = composed {
    val scope = rememberCoroutineScope()

    LaunchedEffect(key1 = state.isScrollInProgress) {
        if (!state.isScrollInProgress && topBarOffsetY != 0f && topBarOffsetY != -topBarHeightPx) {
            val half = topBarHeightPx / 2

            val targetOffsetY = when {
                abs(topBarOffsetY) >= half -> -topBarHeightPx
                else -> 0f
            }

            launch {
                state.animateScrollBy(topBarOffsetY - targetOffsetY)
            }

            launch {
                animateTo(targetOffsetY)
            }
        }
    }

    nestedScroll(
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                scope.launch {
                    if (state.canScrollForward) {
                        snapTo(
                            (topBarOffsetY + available.y).coerceIn(
                                minimumValue = -topBarHeightPx,
                                maximumValue = 0f,
                            )
                        )
                    }
                }

                return Offset.Zero
            }
        }
    )
}
