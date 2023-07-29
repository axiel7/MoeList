package com.axiel7.moelist.uicompose.search

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.anime.AnimeList
import com.axiel7.moelist.data.model.anime.seasonYearText
import com.axiel7.moelist.data.model.manga.MangaList
import com.axiel7.moelist.data.model.media.BaseMediaList
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.data.model.media.durationText
import com.axiel7.moelist.data.model.media.mediaFormatLocalized
import com.axiel7.moelist.data.model.media.totalDuration
import com.axiel7.moelist.data.model.media.userPreferredTitle
import com.axiel7.moelist.uicompose.composables.OnBottomReached
import com.axiel7.moelist.uicompose.composables.media.MediaItemDetailed
import com.axiel7.moelist.uicompose.composables.media.MediaItemDetailedPlaceholder
import com.axiel7.moelist.uicompose.theme.MoeListTheme
import com.axiel7.moelist.utils.ContextExtensions.showToast
import com.axiel7.moelist.utils.NumExtensions.toStringPositiveValueOrNull
import com.axiel7.moelist.utils.NumExtensions.toStringPositiveValueOrUnknown

const val SEARCH_DESTINATION = "search"

@Composable
fun SearchHostView(
    padding: PaddingValues,
    navigateToMediaDetails: (MediaType, Int) -> Unit,
) {
    var query by remember { mutableStateOf("") }
    val performSearch = remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .padding(top = padding.calculateTopPadding())
            .fillMaxWidth()
    ) {
        TextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(text = stringResource(R.string.search)) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = { performSearch.value = true }
            ),
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
            )
        )
        SearchView(
            query = query,
            performSearch = performSearch,
            showAsGrid = true,
            contentPadding = PaddingValues(bottom = padding.calculateBottomPadding()),
            navigateToMediaDetails = navigateToMediaDetails
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchView(
    query: String,
    performSearch: MutableState<Boolean>,
    showAsGrid: Boolean,
    contentPadding: PaddingValues = PaddingValues(),
    navigateToMediaDetails: (MediaType, Int) -> Unit,
) {
    val context = LocalContext.current
    val viewModel: SearchViewModel = viewModel()
    var mediaType by remember { mutableStateOf(MediaType.ANIME) }
    val shouldShowPlaceholder = query.isNotBlank() && viewModel.mediaList.isEmpty()

    LaunchedEffect(viewModel.message) {
        if (viewModel.showMessage) {
            context.showToast(viewModel.message)
            viewModel.showMessage = false
        }
    }

    LaunchedEffect(mediaType, performSearch.value) {
        if (query.isNotBlank() && performSearch.value) {
            viewModel.search(
                mediaType = mediaType,
                query = query
            )
            performSearch.value = false
        }
    }

    fun onLoadMore() {
        if (!viewModel.isLoading && viewModel.hasNextPage) {
            viewModel.search(
                mediaType = mediaType,
                query = query,
                page = viewModel.nextPage
            )
        }
    }

    @Composable
    fun FilterRow() {
        Row {
            FilterChip(
                selected = mediaType == MediaType.ANIME,
                onClick = {
                    mediaType = MediaType.ANIME
                    if (query.isNotBlank()) performSearch.value = true
                },
                label = { Text(text = stringResource(R.string.anime)) },
                modifier = Modifier.padding(start = 8.dp),
                leadingIcon = {
                    if (mediaType == MediaType.ANIME) {
                        Icon(
                            painter = painterResource(R.drawable.round_check_24),
                            contentDescription = "check"
                        )
                    }
                }
            )
            FilterChip(
                selected = mediaType == MediaType.MANGA,
                onClick = {
                    mediaType = MediaType.MANGA
                    if (query.isNotBlank()) performSearch.value = true
                },
                label = { Text(text = stringResource(R.string.manga)) },
                modifier = Modifier.padding(start = 8.dp),
                leadingIcon = {
                    if (mediaType == MediaType.MANGA) {
                        Icon(
                            painter = painterResource(R.drawable.round_check_24),
                            contentDescription = "check"
                        )
                    }
                }
            )
        }
    }

    @Composable
    fun ItemView(item: BaseMediaList) {
        MediaItemDetailed(
            title = item.node.userPreferredTitle(),
            imageUrl = item.node.mainPicture?.large,
            subtitle1 = {
                Text(
                    text = buildString {
                        append(item.node.mediaType?.mediaFormatLocalized())
                        if (item.node.totalDuration().toStringPositiveValueOrNull() != null) {
                            append(" (${item.node.durationText()})")
                        }
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            subtitle2 = {
                Text(
                    text = when (item) {
                        is AnimeList -> item.node.startSeason.seasonYearText()
                        is MangaList -> item.node.startDate ?: stringResource(R.string.unknown)
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
                    text = item.node.mean.toStringPositiveValueOrUnknown(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            onClick = {
                navigateToMediaDetails(mediaType, item.node.id)
            }
        )
    }

    @Composable
    fun NoResultsText() {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.no_results),
                modifier = Modifier.padding(16.dp)
            )
        }
    }

    if (showAsGrid) {
        val listState = rememberLazyGridState()
        listState.OnBottomReached(buffer = 4) {
            onLoadMore()
        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxWidth(),
            state = listState,
            contentPadding = contentPadding
        ) {
            item(
                span = { GridItemSpan(maxLineSpan) }
            ) {
                FilterRow()
            }
            items(
                items = viewModel.mediaList,
                contentType = { it.node }
            ) {
                ItemView(item = it)
            }
            if (shouldShowPlaceholder) {
                if (viewModel.isLoading) {
                    items(6) {
                        MediaItemDetailedPlaceholder()
                    }
                } else if (performSearch.value) {
                    item(
                        span = { GridItemSpan(maxLineSpan) }
                    ) {
                        NoResultsText()
                    }
                }
            }
        }
    } else {
        val listState = rememberLazyListState()
        listState.OnBottomReached(buffer = 3) {
            onLoadMore()
        }
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            state = listState,
            contentPadding = contentPadding
        ) {
            item { FilterRow() }
            items(
                items = viewModel.mediaList,
                contentType = { it.node }
            ) {
                ItemView(item = it)
            }
            if (shouldShowPlaceholder) {
                if (viewModel.isLoading) {
                    items(10) {
                        MediaItemDetailedPlaceholder()
                    }
                } else if (performSearch.value) {
                    item {
                        NoResultsText()
                    }
                }
            }
        }//: LazyColumn
    }
}

@Preview(showBackground = true)
@Composable
fun SearchPreview() {
    MoeListTheme {
        SearchView(
            query = "one",
            performSearch = remember { mutableStateOf(false) },
            showAsGrid = false,
            navigateToMediaDetails = { _, _ -> }
        )
    }
}