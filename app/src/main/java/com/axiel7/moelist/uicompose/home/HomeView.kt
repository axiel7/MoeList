package com.axiel7.moelist.uicompose.home

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.anime.AnimeSeasonal
import com.axiel7.moelist.data.model.anime.airingInValue
import com.axiel7.moelist.uicompose.composables.*
import com.axiel7.moelist.uicompose.theme.MoeListTheme
import com.axiel7.moelist.utils.SeasonCalendar

const val HOME_DESTINATION = "home"

@Composable
fun HomeView(
    viewModel: HomeViewModel,
    navController: NavController
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier.verticalScroll(scrollState)
    ) {
        // Cards
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            HomeCard(
                text = "Anime Ranking",
                icon = R.drawable.ic_round_movie_24,
                onClick = { },
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 16.dp, bottom = 8.dp, start = 16.dp, end = 4.dp)
            )
            HomeCard(
                text = "Manga Ranking",
                icon = R.drawable.ic_round_menu_book_24,
                onClick = { },
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 16.dp, bottom = 8.dp, start = 4.dp, end = 16.dp)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            HomeCard(
                text = "Seasonal Chart",
                icon = SeasonCalendar.seasonIcon,
                onClick = { },
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 4.dp, bottom = 8.dp, start = 16.dp, end = 4.dp)
            )
            HomeCard(
                text = "Calendar",
                icon = R.drawable.ic_round_event_24,
                onClick = { },
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 4.dp, bottom = 8.dp, start = 4.dp, end = 16.dp)
            )
        }

        // Airing
        HeaderHorizontalList(stringResource(R.string.today), onClick = { })
        LazyRow(
            modifier = Modifier
                .padding(vertical = 8.dp)
                .height(MEDIA_POSTER_SMALL_HEIGHT.dp)
        ) {
            items(viewModel.todayAnimes, key = { it.node.id }) {
                AiringAnimeHorizontalItem(it, onClick = {
                    navController.navigate("details/ANIME/${it.node.id}")
                })
            }
        }

        // This Season
        HeaderHorizontalList(stringResource(R.string.this_season), onClick = { })
        LazyRow(
            modifier = Modifier
                .padding(vertical = 12.dp)
                .height(MEDIA_ITEM_VERTICAL_HEIGHT.dp)
        ) {
            items(viewModel.seasonAnimes, key = { it.node.id }) {
                MediaItemVertical(
                    url = it.node.mainPicture?.large,
                    title = it.node.title,
                    modifier = Modifier.padding(start = 8.dp),
                    onClick = { navController.navigate("details/ANIME/${it.node.id}") }
                )
            }
        }

        //Recommended
        HeaderHorizontalList(stringResource(R.string.recommendations), onClick = { })
        LazyRow(
            modifier = Modifier
                .padding(vertical = 12.dp)
                .height(MEDIA_ITEM_VERTICAL_HEIGHT.dp)
        ) {
            items(viewModel.recommendedAnimes, key = { it.node.id }) {
                MediaItemVertical(
                    url = it.node.mainPicture?.large,
                    title = it.node.title,
                    modifier = Modifier.padding(start = 8.dp),
                    onClick = { navController.navigate("details/ANIME/${it.node.id}") }
                )
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.initRequestChain()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeCard(
    text: String,
    @DrawableRes icon: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            Icon(imageVector = ImageVector.vectorResource(icon), contentDescription = text)
            Text(
                text = text,
                modifier = Modifier.padding(start = 8.dp),
                fontSize = 14.sp,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
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
            Text(text, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Icon(imageVector = ImageVector.vectorResource(R.drawable.ic_round_arrow_forward_24), contentDescription = text)
        }
    }
}

@Composable
fun AiringAnimeHorizontalItem(item: AnimeSeasonal, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .padding(start = 16.dp)
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
                text = item.airingInValue(),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 4.dp))
            Text(
                text = stringResource(R.string.score_value).format(item.node.mean),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, device = Devices.DEFAULT)
@Composable
fun HomePreview() {
    MoeListTheme {
        Surface {
            HomeView(
                viewModel = viewModel(),
                navController = rememberNavController()
            )
        }
    }
}