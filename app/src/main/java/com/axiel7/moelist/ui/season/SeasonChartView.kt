package com.axiel7.moelist.ui.season

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.ui.base.navigation.NavActionManager
import com.axiel7.moelist.ui.composables.DefaultScaffoldWithTopAppBar
import com.axiel7.moelist.ui.composables.TextIconHorizontal
import com.axiel7.moelist.ui.composables.media.MEDIA_POSTER_SMALL_WIDTH
import com.axiel7.moelist.ui.composables.media.MediaItemDetailedPlaceholder
import com.axiel7.moelist.ui.composables.media.MediaItemVertical
import com.axiel7.moelist.ui.composables.media.SmallScoreIndicator
import com.axiel7.moelist.ui.season.composables.SeasonChartFilterSheet
import com.axiel7.moelist.ui.theme.MoeListTheme
import com.axiel7.moelist.utils.ContextExtensions.showToast
import com.axiel7.moelist.utils.NumExtensions.format
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
    navActionManager: NavActionManager
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()
    val bottomBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    if (sheetState.isVisible) {
        SeasonChartFilterSheet(
            uiState = uiState,
            event = event,
            onApply = {
                scope.launch { sheetState.hide() }
                event?.onApplyFilters()
            },
            onDismiss = { scope.launch { sheetState.hide() } },
            sheetState = sheetState,
            bottomPadding = bottomBarPadding
        )
    }

    LaunchedEffect(uiState.message) {
        if (uiState.message != null) {
            context.showToast(uiState.message)
            event?.onMessageDisplayed()
        }
    }

    DefaultScaffoldWithTopAppBar(
        title = uiState.season.seasonYearText(),
        navigateBack = navActionManager::goBack,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { scope.launch { sheetState.show() } },
                modifier = Modifier.padding(WindowInsets.navigationBars.asPaddingValues())
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_round_filter_list_24),
                    contentDescription = stringResource(R.string.filters)
                )
            }
        },
        contentWindowInsets = WindowInsets.systemBars
            .only(WindowInsetsSides.Horizontal)
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = MEDIA_POSTER_SMALL_WIDTH.dp),
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(
                start = 8.dp,
                top = 8.dp,
                end = 8.dp,
                bottom = bottomBarPadding
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
        ) {
            items(
                items = uiState.animes,
                key = { it.node.id },
                contentType = { it.node }
            ) { item ->
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    MediaItemVertical(
                        imageUrl = item.node.mainPicture?.large,
                        title = item.node.title,
                        subtitle = {
                            SmallScoreIndicator(
                                score = item.node.mean,
                                fontSize = 13.sp
                            )
                        },
                        subtitle2 = {
                            item.node.numListUsers?.format()?.let { users ->
                                TextIconHorizontal(
                                    text = users,
                                    icon = R.drawable.ic_round_group_24,
                                    color = MaterialTheme.colorScheme.outline,
                                    fontSize = 13.sp,
                                    iconSize = 16.dp
                                )
                            }
                        },
                        minLines = 2,
                        onClick = {
                            navActionManager.toMediaDetails(MediaType.ANIME, item.node.id)
                        }
                    )
                }
            }
            if (uiState.isLoading) {
                items(12) {
                    MediaItemDetailedPlaceholder()
                }
            }
            item(contentType = { 0 }) {
                LaunchedEffect(uiState.nextPage) {
                    event?.loadMore()
                }
            }
        }
    }//:Scaffold
}

@Preview
@Composable
fun SeasonChartPreview() {
    MoeListTheme {
        Surface {
            SeasonChartViewContent(
                uiState = SeasonChartUiState(),
                event = null,
                navActionManager = NavActionManager.rememberNavActionManager()
            )
        }
    }
}