package com.axiel7.moelist.uicompose.home

import androidx.annotation.DrawableRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyHorizontalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.anime.AnimeSeasonal
import com.axiel7.moelist.data.model.anime.airingInString
import com.axiel7.moelist.data.model.anime.icon
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.uicompose.calendar.CALENDAR_DESTINATION
import com.axiel7.moelist.uicompose.composables.MEDIA_ITEM_VERTICAL_HEIGHT
import com.axiel7.moelist.uicompose.composables.MEDIA_POSTER_SMALL_HEIGHT
import com.axiel7.moelist.uicompose.composables.MEDIA_POSTER_SMALL_WIDTH
import com.axiel7.moelist.uicompose.composables.MediaItemDetailedPlaceholder
import com.axiel7.moelist.uicompose.composables.MediaItemVertical
import com.axiel7.moelist.uicompose.composables.MediaItemVerticalPlaceholder
import com.axiel7.moelist.uicompose.composables.MediaPoster
import com.axiel7.moelist.uicompose.composables.SmallScoreIndicator
import com.axiel7.moelist.uicompose.season.SEASON_CHART_DESTINATION
import com.axiel7.moelist.uicompose.theme.MoeListTheme
import com.axiel7.moelist.utils.SeasonCalendar
import kotlin.random.Random

const val HOME_DESTINATION = "home"

@Composable
fun HomeView(
    navController: NavController
) {
    val viewModel: HomeViewModel = viewModel()
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier.verticalScroll(scrollState)
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
                    navController.navigate("ranking/ANIME")
                },
            )

            HomeCard(
                text = stringResource(R.string.manga_ranking),
                icon = R.drawable.ic_round_menu_book_24,
                modifier = Modifier.weight(1f),
                onClick = {
                    navController.navigate("ranking/MANGA")
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
                onClick = {
                    navController.navigate(SEASON_CHART_DESTINATION)
                },
            )

            HomeCard(
                text = stringResource(R.string.calendar),
                icon = R.drawable.ic_round_event_24,
                modifier = Modifier.weight(1f),
                onClick = {
                    navController.navigate(CALENDAR_DESTINATION)
                },
            )
        }

        // Airing
        HeaderHorizontalList(
            text = stringResource(R.string.today),
            onClick = { navController.navigate(CALENDAR_DESTINATION) }
        )
        LazyRow(
            modifier = Modifier
                .padding(top = 8.dp)
                .sizeIn(minHeight = MEDIA_POSTER_SMALL_HEIGHT.dp),
            contentPadding = PaddingValues(horizontal = 8.dp)
        ) {
            if (viewModel.isLoading) {
                items(5) {
                    MediaItemDetailedPlaceholder()
                }
            }
            else items(viewModel.todayAnimes,
                key = { it.node.id },
                contentType = { it.node }
            ) {
                AiringAnimeHorizontalItem(
                    item = it,
                    onClick = {
                        navController.navigate("details/anime/${it.node.id}")
                    }
                )
            }
        }

        // This Season
        HeaderHorizontalList(
            text = stringResource(R.string.this_season),
            onClick = { navController.navigate(SEASON_CHART_DESTINATION) }
        )
        LazyRow(
            modifier = Modifier
                .padding(top = 8.dp)
                .sizeIn(minHeight = MEDIA_ITEM_VERTICAL_HEIGHT.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            if (viewModel.isLoading) {
                items(10) {
                    MediaItemVerticalPlaceholder()
                }
            }
            else items(viewModel.seasonAnimes,
                key = { it.node.id },
                contentType = { it.node }
            ) {
                MediaItemVertical(
                    url = it.node.mainPicture?.large,
                    title = it.node.title,
                    modifier = Modifier.padding(end = 8.dp),
                    subtitle = {
                        SmallScoreIndicator(
                            score = it.node.mean,
                            fontSize = 13.sp
                        )
                    },
                    onClick = { navController.navigate("details/anime/${it.node.id}") }
                )
            }
        }

        //Recommended
        HeaderHorizontalList(stringResource(R.string.recommendations), onClick = { })
        LazyRow(
            modifier = Modifier
                .padding(vertical = 8.dp)
                .sizeIn(minHeight = MEDIA_ITEM_VERTICAL_HEIGHT.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            if (viewModel.isLoading) {
                items(10) {
                    MediaItemVerticalPlaceholder()
                }
            }
            else items(viewModel.recommendedAnimes,
                key = { it.node.id },
                contentType = { it.node }
            ) {
                MediaItemVertical(
                    url = it.node.mainPicture?.large,
                    title = it.node.title,
                    modifier = Modifier.padding(end = 8.dp),
                    subtitle = {
                        SmallScoreIndicator(
                            score = it.node.mean,
                            fontSize = 13.sp
                        )
                    },
                    onClick = { navController.navigate("details/anime/${it.node.id}") }
                )
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
                    val type = if (Random.nextBoolean()) MediaType.ANIME.value
                    else MediaType.MANGA.value
                    val id = Random.nextInt(from = 0, until = 6000)
                    navController.navigate("details/$type/$id")
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

    LaunchedEffect(Unit) {
        viewModel.initRequestChain()
    }
}

@Composable
fun HomeCard(
    text: String,
    @DrawableRes icon: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier.padding(start = 8.dp)
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = text,
            modifier = Modifier
                .padding(end = 8.dp)
                .size(18.dp)
        )
        Text(
            text = text,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1
        )
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
fun AiringAnimeHorizontalItem(item: AnimeSeasonal, onClick: () -> Unit) {
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
                text = item.node.title,
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
                navController = rememberNavController()
            )
        }
    }
}