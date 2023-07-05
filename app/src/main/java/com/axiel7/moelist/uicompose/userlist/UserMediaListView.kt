package com.axiel7.moelist.uicompose.userlist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.axiel7.moelist.App
import com.axiel7.moelist.R
import com.axiel7.moelist.data.datastore.PreferencesDataStore.GENERAL_LIST_STYLE_PREFERENCE_KEY
import com.axiel7.moelist.data.datastore.PreferencesDataStore.GRID_ITEMS_PER_ROW_PREFERENCE_KEY
import com.axiel7.moelist.data.datastore.PreferencesDataStore.USE_GENERAL_LIST_STYLE_PREFERENCE_KEY
import com.axiel7.moelist.data.datastore.PreferencesDataStore.rememberPreference
import com.axiel7.moelist.data.model.anime.AnimeNode
import com.axiel7.moelist.data.model.manga.MyMangaListStatus
import com.axiel7.moelist.data.model.manga.UserMangaList
import com.axiel7.moelist.data.model.manga.isUsingVolumeProgress
import com.axiel7.moelist.data.model.media.ListStatus
import com.axiel7.moelist.data.model.media.ListType
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.data.model.media.animeListSortItems
import com.axiel7.moelist.data.model.media.icon
import com.axiel7.moelist.data.model.media.listStatusValues
import com.axiel7.moelist.data.model.media.localized
import com.axiel7.moelist.data.model.media.mangaListSortItems
import com.axiel7.moelist.data.model.media.totalProgress
import com.axiel7.moelist.data.model.media.userPreferredTitle
import com.axiel7.moelist.data.model.media.userProgress
import com.axiel7.moelist.uicompose.base.ListStyle
import com.axiel7.moelist.uicompose.composables.MEDIA_POSTER_MEDIUM_WIDTH
import com.axiel7.moelist.uicompose.composables.OnBottomReached
import com.axiel7.moelist.uicompose.composables.collapsable
import com.axiel7.moelist.uicompose.details.EditMediaSheet
import com.axiel7.moelist.uicompose.theme.MoeListTheme
import com.axiel7.moelist.utils.ContextExtensions.showToast
import kotlinx.coroutines.launch

const val ANIME_LIST_DESTINATION = "anime_list"
const val MANGA_LIST_DESTINATION = "manga_list"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserMediaListHostView(
    mediaType: MediaType,
    navigateToMediaDetails: (MediaType, Int) -> Unit,
    topBarHeightPx: Float,
    topBarOffsetY: Animatable<Float, AnimationVector1D>,
    padding: PaddingValues,
) {
    val scope = rememberCoroutineScope()
    val selectedStatus = rememberSaveable { mutableStateOf(listStatusValues(mediaType)[0]) }
    val statusSheetState = rememberModalBottomSheetState()
    var isFabVisible by remember { mutableStateOf(true) }
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (available.y < -1) isFabVisible = false
                else if (available.y > 1) isFabVisible = true
                return Offset.Zero
            }
        }
    }
    val listType by remember {
        derivedStateOf { ListType(selectedStatus.value, mediaType) }
    }

    if (statusSheetState.isVisible) {
        ListStatusSheet(
            mediaType = mediaType,
            selectedStatus = selectedStatus,
            sheetState = statusSheetState
        )
    }

    Scaffold(
        modifier = Modifier.padding(
            bottom = padding.calculateBottomPadding()
        ),
        floatingActionButton = {
            AnimatedVisibility(
                visible = isFabVisible,
                modifier = Modifier.sizeIn(minWidth = 80.dp, minHeight = 56.dp),
                enter = slideInVertically(initialOffsetY = { it * 2 }),
                exit = slideOutVertically(targetOffsetY = { it * 2 }),
            ) {
                ExtendedFloatingActionButton(
                    onClick = { scope.launch { statusSheetState.show() } }
                ) {
                    Icon(
                        painter = painterResource(selectedStatus.value.icon()),
                        contentDescription = "status",
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(text = selectedStatus.value.localized())
                }
            }
        },
        contentWindowInsets = WindowInsets.systemBars
            .only(WindowInsetsSides.Horizontal)
    ) { childPadding ->
        UserMediaListView(
            listType = listType,
            modifier = Modifier.padding(childPadding),
            nestedScrollConnection = nestedScrollConnection,
            navigateToMediaDetails = navigateToMediaDetails,
            topBarHeightPx = topBarHeightPx,
            topBarOffsetY = topBarOffsetY,
            contentPadding = padding
        )
    }//:Scaffold
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun UserMediaListView(
    listType: ListType,
    modifier: Modifier = Modifier,
    nestedScrollConnection: NestedScrollConnection? = null,
    navigateToMediaDetails: (MediaType, Int) -> Unit,
    topBarHeightPx: Float,
    topBarOffsetY: Animatable<Float, AnimationVector1D>,
    contentPadding: PaddingValues = PaddingValues()
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val layoutDirection = LocalLayoutDirection.current
    val viewModel = viewModel(key = listType.toString()) {
        UserMediaListViewModel(listType)
    }
    val pullRefreshState = rememberPullRefreshState(viewModel.isLoading, { viewModel.getUserList() })
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()
    val bottomBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    val useGeneralListStyle by rememberPreference(USE_GENERAL_LIST_STYLE_PREFERENCE_KEY, App.useGeneralListStyle)
    val generalListStyle by rememberPreference(GENERAL_LIST_STYLE_PREFERENCE_KEY, App.generalListStyle.value)

    val listStyle = if (useGeneralListStyle) generalListStyle else viewModel.listTypeStyle

    if (viewModel.openSortDialog) {
        MediaListSortDialog(viewModel = viewModel)
    }

    if (sheetState.isVisible) {
        EditMediaSheet(
            coroutineScope = coroutineScope,
            sheetState = sheetState,
            mediaViewModel = viewModel,
            bottomPadding = bottomBarPadding
        )
    }

    LaunchedEffect(viewModel.message) {
        if (viewModel.showMessage) {
            context.showToast(viewModel.message)
            viewModel.showMessage = false
        }
    }

    LaunchedEffect(viewModel.listSort, listType) {
        if (!viewModel.isLoading && viewModel.nextPage == null && !viewModel.loadedAllPages)
            viewModel.getUserList()
    }

    Box(
        modifier = modifier
            .clipToBounds()
            .pullRefresh(pullRefreshState)
            .fillMaxSize()
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
            val itemsPerRow by rememberPreference(GRID_ITEMS_PER_ROW_PREFERENCE_KEY, App.gridItemsPerRow)
            val listState = rememberLazyGridState()
            if (!viewModel.isLoadingList) {
                listState.OnBottomReached(buffer = 3) {
                    if (viewModel.hasNextPage) {
                        viewModel.getUserList(viewModel.nextPage)
                    }
                }
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
                    Row {
                        AssistChip(
                            onClick = { viewModel.openSortDialog = true },
                            label = { Text(text = viewModel.listSort.localized()) },
                            modifier = Modifier.padding(horizontal = 8.dp),
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(R.drawable.ic_round_sort_24),
                                    contentDescription = stringResource(R.string.sort_by)
                                )
                            }
                        )
                    }
                }
                items(
                    items = viewModel.mediaList,
                    key = { it.node.id },
                    contentType = { it.node }
                ) { item ->
                    GridUserMediaListItem(
                        imageUrl = item.node.mainPicture?.large,
                        title = item.node.userPreferredTitle(),
                        score = item.listStatus?.score,
                        mediaStatus = item.node.status,
                        broadcast = (item.node as? AnimeNode)?.broadcast,
                        userProgress = item.userProgress(),
                        totalProgress = item.totalProgress(),
                        isVolumeProgress = (item as? UserMangaList)?.listStatus?.isUsingVolumeProgress()
                            ?: false,
                        onClick = {
                            navigateToMediaDetails(listType.mediaType, item.node.id)
                        },
                        onLongClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            coroutineScope.launch {
                                viewModel.onItemSelected(item)
                                sheetState.show()
                            }
                        }
                    )
                }
                if (viewModel.isLoadingList) {
                    items(9, contentType = { it }) {
                        GridUserMediaListItemPlaceholder()
                    }
                }
            }
        } else {
            val listState = rememberLazyListState()
            if (!viewModel.isLoadingList) {
                listState.OnBottomReached(buffer = 3) {
                    if (viewModel.hasNextPage) {
                        viewModel.getUserList(viewModel.nextPage)
                    }
                }
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
                    AssistChip(
                        onClick = { viewModel.openSortDialog = true },
                        label = { Text(text = viewModel.listSort.localized()) },
                        modifier = Modifier.padding(horizontal = 16.dp),
                        leadingIcon = {
                            Icon(
                                painter = painterResource(R.drawable.ic_round_sort_24),
                                contentDescription = stringResource(R.string.sort_by)
                            )
                        }
                    )
                }
                when (listStyle) {
                    ListStyle.STANDARD.value -> {
                        items(
                            items = viewModel.mediaList,
                            key = { it.node.id },
                            contentType = { it.node }
                        ) { item ->
                            StandardUserMediaListItem(
                                imageUrl = item.node.mainPicture?.large,
                                title = item.node.userPreferredTitle(),
                                score = item.listStatus?.score,
                                mediaFormat = item.node.mediaType,
                                mediaStatus = item.node.status,
                                broadcast = (item.node as? AnimeNode)?.broadcast,
                                userProgress = item.userProgress(),
                                totalProgress = item.totalProgress(),
                                isVolumeProgress = (item as? UserMangaList)?.listStatus?.isUsingVolumeProgress()
                                    ?: false,
                                listStatus = listType.status,
                                onClick = {
                                    navigateToMediaDetails(listType.mediaType, item.node.id)
                                },
                                onLongClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    coroutineScope.launch {
                                        viewModel.onItemSelected(item)
                                        sheetState.show()
                                    }
                                },
                                onClickPlus = {
                                    val isVolumeProgress =
                                        (item as? UserMangaList)?.listStatus?.isUsingVolumeProgress()
                                            ?: false
                                    viewModel.updateListItem(
                                        mediaId = item.node.id,
                                        progress = if (!isVolumeProgress) item.listStatus?.progress?.plus(
                                            1
                                        ) else null,
                                        volumeProgress = if (isVolumeProgress) (item.listStatus as? MyMangaListStatus)
                                            ?.numVolumesRead?.plus(1) else null
                                    )
                                }
                            )
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
                            CompactUserMediaListItem(
                                imageUrl = item.node.mainPicture?.large,
                                title = item.node.userPreferredTitle(),
                                score = item.listStatus?.score,
                                userProgress = item.userProgress(),
                                totalProgress = item.totalProgress(),
                                isVolumeProgress = (item as? UserMangaList)?.listStatus?.isUsingVolumeProgress()
                                    ?: false,
                                mediaStatus = item.node.status,
                                broadcast = (item.node as? AnimeNode)?.broadcast,
                                listStatus = listType.status,
                                onClick = {
                                    navigateToMediaDetails(listType.mediaType, item.node.id)
                                },
                                onLongClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    coroutineScope.launch {
                                        viewModel.onItemSelected(item)
                                        sheetState.show()
                                    }
                                },
                                onClickPlus = {
                                    val isVolumeProgress =
                                        (item as? UserMangaList)?.listStatus?.isUsingVolumeProgress()
                                            ?: false
                                    viewModel.updateListItem(
                                        mediaId = item.node.id,
                                        progress = if (!isVolumeProgress) item.listStatus?.progress?.plus(
                                            1
                                        ) else null,
                                        volumeProgress = if (isVolumeProgress) (item.listStatus as? MyMangaListStatus)
                                            ?.numVolumesRead?.plus(1) else null
                                    )
                                }
                            )
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
                            MinimalUserMediaListItem(
                                title = item.node.userPreferredTitle(),
                                score = item.listStatus?.score,
                                userProgress = item.userProgress(),
                                totalProgress = item.totalProgress(),
                                isVolumeProgress = (item as? UserMangaList)?.listStatus?.isUsingVolumeProgress()
                                    ?: false,
                                mediaStatus = item.node.status,
                                broadcast = (item.node as? AnimeNode)?.broadcast,
                                listStatus = listType.status,
                                onClick = {
                                    navigateToMediaDetails(listType.mediaType, item.node.id)
                                },
                                onLongClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    coroutineScope.launch {
                                        viewModel.onItemSelected(item)
                                        sheetState.show()
                                    }
                                },
                                onClickPlus = {
                                    val isVolumeProgress =
                                        (item as? UserMangaList)?.listStatus?.isUsingVolumeProgress()
                                            ?: false
                                    viewModel.updateListItem(
                                        mediaId = item.node.id,
                                        progress = if (!isVolumeProgress) item.listStatus?.progress?.plus(
                                            1
                                        ) else null,
                                        volumeProgress = if (isVolumeProgress) (item.listStatus as? MyMangaListStatus)
                                            ?.numVolumesRead?.plus(1) else null
                                    )
                                }
                            )
                        }
                        if (viewModel.isLoadingList) {
                            items(5, contentType = { it }) {
                                MinimalUserMediaListItemPlaceholder()
                            }
                        }
                    }
                }
            }//:LazyColumn
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListStatusSheet(
    mediaType: MediaType,
    selectedStatus: MutableState<ListStatus>,
    sheetState: SheetState,
    onDismiss: () -> Unit = {},
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        windowInsets = WindowInsets.navigationBars
    ) {
        Column(
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            listStatusValues(mediaType).forEach {
                val isSelected = selectedStatus.value == it
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedStatus.value = it }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(it.icon()),
                        contentDescription = "check",
                        tint = if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = it.localized(),
                        modifier = Modifier.padding(start = 8.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun MediaListSortDialog(
    viewModel: UserMediaListViewModel
) {
    val configuration = LocalConfiguration.current
    val sortOptions = remember {
        if (viewModel.mediaType == MediaType.ANIME) animeListSortItems else mangaListSortItems
    }
    var selectedIndex by remember {
        mutableIntStateOf(sortOptions.indexOf(viewModel.listSort))
    }
    AlertDialog(
        onDismissRequest = { viewModel.openSortDialog = false },
        confirmButton = {
            TextButton(onClick = {
                viewModel.setSort(sortOptions[selectedIndex])
                viewModel.openSortDialog = false
            }) {
                Text(text = stringResource(R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = { viewModel.openSortDialog = false }) {
                Text(text = stringResource(R.string.cancel))
            }
        },
        title = { Text(text = stringResource(R.string.sort_by)) },
        text = {
            LazyColumn(
                modifier = Modifier.sizeIn(
                    maxHeight = (configuration.screenHeightDp - 48).dp
                )
            ) {
                itemsIndexed(sortOptions) { index, sort ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedIndex = index },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedIndex == index,
                            onClick = { selectedIndex = index }
                        )
                        Text(text = sort.localized())
                    }
                }
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun UserMediaListHostPreview() {
    MoeListTheme {
        UserMediaListHostView(
            mediaType = MediaType.ANIME,
            navigateToMediaDetails = { _, _ -> },
            topBarHeightPx = 0f,
            topBarOffsetY = remember { Animatable(0f) },
            padding = PaddingValues(),
        )
    }
}