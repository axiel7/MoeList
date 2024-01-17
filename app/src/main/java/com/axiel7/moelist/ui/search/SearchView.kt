package com.axiel7.moelist.ui.search

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.anime.AnimeList
import com.axiel7.moelist.data.model.manga.MangaList
import com.axiel7.moelist.data.model.media.BaseMediaList
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.ui.base.navigation.NavActionManager
import com.axiel7.moelist.ui.composables.BackIconButton
import com.axiel7.moelist.ui.composables.OnBottomReached
import com.axiel7.moelist.ui.composables.media.MediaItemDetailed
import com.axiel7.moelist.ui.composables.media.MediaItemDetailedPlaceholder
import com.axiel7.moelist.ui.theme.MoeListTheme
import com.axiel7.moelist.utils.ContextExtensions.showToast
import com.axiel7.moelist.utils.DateUtils.parseDateAndLocalize
import com.axiel7.moelist.utils.NumExtensions.toStringPositiveValueOrNull
import com.axiel7.moelist.utils.NumExtensions.toStringPositiveValueOrUnknown
import org.koin.androidx.compose.koinViewModel

@Composable
fun SearchHostView(
    isCompactScreen: Boolean,
    navActionManager: NavActionManager,
    padding: PaddingValues,
) {
    val viewModel: SearchViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var query by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(focusRequester) {
        focusRequester.requestFocus()
    }

    Column(
        modifier = Modifier
            .statusBarsPadding()
            .padding(top = padding.calculateTopPadding())
            .fillMaxWidth()
    ) {
        TextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            placeholder = { Text(text = stringResource(R.string.search)) },
            leadingIcon = {
                if (isCompactScreen) BackIconButton(onClick = navActionManager::goBack)
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = {
                    keyboardController?.hide()
                    viewModel.search(query)
                }
            ),
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = MaterialTheme.colorScheme.outlineVariant,
                unfocusedIndicatorColor = MaterialTheme.colorScheme.outlineVariant
            )
        )
        SearchViewContent(
            uiState = uiState,
            event = viewModel,
            query = query,
            isCompactScreen = isCompactScreen,
            navActionManager = navActionManager,
            contentPadding = PaddingValues(bottom = padding.calculateBottomPadding()),
        )
    }
}

@Composable
private fun SearchViewContent(
    uiState: SearchUiState,
    event: SearchEvent?,
    query: String,
    isCompactScreen: Boolean,
    navActionManager: NavActionManager,
    contentPadding: PaddingValues = PaddingValues(),
) {
    val context = LocalContext.current
    val shouldShowPlaceholder = query.isNotBlank() && uiState.mediaList.isEmpty()

    LaunchedEffect(uiState.message) {
        if (uiState.message != null) {
            context.showToast(uiState.message)
            event?.onMessageDisplayed()
        }
    }

    @Composable
    fun FilterRow() {
        Row {
            MediaType.entries.forEach {
                FilterChip(
                    selected = uiState.mediaType == it,
                    onClick = {
                        event?.onChangeMediaType(it)
                    },
                    label = { Text(text = it.localized()) },
                    modifier = Modifier.padding(start = 8.dp),
                    leadingIcon = {
                        if (uiState.mediaType == it) {
                            Icon(
                                painter = painterResource(R.drawable.round_check_24),
                                contentDescription = "check"
                            )
                        }
                    }
                )
            }
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
                        append(item.node.mediaFormat?.localized())
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
                        is AnimeList -> item.node.startSeason?.seasonYearText()
                            ?: stringResource(R.string.unknown)

                        is MangaList -> item.node.startDate?.parseDateAndLocalize()
                            ?: stringResource(R.string.unknown)
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
                navActionManager.toMediaDetails(uiState.mediaType, item.node.id)
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

    if (!isCompactScreen) {
        val listState = rememberLazyGridState()
        listState.OnBottomReached(buffer = 4) {
            event?.loadMore()
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
                items = uiState.mediaList,
                contentType = { it.node }
            ) {
                ItemView(item = it)
            }
            if (shouldShowPlaceholder) {
                if (uiState.isLoading) {
                    items(6) {
                        MediaItemDetailedPlaceholder()
                    }
                } else if (uiState.noResults) {
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
            event?.loadMore()
        }
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            state = listState,
            contentPadding = contentPadding
        ) {
            item { FilterRow() }
            items(
                items = uiState.mediaList,
                contentType = { it.node }
            ) {
                ItemView(item = it)
            }
            if (shouldShowPlaceholder) {
                if (uiState.isLoading) {
                    items(10) {
                        MediaItemDetailedPlaceholder()
                    }
                } else if (uiState.noResults) {
                    item {
                        NoResultsText()
                    }
                }
            }
        }//: LazyColumn
    }
}

@Preview
@Composable
fun SearchPreview() {
    MoeListTheme {
        Surface {
            SearchViewContent(
                uiState = SearchUiState(),
                event = null,
                query = "",
                isCompactScreen = false,
                navActionManager = NavActionManager.rememberNavActionManager()
            )
        }
    }
}