package com.axiel7.moelist.ui.home

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.ui.base.navigation.NavActionManager
import com.axiel7.moelist.ui.composables.HeaderHorizontalList
import com.axiel7.moelist.ui.composables.collapsable
import com.axiel7.moelist.ui.composables.media.MEDIA_ITEM_VERTICAL_HEIGHT
import com.axiel7.moelist.ui.composables.media.MEDIA_POSTER_SMALL_HEIGHT
import com.axiel7.moelist.ui.composables.media.MediaItemDetailedPlaceholder
import com.axiel7.moelist.ui.composables.media.MediaItemVertical
import com.axiel7.moelist.ui.composables.media.MediaItemVerticalPlaceholder
import com.axiel7.moelist.ui.composables.score.SmallScoreIndicator
import com.axiel7.moelist.ui.home.composables.AiringAnimeHorizontalItem
import com.axiel7.moelist.ui.home.composables.HomeCard
import com.axiel7.moelist.ui.theme.MoeListTheme
import com.axiel7.moelist.utils.ContextExtensions.showToast
import com.axiel7.moelist.utils.SeasonCalendar
import org.koin.androidx.compose.koinViewModel
import kotlin.random.Random

@Composable
fun HomeView(
    isLoggedIn: Boolean,
    navActionManager: NavActionManager,
    topBarHeightPx: Float,
    topBarOffsetY: Animatable<Float, AnimationVector1D>,
    padding: PaddingValues,
) {
    val viewModel: HomeViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HomeViewContent(
        uiState = uiState,
        event = viewModel,
        isLoggedIn = isLoggedIn,
        navActionManager = navActionManager,
        topBarHeightPx = topBarHeightPx,
        topBarOffsetY = topBarOffsetY,
        padding = padding,
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HomeViewContent(
    uiState: HomeUiState,
    event: HomeEvent?,
    isLoggedIn: Boolean,
    navActionManager: NavActionManager,
    topBarHeightPx: Float = 0f,
    topBarOffsetY: Animatable<Float, AnimationVector1D> = Animatable(0f),
    padding: PaddingValues = PaddingValues(),
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val airingListState = rememberLazyListState()
    val seasonListState = rememberLazyListState()
    val recommendListState = rememberLazyListState()

    LaunchedEffect(uiState.message) {
        if (uiState.message != null) {
            context.showToast(uiState.message)
            event?.onMessageDisplayed()
        }
    }

    LaunchedEffect(isLoggedIn) {
        event?.initRequestChain(isLoggedIn)
    }

    Column(
        modifier = Modifier
            .collapsable(
                state = scrollState,
                topBarHeightPx = topBarHeightPx,
                topBarOffsetY = topBarOffsetY,
            )
            .verticalScroll(scrollState)
            .padding(padding)
    ) {
        // Chips
        Row(
            modifier = Modifier.padding(top = 10.dp, start = 8.dp, end = 16.dp)
        ) {
            HomeCard(
                text = stringResource(R.string.anime_ranking),
                icon = R.drawable.ic_round_movie_24,
                modifier = Modifier.weight(1f),
                onClick = dropUnlessResumed {
                    navActionManager.toMediaRanking(MediaType.ANIME)
                },
            )

            HomeCard(
                text = stringResource(R.string.manga_ranking),
                icon = R.drawable.ic_round_menu_book_24,
                modifier = Modifier.weight(1f),
                onClick = dropUnlessResumed {
                    navActionManager.toMediaRanking(MediaType.MANGA)
                },
            )
        }

        Row(
            modifier = Modifier.padding(top = 10.dp, start = 8.dp, end = 16.dp)
        ) {
            HomeCard(
                text = stringResource(R.string.seasonal_chart),
                icon = SeasonCalendar.currentSeason.icon,
                modifier = Modifier.weight(1f),
                onClick = dropUnlessResumed {
                    navActionManager.toSeasonChart()
                },
            )

            HomeCard(
                text = stringResource(R.string.calendar),
                icon = R.drawable.ic_round_event_24,
                modifier = Modifier.weight(1f),
                onClick = dropUnlessResumed {
                    navActionManager.toCalendar()
                },
            )
        }

        // Airing
        HeaderHorizontalList(
            text = stringResource(R.string.today),
            onClick = dropUnlessResumed { navActionManager.toCalendar() }
        )
        if (!uiState.isLoading && uiState.todayAnimes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(MEDIA_POSTER_SMALL_HEIGHT.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.nothing_today),
                    textAlign = TextAlign.Center
                )
            }
        } else LazyRow(
            modifier = Modifier
                .padding(top = 8.dp)
                .sizeIn(minHeight = MEDIA_POSTER_SMALL_HEIGHT.dp),
            state = airingListState,
            contentPadding = PaddingValues(horizontal = 8.dp),
            flingBehavior = rememberSnapFlingBehavior(lazyListState = airingListState)
        ) {
            items(
                items = uiState.todayAnimes,
                key = { it.node.id },
                contentType = { it.node }
            ) {
                AiringAnimeHorizontalItem(
                    item = it,
                    onClick = dropUnlessResumed {
                        navActionManager.toMediaDetails(MediaType.ANIME, it.node.id)
                    }
                )
            }
            if (uiState.isLoading) {
                items(5) {
                    MediaItemDetailedPlaceholder()
                }
            }
        }

        // This Season
        HeaderHorizontalList(
            text = stringResource(R.string.this_season),
            onClick = dropUnlessResumed { navActionManager.toSeasonChart() }
        )
        if (!uiState.isLoading && uiState.seasonAnimes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(MEDIA_POSTER_SMALL_HEIGHT.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.error_server),
                    textAlign = TextAlign.Center
                )
            }
        } else LazyRow(
            modifier = Modifier
                .padding(top = 8.dp)
                .sizeIn(minHeight = MEDIA_ITEM_VERTICAL_HEIGHT.dp),
            state = seasonListState,
            contentPadding = PaddingValues(horizontal = 16.dp),
            flingBehavior = rememberSnapFlingBehavior(lazyListState = seasonListState)
        ) {
            items(
                items = uiState.seasonAnimes,
                key = { it.node.id },
                contentType = { it.node }
            ) {
                MediaItemVertical(
                    imageUrl = it.node.mainPicture?.large,
                    title = it.node.userPreferredTitle(),
                    modifier = Modifier.padding(end = 8.dp),
                    subtitle = {
                        SmallScoreIndicator(
                            score = it.node.mean,
                            fontSize = 13.sp
                        )
                    },
                    minLines = 2,
                    onClick = dropUnlessResumed {
                        navActionManager.toMediaDetails(MediaType.ANIME, it.node.id)
                    }
                )
            }
            if (uiState.isLoading) {
                items(10) {
                    MediaItemVerticalPlaceholder()
                }
            }
        }

        //Recommended
        HeaderHorizontalList(
            text = stringResource(R.string.recommendations),
            onClick = dropUnlessResumed { navActionManager.toRecommendations() }
        )
        if (!isLoggedIn) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(MEDIA_POSTER_SMALL_HEIGHT.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.please_login_to_use_this_feature),
                    textAlign = TextAlign.Center
                )
            }
        } else if (!uiState.isLoading && uiState.recommendedAnimes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(MEDIA_POSTER_SMALL_HEIGHT.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.no_recommendations),
                    textAlign = TextAlign.Center
                )
            }
        } else LazyRow(
            modifier = Modifier
                .padding(vertical = 8.dp)
                .sizeIn(minHeight = MEDIA_ITEM_VERTICAL_HEIGHT.dp),
            state = recommendListState,
            contentPadding = PaddingValues(horizontal = 16.dp),
            flingBehavior = rememberSnapFlingBehavior(lazyListState = recommendListState)
        ) {
            items(
                items = uiState.recommendedAnimes,
                key = { it.node.id },
                contentType = { it.node }
            ) {
                MediaItemVertical(
                    imageUrl = it.node.mainPicture?.large,
                    title = it.node.userPreferredTitle(),
                    modifier = Modifier.padding(end = 8.dp),
                    subtitle = {
                        SmallScoreIndicator(
                            score = it.node.mean,
                            fontSize = 13.sp
                        )
                    },
                    minLines = 2,
                    onClick = dropUnlessResumed {
                        navActionManager.toMediaDetails(MediaType.ANIME, it.node.id)
                    }
                )
            }
            if (uiState.isLoading) {
                items(10) {
                    MediaItemVerticalPlaceholder()
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            //Random
            OutlinedButton(
                onClick = dropUnlessResumed {
                    val type = if (Random.nextBoolean()) MediaType.ANIME else MediaType.MANGA
                    val id = Random.nextInt(from = 0, until = 6000)
                    navActionManager.toMediaDetails(type, id)
                }
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_round_casino_24),
                    contentDescription = stringResource(R.string.random),
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .size(18.dp)
                )
                Text(
                    text = stringResource(R.string.random),
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1
                )
            }
        }
    }
}

@Preview
@Composable
fun HomePreview() {
    MoeListTheme {
        Surface {
            HomeViewContent(
                uiState = HomeUiState(),
                event = null,
                isLoggedIn = false,
                navActionManager = NavActionManager.rememberNavActionManager()
            )
        }
    }
}
