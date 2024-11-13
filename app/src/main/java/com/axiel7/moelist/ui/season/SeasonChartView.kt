package com.axiel7.moelist.ui.season

import android.content.res.Configuration
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.ui.base.navigation.NavActionManager
import com.axiel7.moelist.ui.composables.DefaultScaffoldWithTopAppBar
import com.axiel7.moelist.ui.composables.TextIconHorizontal
import com.axiel7.moelist.ui.composables.media.MEDIA_POSTER_SMALL_WIDTH
import com.axiel7.moelist.ui.composables.media.MEDIA_POSTER_SMALL_WIDTH_2pr
import com.axiel7.moelist.ui.composables.media.MediaItemDetailedPlaceholder
import com.axiel7.moelist.ui.composables.media.MediaItemVertical
import com.axiel7.moelist.ui.composables.media.MediaItemVertical_2perRow
import com.axiel7.moelist.ui.composables.media.MediaItemVertical_2perRowPlaceholder
import com.axiel7.moelist.ui.composables.score.SmallScoreIndicator
import com.axiel7.moelist.ui.season.composables.SeasonChartFilterSheet
import com.axiel7.moelist.ui.theme.MoeListTheme
import com.axiel7.moelist.utils.ContextExtensions.showToast
import com.axiel7.moelist.utils.NumExtensions.format
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

//val Teal200 = Color(0xFF03DAC5)
val DarkTheme_textColor = Color(200, 200, 200)

@Composable
fun getGridCellFixed_Count_ForOrientation():Int
{
    var orient = LocalConfiguration.current.orientation
    if(orient == Configuration.ORIENTATION_LANDSCAPE)
        return 3
    else
        return 2
}

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
    var showSheet by remember { mutableStateOf(false) }
    fun hideSheet() {
        scope.launch { sheetState.hide() }.invokeOnCompletion { showSheet = false }
    }
    val bottomBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    if (showSheet) {
        SeasonChartFilterSheet(
            uiState = uiState,
            event = event,
            onApply = {
                hideSheet()
                event?.onApplyFilters()
            },
            onDismiss = { hideSheet() },
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
                onClick = { showSheet = true },
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
//            columns = GridCells.Adaptive(minSize = MEDIA_POSTER_SMALL_WIDTH_2pr.dp),
//            columns = GridCells.Fixed(2),
            columns = GridCells.Fixed(getGridCellFixed_Count_ForOrientation() ),
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(
                start = 2.dp,
                top = 8.dp,
                end = 2.dp,
                bottom = bottomBarPadding
            ),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterHorizontally)
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
                    MediaItemVertical_2perRow(
                        imageUrl = item.node.mainPicture?.large,
                        title = item.node.userPreferredTitle(),
                        subtitle = {
                            SmallScoreIndicator(
                                score = item.node.mean,
                                fontSize = 13.sp,
                                textColor = DarkTheme_textColor,
                            )
                        },
                        subtitle2 = {
                            item.node.numListUsers?.format()?.let { users ->
                                TextIconHorizontal(
                                    text = users,
                                    icon = R.drawable.ic_round_group_24,
                                    color = DarkTheme_textColor,
                                    fontSize = 13.sp,
                                    iconSize = 16.dp
                                )
                            }
                        },
                        minLines = 2,
                        onClick = dropUnlessResumed {
                            navActionManager.toMediaDetails(MediaType.ANIME, item.node.id)
                        }
                    )
                }
            }
            if (uiState.isLoading) {
                items(11) {
                    MediaItemVertical_2perRowPlaceholder()
                    //MediaItemDetailedPlaceholder()
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
