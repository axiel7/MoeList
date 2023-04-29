package com.axiel7.moelist.uicompose.search

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.anime.AnimeList
import com.axiel7.moelist.data.model.anime.seasonYearText
import com.axiel7.moelist.data.model.manga.MangaList
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.data.model.media.durationText
import com.axiel7.moelist.data.model.media.localized
import com.axiel7.moelist.data.model.media.mediaFormatLocalized
import com.axiel7.moelist.data.model.media.totalDuration
import com.axiel7.moelist.uicompose.base.TabRowItem
import com.axiel7.moelist.uicompose.composables.MediaItemDetailed
import com.axiel7.moelist.uicompose.composables.MediaItemDetailedPlaceholder
import com.axiel7.moelist.uicompose.composables.OnBottomReached
import com.axiel7.moelist.uicompose.composables.RoundedTabRowIndicator
import com.axiel7.moelist.uicompose.theme.MoeListTheme
import com.axiel7.moelist.utils.NumExtensions.toStringPositiveValueOrNull
import com.axiel7.moelist.utils.StringExtensions.toStringOrNull
import kotlinx.coroutines.launch

val searchTabRowItems = listOf(
    TabRowItem(MediaType.ANIME, title = "anime"),
    TabRowItem(MediaType.MANGA, title = "manga")
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SearchView(
    query: String,
    performSearch: MutableState<Boolean>,
    navController: NavController
) {
    val pagerState = rememberPagerState()
    val coroutineScope = rememberCoroutineScope()

    Column {
        TabRow(
            selectedTabIndex = pagerState.currentPage,
            indicator = { tabPositions ->
                RoundedTabRowIndicator(tabPositions[pagerState.currentPage])
            }
        ) {
            searchTabRowItems.forEachIndexed { index, tabRowItem ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = { coroutineScope.launch { pagerState.animateScrollToPage(index) } },
                    text = { Text(text = tabRowItem.value.localized()) }
                )
            }
        }

        HorizontalPager(
            pageCount = searchTabRowItems.size,
            state = pagerState,
            beyondBoundsPageCount = 0,
            key = { searchTabRowItems[it].title }
        ) {
            SearchResultList(
                query = query,
                performSearch = performSearch,
                mediaType = searchTabRowItems[it].value,
                navController = navController
            )
        }//: HorizontalPager
    }//: Column
}

@Composable
fun SearchResultList(
    query: String,
    performSearch: MutableState<Boolean>,
    mediaType: MediaType,
    navController: NavController
) {
    val viewModel: SearchViewModel = viewModel(key = mediaType.value)
    val listState = rememberLazyListState()

    LazyColumn(
        modifier = Modifier.fillMaxHeight(),
        state = listState
    ) {
        if (viewModel.mediaList.isEmpty()) {
            if (viewModel.isLoading)
                items(10) {
                    MediaItemDetailedPlaceholder()
                }
            else if (query.isNotBlank() && performSearch.value)
                item {
                    Text(
                        text = stringResource(R.string.no_results),
                        modifier = Modifier.padding(16.dp)
                    )
                }
        }
        else items(viewModel.mediaList,
            contentType = { it.node }
        ) {
            MediaItemDetailed(
                title = it.node.title,
                imageUrl = it.node.mainPicture?.large,
                subtitle1 = {
                    Text(
                        text = buildString {
                            append(it.node.mediaType?.mediaFormatLocalized())
                            if (it.node.totalDuration().toStringPositiveValueOrNull() != null) {
                                append(" (${it.node.durationText()})")
                            }
                        },
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                subtitle2 = {
                    Text(
                        text = when (it) {
                            is AnimeList -> it.node.startSeason.seasonYearText()
                            is MangaList -> it.node.startDate ?: stringResource(R.string.unknown)
                            else -> stringResource(R.string.unknown)
                        },
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                subtitle3 = {
                    Icon(
                        painter = painterResource(R.drawable.ic_round_details_star_24),
                        contentDescription = "star",
                        modifier = Modifier.padding(end = 4.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = it.node.mean?.toStringOrNull() ?: "??",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                onClick = {
                    navController.navigate("details/${mediaType.value}/${it.node.id}")
                }
            )
        }
    }//: LazyColumn

    listState.OnBottomReached(buffer = 3) {
        if (!viewModel.isLoading && viewModel.hasNextPage) {
            viewModel.search(
                mediaType = mediaType,
                query = query,
                page = viewModel.nextPage
            )
        }
    }

    LaunchedEffect(query, performSearch.value) {
        if (query.isNotBlank() && performSearch.value) {
            viewModel.search(
                mediaType = mediaType,
                query = query
            )
            performSearch.value = false
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SearchPreview() {
    MoeListTheme {
        SearchView(
            query = "one",
            performSearch = remember { mutableStateOf(false) },
            navController = rememberNavController()
        )
    }
}