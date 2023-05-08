package com.axiel7.moelist.uicompose.userlist

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.axiel7.moelist.App
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.anime.AnimeNode
import com.axiel7.moelist.data.model.manga.MyMangaListStatus
import com.axiel7.moelist.data.model.manga.UserMangaList
import com.axiel7.moelist.data.model.manga.isUsingVolumeProgress
import com.axiel7.moelist.data.model.media.*
import com.axiel7.moelist.uicompose.base.ListMode
import com.axiel7.moelist.uicompose.base.TabRowItem
import com.axiel7.moelist.uicompose.composables.*
import com.axiel7.moelist.uicompose.details.EditMediaSheet
import com.axiel7.moelist.uicompose.theme.MoeListTheme
import com.axiel7.moelist.utils.ContextExtensions.showToast
import com.axiel7.moelist.data.datastore.PreferencesDataStore.LIST_DISPLAY_MODE_PREFERENCE
import com.axiel7.moelist.data.datastore.PreferencesDataStore.rememberPreference
import kotlinx.coroutines.launch

const val ANIME_LIST_DESTINATION = "anime_list"
const val MANGA_LIST_DESTINATION = "manga_list"

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UserMediaListHostView(
    mediaType: MediaType,
    navController: NavController
) {
    val pagerState = rememberPagerState()
    val coroutineScope = rememberCoroutineScope()
    val tabRowItems = remember {
        (if (mediaType == MediaType.ANIME) listStatusAnimeValues else listStatusMangaValues)
            .map { TabRowItem(
                value = it,
                title = it.value
            ) }
    }

    Column {
        ScrollableTabRow(
            selectedTabIndex = pagerState.currentPage,
            edgePadding = 16.dp,
            indicator = { tabPositions ->
                RoundedTabRowIndicator(tabPositions[pagerState.currentPage])
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

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun UserMediaListView(
    mediaType: MediaType,
    status: ListStatus,
    navController: NavController
) {
    val context = LocalContext.current
    val viewModel: UserMediaListViewModel = viewModel(key = status.value) {
        UserMediaListViewModel(mediaType, status)
    }
    val pullRefreshState = rememberPullRefreshState(viewModel.isLoading, { viewModel.getUserList() })
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()
    val listDisplayMode by rememberPreference(LIST_DISPLAY_MODE_PREFERENCE, App.listDisplayMode.value)

    Box(
        modifier = Modifier
            .clipToBounds()
            .pullRefresh(pullRefreshState)
            .fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopStart),
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
            items(viewModel.mediaList,
                key = { it.node.id },
                contentType = { it.node }
            ) { item ->
                when (listDisplayMode) {
                    ListMode.STANDARD.value -> {
                        StandardUserMediaListItem(
                            imageUrl = item.node.mainPicture?.large,
                            title = item.node.title,
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
                                navController.navigate("details/${mediaType.value}/${item.node.id}")
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
                    ListMode.COMPACT.value -> {
                        CompactUserMediaListItem(
                            imageUrl = item.node.mainPicture?.large,
                            title = item.node.title,
                            score = item.listStatus?.score,
                            mediaType = mediaType,
                            userProgress = item.userProgress(),
                            totalProgress = item.totalProgress(),
                            isVolumeProgress = (item as? UserMangaList)?.listStatus?.isUsingVolumeProgress() ?: false,
                            mediaStatus = item.node.status,
                            broadcast = (item.node as? AnimeNode)?.broadcast,
                            listStatus = status,
                            onClick = {
                                navController.navigate("details/${mediaType.value}/${item.node.id}")
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
                    ListMode.MINIMAL.value -> {
                        MinimalUserMediaListItem(
                            title = item.node.title,
                            mediaType = mediaType,
                            userProgress = item.userProgress(),
                            totalProgress = item.totalProgress(),
                            isVolumeProgress = (item as? UserMangaList)?.listStatus?.isUsingVolumeProgress() ?: false,
                            mediaStatus = item.node.status,
                            broadcast = (item.node as? AnimeNode)?.broadcast,
                            listStatus = status,
                            onClick = {
                                navController.navigate("details/${mediaType.value}/${item.node.id}")
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
            }//:items
        }//:LazyColumn

        PullRefreshIndicator(
            refreshing = viewModel.isLoading,
            state = pullRefreshState,
            modifier = Modifier
                .padding(8.dp)
                .align(Alignment.TopCenter)
        )
    }//:Box

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

    LaunchedEffect(viewModel.listSort) {
        if (!viewModel.isLoading && viewModel.nextPage == null && !viewModel.loadedAllPages)
            viewModel.getUserList()
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
        mutableStateOf(sortOptions.indexOf(viewModel.listSort))
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
            navController = rememberNavController()
        )
    }
}