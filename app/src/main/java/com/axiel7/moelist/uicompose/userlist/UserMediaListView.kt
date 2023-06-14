package com.axiel7.moelist.uicompose.userlist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.axiel7.moelist.App
import com.axiel7.moelist.R
import com.axiel7.moelist.data.datastore.PreferencesDataStore.LIST_DISPLAY_MODE_PREFERENCE_KEY
import com.axiel7.moelist.data.datastore.PreferencesDataStore.rememberPreference
import com.axiel7.moelist.data.model.anime.AnimeNode
import com.axiel7.moelist.data.model.manga.MyMangaListStatus
import com.axiel7.moelist.data.model.manga.UserMangaList
import com.axiel7.moelist.data.model.manga.isUsingVolumeProgress
import com.axiel7.moelist.data.model.media.*
import com.axiel7.moelist.uicompose.base.ListMode
import com.axiel7.moelist.uicompose.composables.*
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
    modifier: Modifier = Modifier,
    navigateToMediaDetails: (MediaType, Int) -> Unit,
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

    if (statusSheetState.isVisible) {
        ListStatusSheet(
            mediaType = mediaType,
            selectedStatus = selectedStatus,
            sheetState = statusSheetState
        )
    }

    Scaffold(
        modifier = modifier,
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
    ) { padding ->
        UserMediaListView(
            mediaType = mediaType,
            status = selectedStatus.value,
            modifier = Modifier.padding(padding),
            nestedScrollConnection = nestedScrollConnection,
            navigateToMediaDetails = navigateToMediaDetails,
        )
    }//:Scaffold
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun UserMediaListView(
    mediaType: MediaType,
    status: ListStatus,
    modifier: Modifier = Modifier,
    nestedScrollConnection: NestedScrollConnection,
    navigateToMediaDetails: (MediaType, Int) -> Unit,
) {
    val context = LocalContext.current
    val viewModel: UserMediaListViewModel = viewModel(key = status.value) {
        UserMediaListViewModel(mediaType, status)
    }
    val pullRefreshState = rememberPullRefreshState(viewModel.isLoading, { viewModel.getUserList() })
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()
    val listDisplayMode by rememberPreference(LIST_DISPLAY_MODE_PREFERENCE_KEY, App.listDisplayMode.value)

    listState.OnBottomReached(buffer = 3) {
        if (!viewModel.isLoading && viewModel.hasNextPage) {
            viewModel.getUserList(viewModel.nextPage)
        }
    }

    if (viewModel.openSortDialog) {
        MediaListSortDialog(viewModel = viewModel)
    }

    if (sheetState.isVisible) {
        EditMediaSheet(
            coroutineScope = coroutineScope,
            sheetState = sheetState,
            mediaViewModel = viewModel
        )
    }

    LaunchedEffect(viewModel.message) {
        if (viewModel.showMessage) {
            context.showToast(viewModel.message)
            viewModel.showMessage = false
        }
    }

    LaunchedEffect(viewModel.listSort, status) {
        if (!viewModel.isLoading && viewModel.nextPage == null && !viewModel.loadedAllPages)
            viewModel.getUserList()
    }

    Box(
        modifier = modifier
            .clipToBounds()
            .pullRefresh(pullRefreshState)
            .fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopStart)
                .nestedScroll(nestedScrollConnection),
            state = listState,
            contentPadding = PaddingValues(vertical = 8.dp)
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
            when (listDisplayMode) {
                ListMode.STANDARD.value -> {
                    items(
                        items = viewModel.mediaList,
                        key = { it.node.id },
                        contentType = { it.node }
                    ) { item ->
                        StandardUserMediaListItem(
                            imageUrl = item.node.mainPicture?.large,
                            title = item.node.userPreferredTitle(),
                            score = item.listStatus?.score,
                            mediaType = mediaType,
                            mediaFormat = item.node.mediaType,
                            mediaStatus = item.node.status,
                            broadcast = (item.node as? AnimeNode)?.broadcast,
                            userProgress = item.userProgress(),
                            totalProgress = item.totalProgress(),
                            isVolumeProgress = (item as? UserMangaList)?.listStatus?.isUsingVolumeProgress() ?: false,
                            listStatus = status,
                            onClick = {
                                navigateToMediaDetails(mediaType, item.node.id)
                            },
                            onLongClick = {
                                coroutineScope.launch {
                                    viewModel.onItemSelected(item)
                                    sheetState.show()
                                }
                            },
                            onClickPlus = {
                                val isVolumeProgress = (item as? UserMangaList)?.listStatus?.isUsingVolumeProgress() ?: false
                                viewModel.updateListItem(
                                    mediaId = item.node.id,
                                    progress = if (!isVolumeProgress) item.listStatus?.progress?.plus(1) else null,
                                    volumeProgress = if (isVolumeProgress) (item.listStatus as? MyMangaListStatus)
                                        ?.numVolumesRead?.plus(1) else null
                                )
                            }
                        )
                    }
                }
                ListMode.COMPACT.value -> {
                    items(
                        items = viewModel.mediaList,
                        key = { it.node.id },
                        contentType = { it.node }
                    ) { item ->
                        CompactUserMediaListItem(
                            imageUrl = item.node.mainPicture?.large,
                            title = item.node.userPreferredTitle(),
                            score = item.listStatus?.score,
                            mediaType = mediaType,
                            userProgress = item.userProgress(),
                            totalProgress = item.totalProgress(),
                            isVolumeProgress = (item as? UserMangaList)?.listStatus?.isUsingVolumeProgress() ?: false,
                            mediaStatus = item.node.status,
                            broadcast = (item.node as? AnimeNode)?.broadcast,
                            listStatus = status,
                            onClick = {
                                navigateToMediaDetails(mediaType, item.node.id)
                            },
                            onLongClick = {
                                coroutineScope.launch {
                                    viewModel.onItemSelected(item)
                                    sheetState.show()
                                }
                            },
                            onClickPlus = {
                                val isVolumeProgress = (item as? UserMangaList)?.listStatus?.isUsingVolumeProgress() ?: false
                                viewModel.updateListItem(
                                    mediaId = item.node.id,
                                    progress = if (!isVolumeProgress) item.listStatus?.progress?.plus(1) else null,
                                    volumeProgress = if (isVolumeProgress) (item.listStatus as? MyMangaListStatus)
                                        ?.numVolumesRead?.plus(1) else null
                                )
                            }
                        )
                    }
                }
                ListMode.MINIMAL.value -> {
                    items(
                        items = viewModel.mediaList,
                        key = { it.node.id },
                        contentType = { it.node }
                    ) { item ->
                        MinimalUserMediaListItem(
                            title = item.node.userPreferredTitle(),
                            mediaType = mediaType,
                            userProgress = item.userProgress(),
                            totalProgress = item.totalProgress(),
                            isVolumeProgress = (item as? UserMangaList)?.listStatus?.isUsingVolumeProgress() ?: false,
                            mediaStatus = item.node.status,
                            broadcast = (item.node as? AnimeNode)?.broadcast,
                            listStatus = status,
                            onClick = {
                                navigateToMediaDetails(mediaType, item.node.id)
                            },
                            onLongClick = {
                                coroutineScope.launch {
                                    viewModel.onItemSelected(item)
                                    sheetState.show()
                                }
                            },
                            onClickPlus = {
                                val isVolumeProgress = (item as? UserMangaList)?.listStatus?.isUsingVolumeProgress() ?: false
                                viewModel.updateListItem(
                                    mediaId = item.node.id,
                                    progress = if (!isVolumeProgress) item.listStatus?.progress?.plus(1) else null,
                                    volumeProgress = if (isVolumeProgress) (item.listStatus as? MyMangaListStatus)
                                        ?.numVolumesRead?.plus(1) else null
                                )
                            }
                        )
                    }
                }
            }
        }//:LazyColumn

        PullRefreshIndicator(
            refreshing = viewModel.isLoading,
            state = pullRefreshState,
            modifier = Modifier
                .padding(8.dp)
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
            navigateToMediaDetails = { _, _ -> }
        )
    }
}