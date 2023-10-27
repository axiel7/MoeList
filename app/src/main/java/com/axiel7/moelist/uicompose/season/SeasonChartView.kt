package com.axiel7.moelist.uicompose.season

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.uicompose.composables.DefaultScaffoldWithTopAppBar
import com.axiel7.moelist.uicompose.composables.OnBottomReached
import com.axiel7.moelist.uicompose.composables.media.MEDIA_POSTER_SMALL_WIDTH
import com.axiel7.moelist.uicompose.composables.media.MediaItemDetailedPlaceholder
import com.axiel7.moelist.uicompose.composables.media.MediaItemVertical
import com.axiel7.moelist.uicompose.composables.media.SmallScoreIndicator
import com.axiel7.moelist.uicompose.season.composables.SeasonChartFilterSheet
import com.axiel7.moelist.uicompose.theme.MoeListTheme
import com.axiel7.moelist.utils.ContextExtensions.showToast
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

const val SEASON_CHART_DESTINATION = "season_chart"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeasonChartView(
    viewModel: SeasonChartViewModel = koinViewModel(),
    navigateBack: () -> Unit,
    navigateToMediaDetails: (MediaType, Int) -> Unit,
) {
    val context = LocalContext.current
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
        val listState = rememberLazyGridState()
        listState.OnBottomReached(buffer = 3) {
            onLoadMore()
        }
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = MEDIA_POSTER_SMALL_WIDTH.dp),
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(
                start = 8.dp,
                top = 8.dp,
                end = 8.dp,
                bottom = bottomBarPadding
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
        ) {
            items(
                items = viewModel.animes,
                key = { it.node.id },
                contentType = { it.node }
            ) { item ->
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    MediaItemVertical(
                        imageUrl = item.node.mainPicture?.large,
                        title = item.node.title,
                        subtitle = {
                            SmallScoreIndicator(
                                score = item.node.mean,
                                fontSize = 13.sp
                            )
                        },
                        minLines = 2,
                        onClick = {
                            navigateToMediaDetails(MediaType.ANIME, item.node.id)
                        }
                    )
                }
            }
            if (viewModel.isLoading) {
                items(12) {
                    MediaItemDetailedPlaceholder()
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
            navigateBack = {},
            navigateToMediaDetails = { _, _ -> }
        )
    }
}