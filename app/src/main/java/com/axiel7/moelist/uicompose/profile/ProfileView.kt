package com.axiel7.moelist.uicompose.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.uicompose.composables.DefaultScaffoldWithTopBar
import com.axiel7.moelist.uicompose.composables.DonutChart
import com.axiel7.moelist.uicompose.composables.TextIconHorizontal
import com.axiel7.moelist.uicompose.composables.TextIconVertical
import com.axiel7.moelist.uicompose.composables.defaultPlaceholder
import com.axiel7.moelist.uicompose.theme.MoeListTheme
import com.axiel7.moelist.utils.Constants
import com.axiel7.moelist.utils.ContextExtensions.openLink
import com.axiel7.moelist.utils.DateUtils.parseDateAndLocalize
import com.axiel7.moelist.utils.NumExtensions.toStringOrZero
import com.axiel7.moelist.utils.StringExtensions.toNavArgument
import java.time.format.DateTimeFormatter

const val PROFILE_DESTINATION = "profile"

@Composable
fun ProfileView(navController: NavController) {
    val context = LocalContext.current
    val viewModel: ProfileViewModel = viewModel()
    val scrollState = rememberScrollState()

    DefaultScaffoldWithTopBar(
        title = stringResource(R.string.title_profile),
        navController = navController
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = viewModel.profilePictureUrl,
                    contentDescription = "profile",
                    placeholder = painterResource(R.drawable.ic_round_account_circle_24),
                    error = painterResource(R.drawable.ic_round_account_circle_24),
                    modifier = Modifier
                        .padding(16.dp)
                        .clip(RoundedCornerShape(100))
                        .size(100.dp)
                        .defaultPlaceholder(visible = viewModel.isLoading)
                        .clickable {
                            navController.navigate(
                                "full_poster/${arrayOf(viewModel.profilePictureUrl ?: "").toNavArgument()}"
                            )
                        }
                )

                Column {
                    Text(
                        text = viewModel.user?.name ?: "Loading...",
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .defaultPlaceholder(visible = viewModel.isLoading),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )

                    viewModel.user?.location?.let { location ->
                        if (location.isNotBlank())
                            TextIconHorizontal(
                                text = location,
                                icon = R.drawable.ic_round_location_on_24,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                    }

                    viewModel.user?.birthday?.let { birthday ->
                        TextIconHorizontal(
                            text = birthday.parseDateAndLocalize() ?: "",
                            icon = R.drawable.ic_round_cake_24,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    TextIconHorizontal(
                        text = if (viewModel.user?.joinedAt != null)
                            viewModel.user?.joinedAt!!.parseDateAndLocalize(
                                inputFormat = DateTimeFormatter.ISO_DATE_TIME
                            ) ?: ""
                        else "Loading...",
                        icon = R.drawable.ic_round_access_time_24,
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                            .defaultPlaceholder(visible = viewModel.isLoading)
                    )
                }
            }//:Row

            Divider(modifier = Modifier.padding(vertical = 16.dp))

            //Stats
            UserStatsView(
                viewModel = viewModel,
                mediaType = MediaType.ANIME
            )

            Divider(modifier = Modifier.padding(vertical = 16.dp))

            UserStatsView(
                viewModel = viewModel,
                mediaType = MediaType.MANGA
            )

            Divider(modifier = Modifier.padding(vertical = 16.dp))

            TextButton(
                onClick = { context.openLink(Constants.MAL_PROFILE_URL + viewModel.user?.name) },
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text(
                    text = stringResource(R.string.view_profile_mal),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }//:Column
    }//:Scaffold
    
    LaunchedEffect(Unit) {
        if (viewModel.user == null) viewModel.getMyUser()
    }
}

@Composable
fun UserStatsView(
    viewModel: ProfileViewModel,
    mediaType: MediaType
) {
    Text(
        text = if (mediaType == MediaType.ANIME) stringResource(R.string.anime_stats)
        else stringResource(R.string.manga_stats),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        DonutChart(
            stats = if (mediaType == MediaType.ANIME) viewModel.animeStats else viewModel.mangaStats,
            centerContent = {
                Text(
                    text = stringResource(R.string.total_entries).format(
                    if (mediaType == MediaType.ANIME)
                        viewModel.animeStats.value.sumOf { it.value.toInt() }
                    else viewModel.mangaStats.value.sumOf { it.value.toInt() }
                    ),
                    modifier = Modifier.width(100.dp),
                    textAlign = TextAlign.Center
                )
            }
        )

        Column {
            (if (mediaType == MediaType.ANIME) viewModel.animeStats else viewModel.mangaStats)
                .value.forEach {
                SuggestionChip(
                    onClick = { },
                    label = { Text(text = stringResource(it.title)) },
                    modifier = Modifier.padding(horizontal = 8.dp),
                    icon = { Text(text = String.format("%.0f", it.value)) },
                    border = SuggestionChipDefaults.suggestionChipBorder(
                        borderColor = it.color
                    )
                )
            }
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        TextIconVertical(
            text = if (mediaType == MediaType.ANIME)
                viewModel.user?.animeStatistics?.numDays.toStringOrZero()
            else viewModel.userMangaStats?.days.toStringOrZero(),
            icon = R.drawable.ic_round_event_24,
            tooltip = stringResource(R.string.days)
        )
        if (mediaType == MediaType.ANIME) {
            TextIconVertical(
                text = viewModel.user?.animeStatistics?.numEpisodes.toStringOrZero(),
                icon = R.drawable.play_circle_outline_24,
                tooltip = stringResource(R.string.episodes)
            )
        } else {
            TextIconVertical(
                text = viewModel.userMangaStats?.chaptersRead.toStringOrZero(),
                icon = R.drawable.ic_round_menu_book_24,
                tooltip = stringResource(R.string.chapters)
            )
            TextIconVertical(
                text = viewModel.userMangaStats?.volumesRead.toStringOrZero(),
                icon = R.drawable.ic_outline_book_24,
                tooltip = stringResource(R.string.volumes)
            )
        }

        TextIconVertical(
            text = if (mediaType == MediaType.ANIME)
                viewModel.user?.animeStatistics?.meanScore.toStringOrZero()
            else viewModel.userMangaStats?.meanScore.toStringOrZero(),
            icon = R.drawable.ic_round_details_star_24,
            tooltip = stringResource(R.string.mean_score)
        )
        TextIconVertical(
            text = if (mediaType == MediaType.ANIME)
                viewModel.user?.animeStatistics?.numTimesRewatched.toStringOrZero()
            else viewModel.userMangaStats?.repeat.toStringOrZero(),
            icon = R.drawable.round_repeat_24,
            tooltip = if (mediaType == MediaType.ANIME) stringResource(R.string.rewatched)
            else stringResource(R.string.total_rereads)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ProfilePreview() {
    MoeListTheme {
        ProfileView(navController = rememberNavController())
    }
}