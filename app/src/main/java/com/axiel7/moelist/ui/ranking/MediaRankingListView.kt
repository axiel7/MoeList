package com.axiel7.moelist.ui.ranking

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.media.BaseRanking
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.data.model.media.RankingType
import com.axiel7.moelist.ui.composables.OnBottomReached
import com.axiel7.moelist.ui.composables.TextIconHorizontal
import com.axiel7.moelist.ui.composables.media.MediaItemDetailed
import com.axiel7.moelist.ui.composables.media.MediaItemDetailedPlaceholder
import com.axiel7.moelist.utils.ContextExtensions.showToast
import com.axiel7.moelist.utils.NumExtensions.format
import com.axiel7.moelist.utils.NumExtensions.toStringPositiveValueOrNull
import com.axiel7.moelist.utils.NumExtensions.toStringPositiveValueOrUnknown
import com.axiel7.moelist.utils.UNKNOWN_CHAR
import org.koin.androidx.compose.koinViewModel

@Composable
fun MediaRankingListView(
    mediaType: MediaType,
    rankingType: RankingType,
    showAsGrid: Boolean,
    navigateToMediaDetails: (MediaType, Int) -> Unit,
) {
    val context = LocalContext.current
    val viewModel: MediaRankingViewModel = koinViewModel(key = rankingType.name)
    val shouldShowPlaceholder = viewModel.mediaList.isEmpty() && viewModel.isLoading

    LaunchedEffect(viewModel.message) {
        if (viewModel.showMessage) {
            context.showToast(viewModel.message)
            viewModel.showMessage = false
        }
    }

    LaunchedEffect(Unit) {
        if (!viewModel.isLoading && viewModel.nextPage == null && !viewModel.loadedAllPages)
            viewModel.getRanking(rankingType)
    }

    fun onLoadMore() {
        if (!viewModel.isLoading && viewModel.hasNextPage) {
            viewModel.getRanking(rankingType, viewModel.nextPage)
        }
    }

    @Composable
    fun ItemView(item: BaseRanking) {
        MediaItemDetailed(
            title = item.node.userPreferredTitle(),
            imageUrl = item.node.mainPicture?.large,
            badgeContent = {
                Text(
                    text = "#${item.ranking?.rank}",
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            },
            subtitle1 = {
                Text(
                    text = buildString {
                        append(item.node.mediaType?.localized())
                        if (item.node.totalDuration().toStringPositiveValueOrNull() != null) {
                            append(" (${item.node.durationText()})")
                        }
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            subtitle2 = {
                TextIconHorizontal(
                    text = item.node.mean.toStringPositiveValueOrUnknown(),
                    icon = R.drawable.ic_round_details_star_24,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            subtitle3 = {
                TextIconHorizontal(
                    text = item.node.numListUsers?.format() ?: UNKNOWN_CHAR,
                    icon = R.drawable.ic_round_group_24,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            onClick = {
                navigateToMediaDetails(mediaType, item.node.id)
            }
        )
    }

    if (showAsGrid) {
        val listState = rememberLazyGridState()
        listState.OnBottomReached(buffer = 3) {
            onLoadMore()
        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            state = listState,
            contentPadding = PaddingValues(
                top = 8.dp,
                bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
            ),
        ) {
            items(
                items = viewModel.mediaList,
                key = { it.node.id },
                contentType = { it.node }
            ) { item ->
                ItemView(item = item)
            }
            if (shouldShowPlaceholder) {
                items(10) {
                    MediaItemDetailedPlaceholder()
                }
            }
        }
    } else {
        val listState = rememberLazyListState()
        listState.OnBottomReached(buffer = 3) {
            onLoadMore()
        }
        LazyColumn(
            contentPadding = PaddingValues(
                top = 8.dp,
                bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
            ),
            state = listState
        ) {
            items(
                items = viewModel.mediaList,
                key = { it.node.id },
                contentType = { it.node }
            ) { item ->
                ItemView(item = item)
            }
            if (shouldShowPlaceholder) {
                items(10) {
                    MediaItemDetailedPlaceholder()
                }
            }
        }//:LazyColumn
    }
}