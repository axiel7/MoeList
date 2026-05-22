package com.axiel7.moelist.ui.season

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.ui.base.navigation.NavActionManager
import com.axiel7.moelist.ui.composables.BackIconButton
import com.axiel7.moelist.ui.composables.EmptyState
import com.axiel7.moelist.ui.composables.ErrorState
import com.axiel7.moelist.ui.composables.LoadingState
import com.axiel7.moelist.ui.composables.media.MEDIA_POSTER_SMALL_WIDTH
import com.axiel7.moelist.ui.composables.media.MediaItemVertical
import com.axiel7.moelist.ui.season.composables.SeasonChartFilterSheet
import com.axiel7.moelist.ui.season.composables.SeasonChartFormatSheet
import com.axiel7.moelist.ui.theme.MoeListTheme
import com.axiel7.moelist.utils.ContextExtensions.showToast
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun SeasonChartView(
    navActionManager: NavActionManager
) {
    val viewModel: SeasonChartViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SeasonChartViewContent(
        uiState = uiState,
        event = viewModel,
        navActionManager = navActionManager
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SeasonChartViewContent(
    uiState: SeasonChartUiState,
    event: SeasonChartEvent?,
    navActionManager: NavActionManager?
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val filterSheetState = rememberModalBottomSheetState()
    var showFilterSheet by remember { mutableStateOf(false) }
    fun hideFilterSheet() {
        scope.launch { filterSheetState.hide() }.invokeOnCompletion { showFilterSheet = false }
    }

    val formatSheetState = rememberModalBottomSheetState()
    var showFormatSheet by remember { mutableStateOf(false) }
    fun hideFormatSheet() {
        scope.launch { formatSheetState.hide() }.invokeOnCompletion { showFormatSheet = false }
    }

    val bottomBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    if (showFilterSheet) {
        SeasonChartFilterSheet(
            uiState = uiState,
            event = event,
            onApply = {
                hideFilterSheet()
                event?.onApplyFilters()
            },
            onDismiss = { hideFilterSheet() },
            sheetState = filterSheetState,
            bottomPadding = bottomBarPadding
        )
    }

    if (showFormatSheet) {
        SeasonChartFormatSheet(
            uiState = uiState,
            event = event,
            onDismiss = { hideFormatSheet() },
            sheetState = formatSheetState
        )
    }

    LaunchedEffect(uiState.message) {
        if (uiState.message != null) {
            context.showToast(uiState.message)
            event?.onMessageDisplayed()
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            Column(
                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
            ) {
                MediumTopAppBar(
                    title = {
                        Column {
                            Text(
                                text = uiState.season.seasonYearText(),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (uiState.animes.isNotEmpty()) {
                                Text(
                                    text = stringResource(R.string.anime_count, uiState.animes.size),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        BackIconButton(onClick = { navActionManager?.goBack() })
                    },
                    scrollBehavior = scrollBehavior,
                    colors = TopAppBarDefaults.mediumTopAppBarColors(
                        scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                    )
                )
                
                // Integrated Utility Bar (Chips)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val selectedFormatText = uiState.selectedFormat?.localized() ?: stringResource(R.string.all)
                    val count = uiState.formatCounts[uiState.selectedFormat] ?: 0
                    
                    FilterChip(
                        selected = uiState.selectedFormat != null,
                        onClick = { showFormatSheet = true },
                        label = {
                            Text(text = "$selectedFormatText ($count)")
                        },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.ExpandMore,
                                contentDescription = null,
                                modifier = Modifier.size(FilterChipDefaults.IconSize)
                            )
                        },
                        shape = MaterialTheme.shapes.large
                    )

                    FilterChip(
                        selected = false,
                        onClick = { showFilterSheet = true },
                        label = {
                            Text(text = stringResource(R.string.filters))
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.FilterList,
                                contentDescription = null,
                                modifier = Modifier.size(FilterChipDefaults.IconSize)
                            )
                        },
                        shape = MaterialTheme.shapes.large
                    )
                }
            }
        },
        contentWindowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Horizontal)
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            AnimatedContent(
                targetState = uiState,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "SeasonChartContent"
            ) { state ->
                when {
                    state.isLoading && state.animes.isEmpty() -> {
                        LoadingState(modifier = Modifier.fillMaxSize())
                    }
                    state.message != null && state.animes.isEmpty() -> {
                        ErrorState(
                            modifier = Modifier.fillMaxSize(),
                            message = state.message,
                            onAction = { event?.onApplyFilters() }
                        )
                    }
                    !state.isLoading && state.animes.isEmpty() -> {
                        EmptyState(modifier = Modifier.fillMaxSize())
                    }
                    else -> {
                        LazyVerticalStaggeredGrid(
                            columns = StaggeredGridCells.Adaptive(minSize = MEDIA_POSTER_SMALL_WIDTH.dp),
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                start = 12.dp,
                                top = 8.dp,
                                end = 12.dp,
                                bottom = bottomBarPadding + 16.dp
                            ),
                            verticalItemSpacing = 16.dp,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(
                                items = state.filteredAnimes,
                                key = { it.node.id }
                            ) { item ->
                                MediaItemVertical(
                                    imageUrl = item.node.mainPicture?.large,
                                    title = item.node.title,
                                    badgeContent = item.node.myListStatus?.status?.let { status ->
                                        {
                                            Icon(
                                                imageVector = status.icon,
                                                contentDescription = status.localized(),
                                                modifier = Modifier.size(16.dp),
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    },
                                    posterOverlay = if (item.node.mean != null && item.node.mean > 0) {
                                        {
                                            Icon(
                                                imageVector = Icons.Rounded.Star,
                                                contentDescription = null,
                                                modifier = Modifier.size(12.dp),
                                                tint = Color(0xFFFFC107)
                                            )
                                            Text(
                                                text = item.node.mean.toString(),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(start = 2.dp)
                                            )
                                        }
                                    } else null,
                                    onClick = dropUnlessResumed {
                                        navActionManager?.toMediaDetails(MediaType.ANIME, item.node.id)
                                    }
                                )
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
fun SeasonChartViewPreview() {
    MoeListTheme {
        SeasonChartViewContent(
            uiState = SeasonChartUiState(),
            event = null,
            navActionManager = null
        )
    }
}
