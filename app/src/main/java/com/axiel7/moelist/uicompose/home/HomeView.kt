package com.axiel7.moelist.uicompose.home

import androidx.annotation.DrawableRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.anime.AnimeRanking
import com.axiel7.moelist.data.model.anime.airingInString
import com.axiel7.moelist.data.model.anime.icon
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.data.model.media.userPreferredTitle
import com.axiel7.moelist.uicompose.composables.MEDIA_ITEM_VERTICAL_HEIGHT
import com.axiel7.moelist.uicompose.composables.MEDIA_POSTER_SMALL_HEIGHT
import com.axiel7.moelist.uicompose.composables.MEDIA_POSTER_SMALL_WIDTH
import com.axiel7.moelist.uicompose.composables.MediaItemDetailedPlaceholder
import com.axiel7.moelist.uicompose.composables.MediaItemVertical
import com.axiel7.moelist.uicompose.composables.MediaItemVerticalPlaceholder
import com.axiel7.moelist.uicompose.composables.MediaPoster
import com.axiel7.moelist.uicompose.composables.SmallScoreIndicator
import com.axiel7.moelist.uicompose.theme.MoeListTheme
import com.axiel7.moelist.utils.ContextExtensions.showToast
import com.axiel7.moelist.utils.SeasonCalendar
import kotlin.random.Random

const val HOME_DESTINATION = "home"

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeView(
    isLoggedIn: Boolean,
    modifier: Modifier = Modifier,
    navigateToMediaDetails: (MediaType, Int) -> Unit,
    navigateToRanking: (MediaType) -> Unit,
    navigateToSeasonChart: () -> Unit,
    navigateToCalendar: () -> Unit,
) {
    val context = LocalContext.current
    val viewModel: HomeViewModel = viewModel()
    val scrollState = rememberScrollState()
    val airingListState = rememberLazyListState()
    val seasonListState = rememberLazyListState()
    val recommendListState = rememberLazyListState()

    LaunchedEffect(viewModel.message) {
        if (viewModel.showMessage) {
            context.showToast(viewModel.message)
            viewModel.showMessage = false
        }
    }

    LaunchedEffect(isLoggedIn) {
        viewModel.initRequestChain(isLoggedIn)
    }

    Column(
        modifier = modifier.verticalScroll(scrollState)
    ) {
        // Chips
        Row(
            modifier = Modifier.padding(top = 10.dp, start = 8.dp, end = 16.dp)
        ) {
            HomeCard(
                text = stringResource(R.string.anime_ranking),
                icon = R.drawable.ic_round_movie_24,
                modifier = Modifier.weight(1f),
                onClick = {
                    navigateToRanking(MediaType.ANIME)
                },
            )

            HomeCard(
                text = stringResource(R.string.manga_ranking),
                icon = R.drawable.ic_round_menu_book_24,
                modifier = Modifier.weight(1f),
                onClick = {
                    navigateToRanking(MediaType.MANGA)
                },
            )
        }

        Row(
            modifier = Modifier.padding(top = 10.dp, start = 8.dp, end = 16.dp)
        ) {
            HomeCard(
                text = stringResource(R.string.seasonal_chart),
                icon = SeasonCalendar.currentSeason.icon(),
                modifier = Modifier.weight(1f),
                onClick = navigateToSeasonChart,
            )

            HomeCard(
                text = stringResource(R.string.calendar),
                icon = R.drawable.ic_round_event_24,
                modifier = Modifier.weight(1f),
                onClick = navigateToCalendar,
            )
        }

        // Airing
        HeaderHorizontalList(
            text = stringResource(R.string.today),
            onClick = navigateToCalendar
        )
        if (!viewModel.isLoading && viewModel.todayAnimes.isEmpty()) {
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
        }
        else LazyRow(
            modifier = Modifier
                .padding(top = 8.dp)
                .sizeIn(minHeight = MEDIA_POSTER_SMALL_HEIGHT.dp),
            state = airingListState,
            contentPadding = PaddingValues(horizontal = 8.dp),
            flingBehavior = rememberSnapFlingBehavior(lazyListState = airingListState)
        ) {
            items(
                items = viewModel.todayAnimes,
                key = { it.node.id },
                contentType = { it.node }
            ) {
                AiringAnimeHorizontalItem(
                    item = it,
                    onClick = {
                        navigateToMediaDetails(MediaType.ANIME, it.node.id)
                    }
                )
            }
            if (viewModel.isLoading) {
                items(5) {
                    MediaItemDetailedPlaceholder()
                }
            }
        }

        // This Season
        HeaderHorizontalList(
            text = stringResource(R.string.this_season),
            onClick = navigateToSeasonChart
        )
        if (!viewModel.isLoading && viewModel.seasonAnimes.isEmpty()) {
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
        }
        else LazyRow(
            modifier = Modifier
                .padding(top = 8.dp)
                .sizeIn(minHeight = MEDIA_ITEM_VERTICAL_HEIGHT.dp),
            state = seasonListState,
            contentPadding = PaddingValues(horizontal = 16.dp),
            flingBehavior = rememberSnapFlingBehavior(lazyListState = seasonListState)
        ) {
            items(viewModel.seasonAnimes,
                key = { it.node.id },
                contentType = { it.node }
            ) {
                MediaItemVertical(
                    url = it.node.mainPicture?.large,
                    title = it.node.userPreferredTitle(),
                    modifier = Modifier.padding(end = 8.dp),
                    subtitle = {
                        SmallScoreIndicator(
                            score = it.node.mean,
                            fontSize = 13.sp
                        )
                    },
                    onClick = { navigateToMediaDetails(MediaType.ANIME, it.node.id) }
                )
            }
            if (viewModel.isLoading) {
                items(10) {
                    MediaItemVerticalPlaceholder()
                }
            }
        }

        //Recommended
        HeaderHorizontalList(stringResource(R.string.recommendations), onClick = { })
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
        }
        else if (!viewModel.isLoading && viewModel.recommendedAnimes.isEmpty()) {
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
        }
        else LazyRow(
            modifier = Modifier
                .padding(vertical = 8.dp)
                .sizeIn(minHeight = MEDIA_ITEM_VERTICAL_HEIGHT.dp),
            state = recommendListState,
            contentPadding = PaddingValues(horizontal = 16.dp),
            flingBehavior = rememberSnapFlingBehavior(lazyListState = recommendListState)
        ) {
            items(viewModel.recommendedAnimes,
                key = { it.node.id },
                contentType = { it.node }
            ) {
                MediaItemVertical(
                    url = it.node.mainPicture?.large,
                    title = it.node.userPreferredTitle(),
                    modifier = Modifier.padding(end = 8.dp),
                    subtitle = {
                        SmallScoreIndicator(
                            score = it.node.mean,
                            fontSize = 13.sp
                        )
                    },
                    onClick = { navigateToMediaDetails(MediaType.ANIME, it.node.id) }
                )
            }
            if (viewModel.isLoading) {
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
                onClick = {
                    val type = if (Random.nextBoolean()) MediaType.ANIME else MediaType.MANGA
                    val id = Random.nextInt(from = 0, until = 6000)
                    navigateToMediaDetails(type, id)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeCard(
    text: String,
    @DrawableRes icon: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = modifier.padding(start = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .height(40.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = text,
                modifier = Modifier
                    .padding(end = 8.dp)
                    .size(18.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = text,
                fontSize = 15.sp,
                overflow = TextOverflow.Ellipsis,
                maxLines = 2,
                lineHeight = 15.sp
            )
        }
    }
}

@Composable
fun HeaderHorizontalList(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Icon(imageVector = ImageVector.vectorResource(R.drawable.ic_round_arrow_forward_24), contentDescription = text)
        }
    }
}

@Composable
fun AiringAnimeHorizontalItem(item: AnimeRanking, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .sizeIn(maxWidth = 300.dp, minWidth = 250.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
    ) {
        MediaPoster(
            url = item.node.mainPicture?.large,
            modifier = Modifier.size(width = MEDIA_POSTER_SMALL_WIDTH.dp, height = MEDIA_POSTER_SMALL_HEIGHT.dp)
        )

        Column(
            modifier = Modifier.padding(start = 16.dp)
        ) {
            Text(
                text = item.node.userPreferredTitle(),
                fontSize = 18.sp,
                maxLines = 2,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = item.node.broadcast?.airingInString() ?: "",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            SmallScoreIndicator(
                score = item.node.mean
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomePreview() {
    MoeListTheme {
        Surface {
            HomeView(
                isLoggedIn = false,
                navigateToMediaDetails = { _, _ -> },
                navigateToRanking = {},
                navigateToSeasonChart = {},
                navigateToCalendar = {}
            )
        }
    }
}