package com.axiel7.moelist.uicompose.ranking

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.data.model.media.localized
import com.axiel7.moelist.data.model.media.rankingAnimeValues
import com.axiel7.moelist.data.model.media.rankingMangaValues
import com.axiel7.moelist.uicompose.base.TabRowItem
import com.axiel7.moelist.uicompose.composables.DefaultScaffoldWithTopAppBar
import com.axiel7.moelist.uicompose.composables.RoundedTabRowIndicator
import com.axiel7.moelist.uicompose.details.MEDIA_TYPE_ARGUMENT
import com.axiel7.moelist.uicompose.theme.MoeListTheme
import kotlinx.coroutines.launch

const val MEDIA_RANKING_DESTINATION = "ranking/$MEDIA_TYPE_ARGUMENT"

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MediaRankingView(
    mediaType: MediaType,
    isCompactScreen: Boolean,
    navigateBack: () -> Unit,
    navigateToMediaDetails: (MediaType, Int) -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val tabRowItems = remember {
        (if (mediaType == MediaType.ANIME) rankingAnimeValues else rankingMangaValues)
            .map {
                TabRowItem(value = it, title = it.value)
            }
    }
    val pagerState = rememberPagerState { tabRowItems.size }

    DefaultScaffoldWithTopAppBar(
        title = stringResource(
            if (mediaType == MediaType.ANIME) R.string.anime_ranking
            else R.string.manga_ranking
        ),
        navigateBack = navigateBack,
        contentWindowInsets = WindowInsets.systemBars
            .only(WindowInsetsSides.Horizontal)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            ScrollableTabRow(
                selectedTabIndex = pagerState.currentPage,
                modifier = Modifier.fillMaxWidth(),
                edgePadding = 8.dp,
                indicator = { tabPositions ->
                    RoundedTabRowIndicator(tabPositions[pagerState.currentPage])
                },
                //TODO: use default when width is fixed upstream
                // https://issuetracker.google.com/issues/242879624
                divider = { }
            ) {
                tabRowItems.forEachIndexed { index, tabRowItem ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = { coroutineScope.launch { pagerState.animateScrollToPage(index) } },
                        text = { Text(text = tabRowItem.value.localized()) }
                    )
                }
            }
            HorizontalDivider()

            HorizontalPager(
                state = pagerState,
                beyondBoundsPageCount = 0,
                key = { tabRowItems[it].title }
            ) {
                MediaRankingListView(
                    mediaType = mediaType,
                    rankingType = tabRowItems[it].value,
                    showAsGrid = !isCompactScreen,
                    navigateToMediaDetails = navigateToMediaDetails
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MediaRankingPreview() {
    MoeListTheme {
        MediaRankingView(
            mediaType = MediaType.MANGA,
            isCompactScreen = true,
            navigateBack = {},
            navigateToMediaDetails = { _, _ -> }
        )
    }
}