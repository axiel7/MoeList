package com.axiel7.moelist.ui.recommendations

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
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.axiel7.moelist.ui.composables.OnBottomReached
import com.axiel7.moelist.ui.composables.media.MEDIA_POSTER_SMALL_WIDTH
import com.axiel7.moelist.ui.composables.media.MediaItemDetailedPlaceholder
import com.axiel7.moelist.ui.composables.media.MediaItemVertical
import com.axiel7.moelist.ui.composables.score.SmallScoreIndicator
import com.axiel7.moelist.ui.theme.MoeListTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun RecommendationsView(
    navActionManager: NavActionManager
) {
    val viewModel: RecommendationsViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    RecommendationsViewContent(
        uiState = uiState,
        event = viewModel,
        navActionManager = navActionManager
    )
}

@Composable
private fun RecommendationsViewContent(
    uiState: RecommendationsUiState,
    event: RecommendationsEvent?,
    navActionManager: NavActionManager,
) {
    val bottomBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    DefaultScaffoldWithTopAppBar(
        title = stringResource(R.string.recommendations),
        navigateBack = navActionManager::goBack,
        contentWindowInsets = WindowInsets.systemBars
            .only(WindowInsetsSides.Horizontal)
    ) { padding ->
        val listState = rememberLazyGridState()
        listState.OnBottomReached(buffer = 3) {
            event?.loadMore()
        }
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
                        title = item.node.userPreferredTitle(),
                        subtitle = if (!uiState.hideScore) {
                            {
                                SmallScoreIndicator(
                                    score = item.node.mean,
                                    fontSize = 13.sp
                                )
                            }
                        } else null,
                        minLines = 2,
                        onClick = dropUnlessResumed {
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
        }
    }//:Scaffold
}

@Preview
@Composable
fun RecommendationsViewPreview() {
    MoeListTheme {
        Surface {
            RecommendationsViewContent(
                uiState = RecommendationsUiState(),
                event = null,
                navActionManager = NavActionManager.rememberNavActionManager()
            )
        }
    }
}