package com.axiel7.moelist.uicompose.composables

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.*

/**
 * Extension function to load more items when the bottom is reached
 * @param buffer Tells how many items before it reaches the bottom of the list to call `onLoadMore`. This value should be >= 0
 * @param onLoadMore The code to execute when it reaches the bottom of the list
 * @author Manav Tamboli
 */
@Composable
fun LazyListState.OnBottomReached(
    buffer: Int = 0,
    onLoadMore: () -> Unit
) {
    // Buffer must be positive.
    // Or our list will never reach the bottom.
    require(buffer >= 0) { "buffer cannot be negative, but was $buffer" }

    val shouldLoadMore = remember {
        derivedStateOf {
            val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()
                ?:
                return@derivedStateOf true

            // subtract buffer from the total items
            lastVisibleItem.index >=  layoutInfo.totalItemsCount - 1 - buffer
        }
    }

    LaunchedEffect(shouldLoadMore){
        snapshotFlow { shouldLoadMore.value }
            .collect { if (it) onLoadMore() }
    }
}