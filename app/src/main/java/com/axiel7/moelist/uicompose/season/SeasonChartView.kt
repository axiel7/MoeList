package com.axiel7.moelist.uicompose.season

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.anime.Season
import com.axiel7.moelist.data.model.anime.dayTimeText
import com.axiel7.moelist.data.model.anime.icon
import com.axiel7.moelist.data.model.anime.localized
import com.axiel7.moelist.data.model.anime.seasonYearText
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.data.model.media.durationText
import com.axiel7.moelist.data.model.media.mediaFormatLocalized
import com.axiel7.moelist.data.model.media.totalDuration
import com.axiel7.moelist.data.model.media.userPreferredTitle
import com.axiel7.moelist.uicompose.composables.DefaultScaffoldWithTopAppBar
import com.axiel7.moelist.uicompose.composables.MediaItemDetailed
import com.axiel7.moelist.uicompose.composables.MediaItemDetailedPlaceholder
import com.axiel7.moelist.uicompose.composables.OnBottomReached
import com.axiel7.moelist.uicompose.composables.SelectableIconToggleButton
import com.axiel7.moelist.uicompose.theme.MoeListTheme
import com.axiel7.moelist.utils.ContextExtensions.showToast
import com.axiel7.moelist.utils.NumExtensions.toStringPositiveValueOrNull
import com.axiel7.moelist.utils.NumExtensions.toStringPositiveValueOrUnknown
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

const val SEASON_CHART_DESTINATION = "season_chart"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeasonChartView(
    navigateBack: () -> Unit,
    navigateToMediaDetails: (MediaType, Int) -> Unit,
) {
    val context = LocalContext.current
    val viewModel: SeasonChartViewModel = viewModel()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()
    val bottomBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    listState.OnBottomReached(buffer = 3) {
        if (!viewModel.isLoading && viewModel.hasNextPage) {
            viewModel.getSeasonalAnime(viewModel.nextPage)
        }
    }

    if (sheetState.isVisible) {
        SeasonChartFilterSheet(
            coroutineScope = coroutineScope,
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
                    contentDescription = "filter"
                )
            }
        },
        contentWindowInsets = WindowInsets.systemBars
            .only(WindowInsetsSides.Horizontal)
    ) { padding ->
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
                MediaItemDetailed(
                    title = item.node.userPreferredTitle(),
                    imageUrl = item.node.mainPicture?.large,
                    subtitle1 = {
                        Text(
                            text = buildString {
                                append(item.node.mediaType?.mediaFormatLocalized())
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
                            text = item.node.broadcast.dayTimeText(),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    onClick = {
                        navigateToMediaDetails(MediaType.ANIME, item.node.id)
                    }
                )
            }
            if (viewModel.isLoading) {
                items(10) {
                    MediaItemDetailedPlaceholder()
                }
            }
        }
    }//:Scaffold
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeasonChartFilterSheet(
    coroutineScope: CoroutineScope,
    sheetState: SheetState,
    viewModel: SeasonChartViewModel,
    bottomPadding: Dp = 0.dp
) {
    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = { coroutineScope.launch { sheetState.hide() } },
        windowInsets = WindowInsets(0, 0, 0, 0)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp + bottomPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = {
                    coroutineScope.launch { sheetState.hide() }
                }) {
                    Text(text = stringResource(R.string.cancel))
                }

                Button(onClick = {
                    viewModel.getSeasonalAnime()
                    coroutineScope.launch { sheetState.hide() }
                }) {
                    Text(text = stringResource(R.string.apply))
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Season.values().forEach { season ->
                    SelectableIconToggleButton(
                        icon = season.icon(),
                        tooltipText = season.localized(),
                        value = season,
                        selectedValue = viewModel.season.season,
                        onClick = {
                            viewModel.setSeason(season = season)
                        }
                    )
                }
            }

            LazyRow(
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                items(viewModel.years) {
                    FilterChip(
                        selected = viewModel.season.year == it,
                        onClick = { viewModel.setSeason(year = it) },
                        label = { Text(text = it.toString()) },
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
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