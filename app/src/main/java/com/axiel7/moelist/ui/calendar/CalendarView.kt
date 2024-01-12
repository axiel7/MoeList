package com.axiel7.moelist.ui.calendar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.data.model.media.WeekDay
import com.axiel7.moelist.ui.base.TabRowItem
import com.axiel7.moelist.ui.composables.DefaultScaffoldWithTopAppBar
import com.axiel7.moelist.ui.composables.TabRowWithPager
import com.axiel7.moelist.ui.composables.media.MEDIA_POSTER_SMALL_WIDTH
import com.axiel7.moelist.ui.composables.media.MediaItemVertical
import com.axiel7.moelist.ui.composables.media.MediaItemVerticalPlaceholder
import com.axiel7.moelist.ui.theme.MoeListTheme
import com.axiel7.moelist.utils.ContextExtensions.showToast
import com.axiel7.moelist.utils.SeasonCalendar
import org.koin.androidx.compose.koinViewModel

const val CALENDAR_DESTINATION = "calendar/{day}"

@Composable
fun CalendarView(
    viewModel: CalendarViewModel = koinViewModel(),
    navigateBack: () -> Unit,
    navigateToMediaDetails: (MediaType, Int) -> Unit,
) {
    val context = LocalContext.current

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

    val tabRowItems = remember {
        WeekDay.entries.map {
            TabRowItem(value = it, title = it.stringRes)
        }.toTypedArray()
    }
    DefaultScaffoldWithTopAppBar(
        title = stringResource(R.string.calendar),
        navigateBack = navigateBack,
        contentWindowInsets = WindowInsets.systemBars
            .only(WindowInsetsSides.Horizontal)
    ) { padding ->
        TabRowWithPager(
            tabs = tabRowItems,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            initialPage = SeasonCalendar.currentWeekday.numeric - 1,
            isTabScrollable = true
        ) { page ->
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = MEDIA_POSTER_SMALL_WIDTH.dp),
                modifier = Modifier.fillMaxHeight(),
                contentPadding = PaddingValues(
                    start = 8.dp,
                    top = 8.dp,
                    end = 8.dp,
                    bottom = WindowInsets.navigationBars.asPaddingValues()
                        .calculateBottomPadding()
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
            ) {
                items(
                    items = viewModel.weekAnime[page],
                    contentType = { it }
                ) { item ->
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        MediaItemVertical(
                            imageUrl = item.node.mainPicture?.large,
                            title = item.node.title,
                            subtitle = {
                                Text(
                                    text = item.node.broadcast?.startTime ?: "??",
                                    color = MaterialTheme.colorScheme.outline
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
                    items(10) {
                        MediaItemVerticalPlaceholder()
                    }
                }
            }
        }//:Column
    }//:Scaffold
}

@Preview(showBackground = true)
@Composable
fun CalendarPreview() {
    MoeListTheme {
        CalendarView(
            navigateBack = {},
            navigateToMediaDetails = { _, _ -> }
        )
    }
}