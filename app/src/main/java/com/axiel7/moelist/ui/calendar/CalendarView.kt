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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.data.model.media.WeekDay
import com.axiel7.moelist.ui.base.navigation.NavActionManager
import com.axiel7.moelist.ui.composables.DefaultScaffoldWithTopAppBar
import com.axiel7.moelist.ui.composables.TabRowWithPager
import com.axiel7.moelist.ui.composables.media.MEDIA_POSTER_SMALL_WIDTH
import com.axiel7.moelist.ui.composables.media.MediaItemVertical
import com.axiel7.moelist.ui.composables.media.MediaItemVerticalPlaceholder
import com.axiel7.moelist.ui.theme.MoeListTheme
import com.axiel7.moelist.utils.ContextExtensions.showToast
import com.axiel7.moelist.utils.SeasonCalendar
import org.koin.androidx.compose.koinViewModel

@Composable
fun CalendarView(
    navActionManager: NavActionManager
) {
    val viewModel: CalendarViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CalendarContent(
        uiState = uiState,
        event = viewModel,
        navActionManager = navActionManager,
    )
}

@Composable
private fun CalendarContent(
    uiState: CalendarUiState,
    event: CalendarEvent?,
    navActionManager: NavActionManager,
) {
    val context = LocalContext.current

    if (uiState.message != null) {
        LaunchedEffect(uiState.message) {
            context.showToast(uiState.message)
            event?.onMessageDisplayed()
        }
    }

    DefaultScaffoldWithTopAppBar(
        title = stringResource(R.string.calendar),
        navigateBack = dropUnlessResumed { navActionManager.goBack() },
        contentWindowInsets = WindowInsets.systemBars
            .only(WindowInsetsSides.Horizontal)
    ) { padding ->
        TabRowWithPager(
            tabs = WeekDay.tabRowItems,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            initialPage = SeasonCalendar.currentWeekday.ordinal,
            isTabScrollable = true
        ) { page ->
            val weekday = page + 1
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
                    items = uiState.weekAnime(weekday),
                    contentType = { it }
                ) { item ->
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        MediaItemVertical(
                            imageUrl = item.node.mainPicture?.large,
                            title = item.node.userPreferredTitle(),
                            badgeContent = item.node.myListStatus?.status?.let { status ->
                                {
                                    Icon(
                                        painter = painterResource(status.icon),
                                        contentDescription = status.localized(),
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            },
                            subtitle = {
                                Text(
                                    text = item.node.broadcast?.startTime ?: "??",
                                    color = MaterialTheme.colorScheme.outline
                                )
                            },
                            minLines = 2,
                            onClick = dropUnlessResumed {
                                navActionManager.toMediaDetails(MediaType.ANIME, item.node.id)
                            }
                        )
                    }
                }
                if (uiState.isLoading) {
                    items(10) {
                        MediaItemVerticalPlaceholder()
                    }
                }
            }
        }//:Column
    }//:Scaffold
}

@Preview
@Composable
fun CalendarPreview() {
    MoeListTheme {
        Surface {
            CalendarContent(
                uiState = CalendarUiState(),
                event = null,
                navActionManager = NavActionManager.rememberNavActionManager()
            )
        }
    }
}