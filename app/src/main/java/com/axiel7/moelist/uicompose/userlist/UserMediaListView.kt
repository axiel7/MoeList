package com.axiel7.moelist.uicompose.userlist

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import com.axiel7.moelist.App
import com.axiel7.moelist.data.datastore.PreferencesDataStore.GENERAL_LIST_STYLE_PREFERENCE_KEY
import com.axiel7.moelist.data.datastore.PreferencesDataStore.GRID_ITEMS_PER_ROW_PREFERENCE_KEY
import com.axiel7.moelist.data.datastore.PreferencesDataStore.USE_GENERAL_LIST_STYLE_PREFERENCE_KEY
import com.axiel7.moelist.data.datastore.PreferencesDataStore.rememberPreference
import com.axiel7.moelist.data.model.media.BaseMediaNode
import com.axiel7.moelist.data.model.media.BaseUserMediaList
import com.axiel7.moelist.data.model.media.ListType
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.uicompose.base.ListStyle
import com.axiel7.moelist.uicompose.composables.OnBottomReached
import com.axiel7.moelist.uicompose.composables.collapsable
import com.axiel7.moelist.uicompose.composables.media.MEDIA_POSTER_MEDIUM_WIDTH
import com.axiel7.moelist.uicompose.composables.pullrefresh.PullRefreshIndicator
import com.axiel7.moelist.uicompose.composables.pullrefresh.pullRefresh
import com.axiel7.moelist.uicompose.composables.pullrefresh.rememberPullRefreshState
import com.axiel7.moelist.uicompose.userlist.composables.CompactUserMediaListItem
import com.axiel7.moelist.uicompose.userlist.composables.CompactUserMediaListItemPlaceholder
import com.axiel7.moelist.uicompose.userlist.composables.GridUserMediaListItem
import com.axiel7.moelist.uicompose.userlist.composables.GridUserMediaListItemPlaceholder
import com.axiel7.moelist.uicompose.userlist.composables.MinimalUserMediaListItem
import com.axiel7.moelist.uicompose.userlist.composables.MinimalUserMediaListItemPlaceholder
import com.axiel7.moelist.uicompose.userlist.composables.SortChip
import com.axiel7.moelist.uicompose.userlist.composables.StandardUserMediaListItem
import com.axiel7.moelist.uicompose.userlist.composables.StandardUserMediaListItemPlaceholder

const val ANIME_LIST_DESTINATION = "anime_list"
const val MANGA_LIST_DESTINATION = "manga_list"

@Composable
fun UserMediaListView(
    viewModel: UserMediaListViewModel,
    listType: ListType,
    isCompactScreen: Boolean,
    modifier: Modifier = Modifier,
    nestedScrollConnection: NestedScrollConnection? = null,
    navigateToMediaDetails: (MediaType, Int) -> Unit,
    topBarHeightPx: Float,
    topBarOffsetY: Animatable<Float, AnimationVector1D>,
    contentPadding: PaddingValues = PaddingValues(),
    onShowEditSheet: (BaseUserMediaList<out BaseMediaNode>) -> Unit,
) {
    val layoutDirection = LocalLayoutDirection.current
    val pullRefreshState =
        rememberPullRefreshState(viewModel.isLoading, onRefresh = viewModel::refreshList)

    val useGeneralListStyle by rememberPreference(
        USE_GENERAL_LIST_STYLE_PREFERENCE_KEY,
        App.useGeneralListStyle
    )
    val generalListStyle by rememberPreference(
        GENERAL_LIST_STYLE_PREFERENCE_KEY,
        App.generalListStyle.value
    )

    val listStyle =
        if (useGeneralListStyle) generalListStyle else listType.styleGlobalAppVariable.value

    @Composable
    fun StandardItemView(item: BaseUserMediaList<out BaseMediaNode>) {
        StandardUserMediaListItem(
            item = item,
            listStatus = listType.status,
            onClick = {
                navigateToMediaDetails(listType.mediaType, item.node.id)
            },
            onLongClick = {
                onShowEditSheet(item)
            },
            onClickPlus = {
                viewModel.onUpdateProgress(item)
            }
        )
    }

    @Composable
    fun CompactItemView(item: BaseUserMediaList<out BaseMediaNode>) {
        CompactUserMediaListItem(
            item = item,
            listStatus = listType.status,
            onClick = {
                navigateToMediaDetails(listType.mediaType, item.node.id)
            },
            onLongClick = {
                onShowEditSheet(item)
            },
            onClickPlus = {
                viewModel.onUpdateProgress(item)
            }
        )
    }

    @Composable
    fun MinimalItemView(item: BaseUserMediaList<out BaseMediaNode>) {
        MinimalUserMediaListItem(
            item = item,
            listStatus = listType.status,
            onClick = {
                navigateToMediaDetails(listType.mediaType, item.node.id)
            },
            onLongClick = {
                onShowEditSheet(item)
            },
            onClickPlus = {
                viewModel.onUpdateProgress(item)
            }
        )
    }

    @Composable
    fun GridItemView(item: BaseUserMediaList<out BaseMediaNode>) {
        GridUserMediaListItem(
            item = item,
            onClick = {
                navigateToMediaDetails(listType.mediaType, item.node.id)
            },
            onLongClick = {
                onShowEditSheet(item)
            }
        )
    }

    Box(
        modifier = modifier
            .clipToBounds()
            .pullRefresh(pullRefreshState)
            .fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        val listModifier = Modifier
            .fillMaxWidth()
            .align(Alignment.TopStart)
            .then(
                if (nestedScrollConnection != null)
                    Modifier.nestedScroll(nestedScrollConnection)
                else Modifier
            )

        if (listStyle == ListStyle.GRID.value) {
            val itemsPerRow by rememberPreference(
                GRID_ITEMS_PER_ROW_PREFERENCE_KEY,
                App.gridItemsPerRow
            )
            val listState = rememberLazyGridState()
            listState.OnBottomReached(buffer = 6) {
                viewModel.onLoadMore()
            }
            LazyVerticalGrid(
                columns = if (itemsPerRow > 0) GridCells.Fixed(itemsPerRow)
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
                    bottom = 8.dp
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.Bottom),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
            ) {
                item(
                    span = { GridItemSpan(maxCurrentLineSpan) }
                ) {
                    Row(modifier = Modifier.padding(horizontal = 8.dp)) {
                        SortChip(
                            text = viewModel.listSort.localized(),
                            onClick = { viewModel.openSortDialog = true },
                        )
                    }
                }
                items(
                    items = viewModel.mediaList,
                    key = { it.node.id },
                    contentType = { it.node }
                ) { item ->
                    GridItemView(item = item)
                }
                if (viewModel.isLoadingList) {
                    items(9, contentType = { it }) {
                        GridUserMediaListItemPlaceholder()
                    }
                }
            }
        } else if (isCompactScreen) {
            val listState = rememberLazyListState()
            listState.OnBottomReached(buffer = 3) {
                viewModel.onLoadMore()
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
                    bottom = 8.dp
                ),
            ) {
                item {
                    SortChip(
                        text = viewModel.listSort.localized(),
                        onClick = { viewModel.openSortDialog = true },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
                when (listStyle) {
                    ListStyle.STANDARD.value -> {
                        items(
                            items = viewModel.mediaList,
                            key = { it.node.id },
                            contentType = { it.node }
                        ) { item ->
                            StandardItemView(item = item)
                        }
                        if (viewModel.isLoadingList) {
                            items(5, contentType = { it }) {
                                StandardUserMediaListItemPlaceholder()
                            }
                        }
                    }

                    ListStyle.COMPACT.value -> {
                        items(
                            items = viewModel.mediaList,
                            key = { it.node.id },
                            contentType = { it.node }
                        ) { item ->
                            CompactItemView(item = item)
                        }
                        if (viewModel.isLoadingList) {
                            items(5, contentType = { it }) {
                                CompactUserMediaListItemPlaceholder()
                            }
                        }
                    }

                    ListStyle.MINIMAL.value -> {
                        items(
                            items = viewModel.mediaList,
                            key = { it.node.id },
                            contentType = { it.node }
                        ) { item ->
                            MinimalItemView(item = item)
                        }
                        if (viewModel.isLoadingList) {
                            items(5, contentType = { it }) {
                                MinimalUserMediaListItemPlaceholder()
                            }
                        }
                    }
                }
            }//:LazyColumn
        } else { // tablet ui
            val listState = rememberLazyGridState()
            listState.OnBottomReached(buffer = 3) {
                viewModel.onLoadMore()
            }
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                state = listState,
                contentPadding = PaddingValues(
                    start = contentPadding.calculateStartPadding(layoutDirection),
                    top = contentPadding.calculateTopPadding() + 8.dp,
                    end = contentPadding.calculateEndPadding(layoutDirection),
                    bottom = 8.dp
                ),
            ) {
                item(
                    span = { GridItemSpan(maxLineSpan) }
                ) {
                    Row(modifier = Modifier.padding(horizontal = 8.dp)) {
                        SortChip(
                            text = viewModel.listSort.localized(),
                            onClick = { viewModel.openSortDialog = true },
                        )
                    }
                }
                when (listStyle) {
                    ListStyle.STANDARD.value -> {
                        items(
                            items = viewModel.mediaList,
                            key = { it.node.id },
                            contentType = { it.node }
                        ) { item ->
                            StandardItemView(item = item)
                        }
                        if (viewModel.isLoadingList) {
                            items(5, contentType = { it }) {
                                StandardUserMediaListItemPlaceholder()
                            }
                        }
                    }

                    ListStyle.COMPACT.value -> {
                        items(
                            items = viewModel.mediaList,
                            key = { it.node.id },
                            contentType = { it.node }
                        ) { item ->
                            CompactItemView(item = item)
                        }
                        if (viewModel.isLoadingList) {
                            items(5, contentType = { it }) {
                                CompactUserMediaListItemPlaceholder()
                            }
                        }
                    }

                    ListStyle.MINIMAL.value -> {
                        items(
                            items = viewModel.mediaList,
                            key = { it.node.id },
                            contentType = { it.node }
                        ) { item ->
                            MinimalItemView(item = item)
                        }
                        if (viewModel.isLoadingList) {
                            items(5, contentType = { it }) {
                                MinimalUserMediaListItemPlaceholder()
                            }
                        }
                    }
                }
            }
        }

        PullRefreshIndicator(
            refreshing = viewModel.isLoading,
            state = pullRefreshState,
            modifier = Modifier
                .padding(top = contentPadding.calculateTopPadding())
                .align(Alignment.TopCenter)
        )
    }//:Box
}