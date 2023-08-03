package com.axiel7.moelist.uicompose.calendar

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
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
import com.axiel7.moelist.data.model.media.WeekDay
import com.axiel7.moelist.uicompose.composables.DefaultScaffoldWithTopAppBar
import com.axiel7.moelist.uicompose.composables.RoundedTabRowIndicator
import com.axiel7.moelist.uicompose.composables.media.MediaItemDetailed
import com.axiel7.moelist.uicompose.composables.media.MediaItemDetailedPlaceholder
import com.axiel7.moelist.uicompose.theme.MoeListTheme
import com.axiel7.moelist.utils.ContextExtensions.showToast
import com.axiel7.moelist.utils.NumExtensions.toStringPositiveValueOrNull
import com.axiel7.moelist.utils.NumExtensions.toStringPositiveValueOrUnknown
import com.axiel7.moelist.utils.SeasonCalendar
import kotlinx.coroutines.launch

const val CALENDAR_DESTINATION = "calendar/{day}"

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CalendarView(
    showAsGrid: Boolean,
    navigateBack: () -> Unit,
    navigateToMediaDetails: (MediaType, Int) -> Unit,
) {
    val context = LocalContext.current
    val viewModel: CalendarViewModel = viewModel()
    val pagerState = rememberPagerState(
        initialPage = SeasonCalendar.currentWeekday.numeric - 1,
        pageCount = { WeekDay.values().size }
    )
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(viewModel.message) {
        if (viewModel.showMessage) {
            context.showToast(viewModel.message)
            viewModel.showMessage = false
        }
    }

    LaunchedEffect(Unit) {
        if (viewModel.weekAnime[0].isEmpty())
            viewModel.getSeasonAnime()
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
                    text = item.node.broadcast?.startTime ?: "??",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            onClick = {
                navigateToMediaDetails(MediaType.ANIME, item.node.id)
            }
        )
    }

    DefaultScaffoldWithTopAppBar(
        title = stringResource(R.string.calendar),
        navigateBack = navigateBack,
        contentWindowInsets = WindowInsets.systemBars
            .only(WindowInsetsSides.Horizontal)
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding)
        ) {
            ScrollableTabRow(
                selectedTabIndex = pagerState.currentPage,
                edgePadding = 8.dp,
                indicator = { tabPositions ->
                    RoundedTabRowIndicator(tabPositions[pagerState.currentPage])
                }
            ) {
                WeekDay.values().forEachIndexed { index, weekDay ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = { coroutineScope.launch { pagerState.animateScrollToPage(index) } },
                        text = { Text(text = weekDay.localized()) }
                    )
                }
            }

            HorizontalPager(
                state = pagerState
            ) { page ->
                if (showAsGrid) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(
                            top = 8.dp,
                            bottom = WindowInsets.navigationBars.asPaddingValues()
                                .calculateBottomPadding()
                        )
                    ) {
                        items(
                            items = viewModel.weekAnime[page],
                            contentType = { it }
                        ) { item ->
                            ItemView(item = item)
                        }
                        if (viewModel.isLoading) {
                            items(10) {
                                MediaItemDetailedPlaceholder()
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(
                            top = 8.dp,
                            bottom = WindowInsets.navigationBars.asPaddingValues()
                                .calculateBottomPadding()
                        )
                    ) {
                        items(
                            items = viewModel.weekAnime[page],
                            contentType = { it }
                        ) { item ->
                            ItemView(item = item)
                        }
                        if (viewModel.isLoading) {
                            items(10) {
                                MediaItemDetailedPlaceholder()
                            }
                        }
                    }//:LazyColumn
                }
            }//:Pager
        }//:Column
    }//:Scaffold
}

@Preview(showBackground = true)
@Composable
fun CalendarPreview() {
    MoeListTheme {
        CalendarView(
            showAsGrid = false,
            navigateBack = {},
            navigateToMediaDetails = { _, _ -> }
        )
    }
}