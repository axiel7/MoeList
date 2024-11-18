package com.axiel7.moelist.ui.userlist

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed
import com.axiel7.moelist.Anilist.AnilistQuery
import com.axiel7.moelist.Anilist.secondsToDays
import com.axiel7.moelist.data.model.anime.AnimeNode
import com.axiel7.moelist.data.model.media.BaseMediaNode
import com.axiel7.moelist.data.model.media.BaseUserMediaList
import com.axiel7.moelist.data.model.media.ListStatus
import com.axiel7.moelist.ui.base.ListStyle
import com.axiel7.moelist.ui.base.navigation.NavActionManager
import com.axiel7.moelist.ui.composables.OnBottomReached
import com.axiel7.moelist.ui.composables.collapsable
import com.axiel7.moelist.ui.composables.media.MEDIA_POSTER_MEDIUM_WIDTH
import com.axiel7.moelist.ui.userlist.composables.CompactUserMediaListItem
import com.axiel7.moelist.ui.userlist.composables.CompactUserMediaListItemPlaceholder
import com.axiel7.moelist.ui.userlist.composables.GridUserMediaListItem
import com.axiel7.moelist.ui.userlist.composables.GridUserMediaListItemPlaceholder
import com.axiel7.moelist.ui.userlist.composables.MinimalUserMediaListItem
import com.axiel7.moelist.ui.userlist.composables.MinimalUserMediaListItemPlaceholder
import com.axiel7.moelist.ui.userlist.composables.RandomChip
import com.axiel7.moelist.ui.userlist.composables.SortChip
import com.axiel7.moelist.ui.userlist.composables.StandardUserMediaListItem
import com.axiel7.moelist.ui.userlist.composables.StandardUserMediaListItemPlaceholder
import kotlinx.coroutines.runBlocking

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserMediaListView(
    uiState: UserMediaListUiState,
    event: UserMediaListEvent?,
    navActionManager: NavActionManager,
    isCompactScreen: Boolean,
    modifier: Modifier = Modifier,
    nestedScrollConnection: NestedScrollConnection? = null,
    topBarHeightPx: Float = 0f,
    topBarOffsetY: Animatable<Float, AnimationVector1D> = Animatable(0f),
    contentPadding: PaddingValues = PaddingValues(),
    onShowEditSheet: (BaseUserMediaList<out BaseMediaNode>) -> Unit,
) {
    val layoutDirection = LocalLayoutDirection.current
    val haptic = LocalHapticFeedback.current
    val pullRefreshState = rememberPullToRefreshState()

    //add Airing NextEp No from AnilistApi
    //AddNextAiringEpInfo(uiState,event)
    //using AddNextAiringEpInfo_v2 at AniRepo

    @Composable
    fun StandardItemView(item: BaseUserMediaList<out BaseMediaNode>) {
        StandardUserMediaListItem(
            item = item,
            listStatus = uiState.listStatus,
            onClick = dropUnlessResumed {
                navActionManager.toMediaDetails(uiState.mediaType, item.node.id)
            },
            onLongClick = {
                onShowEditSheet(item)
            },
            onClickPlus = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                event?.onUpdateProgress(item)
            }
        )
    }

    @Composable
    fun CompactItemView(item: BaseUserMediaList<out BaseMediaNode>) {
        CompactUserMediaListItem(
            item = item,
            listStatus = uiState.listStatus,
            onClick = dropUnlessResumed {
                navActionManager.toMediaDetails(uiState.mediaType, item.node.id)
            },
            onLongClick = {
                onShowEditSheet(item)
            },
            onClickPlus = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                event?.onUpdateProgress(item)
            }
        )
    }

    @Composable
    fun MinimalItemView(item: BaseUserMediaList<out BaseMediaNode>) {
        MinimalUserMediaListItem(
            item = item,
            listStatus = uiState.listStatus,
            onClick = dropUnlessResumed {
                navActionManager.toMediaDetails(uiState.mediaType, item.node.id)
            },
            onLongClick = {
                onShowEditSheet(item)
            },
            onClickPlus = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                event?.onUpdateProgress(item)
            }
        )
    }

    @Composable
    fun GridItemView(item: BaseUserMediaList<out BaseMediaNode>) {
        GridUserMediaListItem(
            item = item,
            onClick = dropUnlessResumed {
                navActionManager.toMediaDetails(uiState.mediaType, item.node.id)
            },
            onLongClick = {
                onShowEditSheet(item)
            }
        )
    }

    PullToRefreshBox(
        isRefreshing = uiState.isLoading,
        onRefresh = { event?.refreshList() },
        modifier = modifier.fillMaxSize(),
        state = pullRefreshState,
    ) {
        val listModifier = Modifier
            .fillMaxWidth()
            .align(Alignment.TopStart)
            .then(
                if (nestedScrollConnection != null)
                    Modifier.nestedScroll(nestedScrollConnection)
                else Modifier
            )

        if (uiState.listStyle == ListStyle.GRID) {
            val listState = rememberLazyGridState()
            LazyVerticalGrid(
                columns = if (uiState.itemsPerRow.value > 0) GridCells.Fixed(uiState.itemsPerRow.value)
                else GridCells.Adaptive(minSize = (MEDIA_POSTER_MEDIUM_WIDTH + 8).dp),
                modifier = listModifier
                    .collapsable(
                        state = listState,
                        topBarHeightPx = topBarHeightPx,
                        topBarOffsetY = topBarOffsetY,
                    ),
                state = listState,
                contentPadding = PaddingValues(
                    start = contentPadding.calculateStartPadding(layoutDirection) + 8.dp,
                    top = contentPadding.calculateTopPadding() + 8.dp,
                    end = contentPadding.calculateEndPadding(layoutDirection) + 8.dp,
                    bottom = contentPadding.calculateBottomPadding() + 8.dp
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.Bottom),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
            ) {
                item(
                    span = { GridItemSpan(maxCurrentLineSpan) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SortChip(uiState, event)
                        if (uiState.showRandomButton) {
                            RandomChip(
                                onClick = { event?.getRandomIdOfList() }
                            )
                        }
                    }
                }
                items(
                    items = uiState.mediaList,
                    key = { it.node.id },
                    contentType = { it.node }
                ) { item ->
                    GridItemView(item = item)
                }
                if (uiState.isLoadingMore) {
                    items(9, contentType = { it }) {
                        GridUserMediaListItemPlaceholder()
                    }
                }
                item(contentType = { 0 }) {
                    if (uiState.canLoadMore) {
                        Box(modifier = Modifier.align(Alignment.Center)) {
                            CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                        LaunchedEffect(true) {
                            event?.loadMore()
                        }
                    }
                }
            }
        } else if (isCompactScreen) {
            val listState = rememberLazyListState()
            listState.OnBottomReached(buffer = 3) {
                event?.loadMore()
            }
            LazyColumn(
                modifier = listModifier
                    .collapsable(
                        state = listState,
                        topBarHeightPx = topBarHeightPx,
                        topBarOffsetY = topBarOffsetY,
                    ),
                state = listState,
                contentPadding = PaddingValues(
                    start = contentPadding.calculateStartPadding(layoutDirection),
                    top = contentPadding.calculateTopPadding() + 8.dp,
                    end = contentPadding.calculateEndPadding(layoutDirection),
                    bottom = contentPadding.calculateBottomPadding() + 8.dp
                ),
            ) {
                item {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SortChip(uiState, event)
                        if (uiState.showRandomButton) {
                            RandomChip(
                                onClick = { event?.getRandomIdOfList() }
                            )
                        }
                    }
                }
                when (uiState.listStyle) {
                    ListStyle.STANDARD -> {
                        items(
                            items = uiState.mediaList,
                            key = { it.node.id },
                            contentType = { it.node }
                        ) { item ->
                            StandardItemView(item = item)
                        }
                        if (uiState.isLoadingMore) {
                            items(5, contentType = { it }) {
                                StandardUserMediaListItemPlaceholder()
                            }
                        }
                    }

                    ListStyle.COMPACT -> {
                        items(
                            items = uiState.mediaList,
                            key = { it.node.id },
                            contentType = { it.node }
                        ) { item ->
                            CompactItemView(item = item)
                        }
                        if (uiState.isLoadingMore) {
                            items(5, contentType = { it }) {
                                CompactUserMediaListItemPlaceholder()
                            }
                        }
                    }

                    ListStyle.MINIMAL -> {
                        items(
                            items = uiState.mediaList,
                            key = { it.node.id },
                            contentType = { it.node }
                        ) { item ->
                            MinimalItemView(item = item)
                        }
                        if (uiState.isLoadingMore) {
                            items(5, contentType = { it }) {
                                MinimalUserMediaListItemPlaceholder()
                            }
                        }
                    }

                    else -> {}
                }
            }//:LazyColumn
        } else { // tablet ui
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(
                    start = contentPadding.calculateStartPadding(layoutDirection),
                    top = contentPadding.calculateTopPadding() + 8.dp,
                    end = contentPadding.calculateEndPadding(layoutDirection),
                    bottom = contentPadding.calculateBottomPadding() + 8.dp
                ),
            ) {
                item(
                    span = { GridItemSpan(maxLineSpan) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SortChip(uiState, event)
                        if (uiState.showRandomButton) {
                            RandomChip(
                                onClick = { event?.getRandomIdOfList() }
                            )
                        }
                    }
                }
                when (uiState.listStyle) {
                    ListStyle.STANDARD -> {
                        items(
                            items = uiState.mediaList,
                            key = { it.node.id },
                            contentType = { it.node }
                        ) { item ->
                            StandardItemView(item = item)
                        }
                        if (uiState.isLoadingMore) {
                            items(5, contentType = { it }) {
                                StandardUserMediaListItemPlaceholder()
                            }
                        }
                    }

                    ListStyle.COMPACT -> {
                        items(
                            items = uiState.mediaList,
                            key = { it.node.id },
                            contentType = { it.node }
                        ) { item ->
                            CompactItemView(item = item)
                        }
                        if (uiState.isLoadingMore) {
                            items(5, contentType = { it }) {
                                CompactUserMediaListItemPlaceholder()
                            }
                        }
                    }

                    ListStyle.MINIMAL -> {
                        items(
                            items = uiState.mediaList,
                            key = { it.node.id },
                            contentType = { it.node }
                        ) { item ->
                            MinimalItemView(item = item)
                        }
                        if (uiState.isLoadingMore) {
                            items(5, contentType = { it }) {
                                MinimalUserMediaListItemPlaceholder()
                            }
                        }
                    }

                    else -> {}
                }
                item(contentType = { 0 }) {
                    if (uiState.canLoadMore) {
                        Box(modifier = Modifier.align(Alignment.Center)) {
                            CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                        LaunchedEffect(true) {
                            event?.loadMore()
                        }
                    }
                }
            }
        }
    }//:Box
}



@Composable
private fun AddNextAiringEpInfo(uiState: UserMediaListUiState, event: UserMediaListEvent?  ) {

    if (uiState.listStatus != ListStatus.WATCHING)
        return

    val airingAnimes_idlist = uiState.mediaList.filter { it.isAiring }.map{ it.node.id }
    if (airingAnimes_idlist.isNullOrEmpty())
        return

    uiState.mediaList
        .filter {  it.isAiring }
        .forEach { (it.node as? AnimeNode)?.al_nextAiringEpisode = "AL loading...";  }

    Thread {
        println("alquery.getAiringInfo run. if this is run too much. cache it. ")

        // Perform network operation here
        runBlocking {
//            var alquery = AnilistQuery();
            var al_mediaList = AnilistQuery.GetAiringInfo_ToPoco_FromCache(airingAnimes_idlist)
            if (al_mediaList?.isEmpty() == true)
                return@runBlocking

            uiState.mediaList.filter { it.isAiring }.forEach { it ->

                // val broadcast = remember { (it.node as? AnimeNode)?.broadcast }
                if (!(it.node is AnimeNode))
                    return@runBlocking

                var _id = (it.node as? AnimeNode)?.id?.toLong()
                // (it.node as? AnimeNode)?.al_nextAiringEpisode = "test success"
                var it_AirInfo = al_mediaList?.firstOrNull { it2 -> it2.idMal == _id }?.nextAiringEpisode
                var str = """Ep ${it_AirInfo?.episode} in ${secondsToDays(it_AirInfo?.timeUntilAiring ?: Long.MAX_VALUE)} day(s) """
                //  runBlocking {}
                (it.node as? AnimeNode)?.al_nextAiringEpisode = str
            }

            //completed - Need to Trigger ui refresh. --help please
            //also added memory cache - if i pull to refresh.
            // next time it shows up.

        }



    }.start()


}





