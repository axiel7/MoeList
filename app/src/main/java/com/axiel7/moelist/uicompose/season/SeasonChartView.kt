package com.axiel7.moelist.uicompose.season

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.anime.AnimeSeasonal
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.uicompose.composables.DefaultScaffoldWithTopAppBar
import com.axiel7.moelist.uicompose.composables.OnBottomReached
import com.axiel7.moelist.uicompose.composables.media.MediaItemDetailed
import com.axiel7.moelist.uicompose.composables.media.MediaItemDetailedPlaceholder
import com.axiel7.moelist.uicompose.season.composables.SeasonChartFilterSheet
import com.axiel7.moelist.uicompose.theme.MoeListTheme
import com.axiel7.moelist.utils.ContextExtensions.showToast
import com.axiel7.moelist.utils.NumExtensions.toStringPositiveValueOrNull
import com.axiel7.moelist.utils.NumExtensions.toStringPositiveValueOrUnknown
import kotlinx.coroutines.launch

const val SEASON_CHART_DESTINATION = "season_chart"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeasonChartView(
    showAsGrid: Boolean,
    navigateBack: () -> Unit,
    navigateToMediaDetails: (MediaType, Int) -> Unit,
) {
    val context = LocalContext.current
    val viewModel: SeasonChartViewModel = viewModel()
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()
    val bottomBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    if (sheetState.isVisible) {
        SeasonChartFilterSheet(
            onDismiss = { coroutineScope.launch { sheetState.hide() } },
            sheetState = sheetState,
            viewModel = viewModel,
            bottomPadding = bottomBarPadding
        )
    }

    LaunchedEffect(viewModel.message) {
        if (viewModel.showMessage) {
            context.showToast(viewModel.message)
            viewModel.showMessage = false
        }
    }

    LaunchedEffect(Unit) {
        if (viewModel.animes.isEmpty()) viewModel.getSeasonalAnime()
    }

    fun onLoadMore() {
        if (!viewModel.isLoading && viewModel.hasNextPage) {
            viewModel.getSeasonalAnime(viewModel.nextPage)
        }
    }

    @Composable
    fun ItemView(item: AnimeSeasonal) {
        MediaItemDetailed(
            title = item.node.userPreferredTitle(),
            imageUrl = item.node.mainPicture?.large,
            subtitle1 = {
                Text(
                    text = buildString {
                        append(item.node.mediaType?.localized())
                        if (item.node.totalDuration()
                                .toStringPositiveValueOrNull() != null
                        ) {
                            append(" (${item.node.durationText()})")
                        }
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            subtitle2 = {
                Icon(
                    painter = painterResource(R.drawable.ic_round_details_star_24),
                    contentDescription = "star",
                    modifier = Modifier.padding(end = 4.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = item.node.mean.toStringPositiveValueOrUnknown(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            subtitle3 = {
                Icon(
                    painter = painterResource(R.drawable.ic_round_event_24),
                    contentDescription = "calendar",
                    modifier = Modifier.padding(end = 4.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = item.node.broadcast?.dayTimeText() ?: stringResource(R.string.unknown),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            onClick = {
                navigateToMediaDetails(MediaType.ANIME, item.node.id)
            }
        )
    }

    DefaultScaffoldWithTopAppBar(
        title = viewModel.season.seasonYearText(),
        navigateBack = navigateBack,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { coroutineScope.launch { sheetState.show() } },
                modifier = Modifier.padding(WindowInsets.navigationBars.asPaddingValues())
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_round_filter_list_24),
                    contentDescription = stringResource(R.string.filters)
                )
            }
        },
        contentWindowInsets = WindowInsets.systemBars
            .only(WindowInsetsSides.Horizontal)
    ) { padding ->
        if (showAsGrid) {
            val listState = rememberLazyGridState()
            listState.OnBottomReached(buffer = 3) {
                onLoadMore()
            }
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.padding(padding),
                state = listState,
                contentPadding = PaddingValues(
                    top = 8.dp,
                    bottom = bottomBarPadding
                )
            ) {
                items(
                    items = viewModel.animes,
                    key = { it.node.id },
                    contentType = { it.node }
                ) { item ->
                    ItemView(item = item)
                }
                if (viewModel.isLoading) {
                    items(6) {
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
                modifier = Modifier.padding(padding),
                state = listState,
                contentPadding = PaddingValues(
                    top = 8.dp,
                    bottom = bottomBarPadding
                )
            ) {
                items(
                    items = viewModel.animes,
                    key = { it.node.id },
                    contentType = { it.node }
                ) { item ->
                    ItemView(item = item)
                }
                if (viewModel.isLoading) {
                    items(10) {
                        MediaItemDetailedPlaceholder()
                    }
                }
            }
        }
    }//:Scaffold
}

@Preview(showBackground = true)
@Composable
fun SeasonChartPreview() {
    MoeListTheme {
        SeasonChartView(
            showAsGrid = false,
            navigateBack = {},
            navigateToMediaDetails = { _, _ -> }
        )
    }
}