package com.axiel7.moelist.uicompose.userlist

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.media.*
import com.axiel7.moelist.uicompose.base.TabRowItem
import com.axiel7.moelist.uicompose.composables.*
import com.axiel7.moelist.uicompose.theme.MoeListTheme
import kotlinx.coroutines.launch

const val ANIME_LIST_DESTINATION = "anime_list"
const val MANGA_LIST_DESTINATION = "manga_list"

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UserMediaListHostView(
    mediaType: MediaType,
    tabRowItems: List<TabRowItem<ListStatus>>,
    navController: NavController
) {
    val pagerState = rememberPagerState()
    val coroutineScope = rememberCoroutineScope()

    Column {
        ScrollableTabRow(
            selectedTabIndex = pagerState.currentPage,
            edgePadding = 16.dp,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    modifier = Modifier
                        .tabIndicatorOffset(tabPositions[pagerState.currentPage])
                        .clip(RoundedCornerShape(topStartPercent = 100, topEndPercent = 100)),
                    height = 3.dp,
                )
            }
        ) {
            tabRowItems.forEachIndexed { index, item ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = { coroutineScope.launch { pagerState.animateScrollToPage(index) } },
                    text = { Text(text = item.value.localized()) },
                    unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        HorizontalPager(
            pageCount = tabRowItems.size,
            state = pagerState,
            beyondBoundsPageCount = 0,
            key = { tabRowItems[it].value }
        ) {
            UserMediaListView(
                mediaType = mediaType,
                status = tabRowItems[it].value,
                navController = navController
            )
        }//:Pager
    }//:Column
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun UserMediaListView(
    mediaType: MediaType,
    status: ListStatus,
    navController: NavController
) {
    val viewModel: UserMediaListViewModel = viewModel(key = status.value) { UserMediaListViewModel(mediaType, status) }
    val pullRefreshState = rememberPullRefreshState(viewModel.isLoading, { viewModel.getUserList() })
    val listState = rememberLazyListState()

    Box(
        modifier = Modifier
            .clipToBounds()
            .pullRefresh(pullRefreshState)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxHeight(),
            state = listState
        ) {
            if (viewModel.mediaType == MediaType.ANIME) {
                items(viewModel.animeList) { item ->
                    UserMediaListItem(
                        imageUrl = item.node.mainPicture?.large,
                        title = item.node.title,
                        score = item.listStatus?.score,
                        mediaFormat = item.node.mediaType,
                        mediaStatus = item.node.status,
                        userProgress = item.listStatus?.numEpisodesWatched,
                        totalProgress = item.node.numEpisodes,
                        listStatus = status,
                        onClick = {
                            navController.navigate("details/ANIME/${item.node.id}")
                        }
                    )
                }
            } else {
                items(viewModel.mangaList) { item ->
                    UserMediaListItem(
                        imageUrl = item.node.mainPicture?.large,
                        title = item.node.title,
                        score = item.listStatus?.score,
                        mediaFormat = item.node.mediaType,
                        mediaStatus = item.node.status,
                        userProgress = item.listStatus?.numChaptersRead,
                        totalProgress = item.node.numChapters,
                        listStatus = status,
                        onClick = {
                            navController.navigate("details/MANGA/${item.node.id}")
                        }
                    )
                }
            }
        }

        PullRefreshIndicator(viewModel.isLoading, pullRefreshState, Modifier.align(Alignment.TopCenter))
    }//:Box

    listState.OnBottomReached(buffer = 3) {
        if (!viewModel.isLoading && viewModel.hasNextPage) {
            viewModel.getUserList(viewModel.nextPage)
        }
    }

    LaunchedEffect(status) {
        if (!viewModel.isLoading) viewModel.getUserList()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserMediaListItem(
    imageUrl: String?,
    title: String,
    score: Int?,
    mediaFormat: String?,
    mediaStatus: String?,
    userProgress: Int?,
    totalProgress: Int?,
    listStatus: ListStatus,
    onClick: () -> Unit
) {
    OutlinedCard(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Row(
            modifier = Modifier.height(MEDIA_POSTER_SMALL_HEIGHT.dp)
        ) {
            Box(
                contentAlignment = Alignment.BottomStart
            ) {
                MediaPoster(
                    url = imageUrl,
                    modifier = Modifier
                        .size(width = MEDIA_POSTER_SMALL_WIDTH.dp, height = MEDIA_POSTER_SMALL_HEIGHT.dp)
                )

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(topEnd = 8.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if ((score ?: 0) == 0) "─" else "$score",
                        modifier = Modifier.padding(start = 8.dp, top = 4.dp, end = 2.dp, bottom = 4.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Icon(
                        painter = painterResource(R.drawable.ic_round_star_24),
                        contentDescription = "star",
                        modifier = Modifier.padding(end = 4.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = title,
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        fontSize = 17.sp,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 2
                    )
                    Text(
                        text = "${mediaFormat?.mediaFormatLocalized()} • ${mediaStatus?.statusLocalized()}",
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, bottom = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = "${userProgress ?: 0}/${totalProgress ?: 0}",
                        )

                        if (listStatus == ListStatus.WATCHING || listStatus == ListStatus.READING) {
                            OutlinedButton(onClick = { /*TODO*/ }) {
                                Text(text = stringResource(R.string.plus_one))
                            }
                        }
                    }

                    LinearProgressIndicator(
                        progress = calculateProgressBarValue(userProgress, totalProgress),
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }//:Column
        }//:Row
    }//:Card
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun UserMediaListHostPreview() {
    val animeTabs = remember {
        listStatusAnimeValues().map { TabRowItem(
            value = it,
            title = it.value
        ) }
    }

    MoeListTheme {
        UserMediaListHostView(
            mediaType = MediaType.ANIME,
            tabRowItems = animeTabs,
            navController = rememberNavController()
        )
    }
}

@Preview
@Composable
fun UserMediaListItemPreview() {
    MoeListTheme {
        UserMediaListItem(
            imageUrl = null,
            title = "This is a large anime or manga title",
            score = null,
            mediaFormat = "tv",
            mediaStatus = "currently_airing",
            userProgress = 4,
            totalProgress = 24,
            listStatus = ListStatus.WATCHING,
            onClick = { }
        )
    }
}