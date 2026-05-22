package com.axiel7.moelist.ui.search

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.SearchHistory
import com.axiel7.moelist.data.model.anime.AnimeList
import com.axiel7.moelist.data.model.manga.MangaList
import com.axiel7.moelist.data.model.media.BaseMediaList
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.ui.base.navigation.NavActionManager
import com.axiel7.moelist.ui.composables.EmptyState
import com.axiel7.moelist.ui.composables.ErrorState
import com.axiel7.moelist.ui.composables.LoadingState
import com.axiel7.moelist.ui.composables.OnBottomReached
import com.axiel7.moelist.ui.composables.media.MediaItemDetailed
import com.axiel7.moelist.ui.composables.media.MediaItemDetailedPlaceholder
import com.axiel7.moelist.ui.theme.MoeListTheme
import com.axiel7.moelist.utils.ContextExtensions.showToast
import com.axiel7.moelist.utils.DateUtils.parseDateAndLocalize
import com.axiel7.moelist.utils.NumExtensions.toStringPositiveValueOrNull
import com.axiel7.moelist.utils.NumExtensions.toStringPositiveValueOrUnknown
import com.axiel7.moelist.utils.UNKNOWN_CHAR
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SearchHostView(
    isCompactScreen: Boolean,
    navActionManager: NavActionManager,
    padding: PaddingValues,
) {
    val viewModel: SearchViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var query by rememberSaveable { mutableStateOf("") }
    var active by rememberSaveable { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = Modifier
            .statusBarsPadding()
            .padding(top = padding.calculateTopPadding())
            .fillMaxSize()
    ) {
        SearchBar(
            inputField = {
                SearchBarDefaults.InputField(
                    query = query,
                    onQueryChange = { 
                        query = it
                        viewModel.search(it)
                    },
                    onSearch = {
                        viewModel.onSaveSearchHistory(it)
                        keyboardController?.hide()
                    },
                    expanded = active,
                    onExpandedChange = { active = it },
                    placeholder = { Text(text = stringResource(R.string.search)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = null
                        )
                    },
                    trailingIcon = {
                        if (active) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "clear",
                                modifier = Modifier.combinedClickable(
                                    onClick = {
                                        if (query.isNotEmpty()) {
                                            query = ""
                                            viewModel.search("")
                                        }
                                        else active = false
                                    }
                                )
                            )
                        }
                    },
                )
            },
            expanded = active,
            onExpandedChange = { active = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = if (active) 0.dp else 16.dp),
            colors = SearchBarDefaults.colors(
                containerColor = if (active) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceContainerHigh,
            ),
            shape = if (active) SearchBarDefaults.fullScreenShape else MaterialTheme.shapes.extraLarge
        ) {
            SearchViewContent(
                uiState = uiState,
                event = viewModel,
                query = query,
                isCompactScreen = isCompactScreen,
                navActionManager = navActionManager,
                showHistory = query.isEmpty(),
                onHistoryItemClick = {
                    query = it
                    viewModel.search(it)
                }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SearchHistoryList(
    history: List<SearchHistory>,
    onHistoryItemClick: (String) -> Unit,
    onHistoryItemRemove: (SearchHistory) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        items(history) { item ->
            val haptic = LocalHapticFeedback.current
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(
                        onClick = { onHistoryItemClick(item.keyword) },
                        onLongClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onHistoryItemRemove(item)
                        }
                    )
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_history_24),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = item.keyword,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SearchViewContent(
    uiState: SearchUiState,
    event: SearchEvent?,
    query: String,
    isCompactScreen: Boolean,
    navActionManager: NavActionManager,
    contentPadding: PaddingValues = PaddingValues(),
    showHistory: Boolean = false,
    onHistoryItemClick: (String) -> Unit = {}
) {
    val context = LocalContext.current

    LaunchedEffect(uiState.message) {
        if (uiState.message != null) {
            context.showToast(uiState.message)
            event?.onMessageDisplayed()
        }
    }

    @Composable
    fun FilterRow() {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MediaType.entries.forEach {
                FilterChip(
                    selected = uiState.mediaType == it,
                    onClick = { event?.onChangeMediaType(it) },
                    label = { 
                        Text(
                            text = it.localized(),
                            fontWeight = if (uiState.mediaType == it) FontWeight.Bold else FontWeight.Normal
                        ) 
                    },
                    leadingIcon = if (uiState.mediaType == it) {
                        {
                            Icon(
                                painter = painterResource(R.drawable.round_check_24),
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    } else null,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    border = null,
                    shape = MaterialTheme.shapes.medium
                )
            }
        }
    }

    @Composable
    fun ItemView(item: BaseMediaList) {
        val userScore = item.node.myListStatus?.score ?: 0
        MediaItemDetailed(
            title = item.node.userPreferredTitle(),
            imageUrl = item.node.mainPicture?.large,
            topBadgeContent = if (userScore > 0) {
                {
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.9f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = userScore.toString(),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            Icon(
                                painter = painterResource(R.drawable.ic_round_star_16),
                                contentDescription = null,
                                modifier = Modifier
                                    .padding(start = 2.dp)
                                    .size(10.dp),
                                tint = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }
            } else null,
            badgeContent = item.node.myListStatus?.status?.let { status ->
                {
                    Icon(
                        imageVector = status.icon,
                        contentDescription = status.localized(),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            },
            subtitle1 = {
                Text(
                    text = buildString {
                        append(item.node.mediaFormat?.localized() ?: UNKNOWN_CHAR)
                        if (item.node.totalDuration().toStringPositiveValueOrNull() != null) {
                            append(" (${item.node.durationText()})")
                        }
                    },
                    style = MaterialTheme.typography.bodySmall,
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
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            subtitle3 = {
                if (!uiState.hideScore) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(R.drawable.ic_round_details_star_24),
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = Color(0xFFFFB300)
                        )
                        Text(
                            text = item.node.mean.toStringPositiveValueOrUnknown(),
                            modifier = Modifier.padding(start = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            },
            onClick = dropUnlessResumed {
                navActionManager.toMediaDetails(uiState.mediaType, item.node.id)
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        FilterRow()
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

        if (showHistory) {
            SearchHistoryList(
                history = uiState.searchHistoryList,
                onHistoryItemClick = onHistoryItemClick,
                onHistoryItemRemove = { event?.onRemoveSearchHistory(it) }
            )
        } else {
            when {
                uiState.isLoading && uiState.mediaList.isEmpty() -> {
                    LoadingState()
                }
                uiState.message != null && uiState.mediaList.isEmpty() -> {
                    ErrorState(
                        message = uiState.message,
                        onAction = { event?.search(query) }
                    )
                }
                uiState.noResults -> {
                    EmptyState()
                }
                uiState.mediaList.isNotEmpty() -> {
                    if (!isCompactScreen) {
                        val gridState = rememberLazyGridState()
                        gridState.OnBottomReached(buffer = 4) { event?.loadMore() }
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier.fillMaxSize(),
                            state = gridState,
                            contentPadding = contentPadding
                        ) {
                            items(
                                items = uiState.mediaList,
                                contentType = { it.node }
                            ) {
                                ItemView(item = it)
                            }
                        }
                    } else {
                        val listState = rememberLazyListState()
                        listState.OnBottomReached(buffer = 3) { event?.loadMore() }
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            state = listState,
                            contentPadding = contentPadding
                        ) {
                            items(
                                items = uiState.mediaList,
                                contentType = { it.node }
                            ) {
                                ItemView(item = it)
                            }
                            if (uiState.isLoading) {
                                items(5) {
                                    MediaItemDetailedPlaceholder()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun SearchPreview() {
    MoeListTheme {
        Surface {
            SearchHostView(
                isCompactScreen = false,
                navActionManager = NavActionManager.rememberNavActionManager(),
                padding = PaddingValues()
            )
        }
    }
}
