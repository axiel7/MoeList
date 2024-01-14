package com.axiel7.moelist.ui.profile.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.ui.composables.TextIconVertical
import com.axiel7.moelist.ui.composables.defaultPlaceholder
import com.axiel7.moelist.ui.composables.stats.DonutChart
import com.axiel7.moelist.ui.profile.ProfileUiState
import com.axiel7.moelist.utils.NumExtensions.toStringOrZero

@Composable
fun UserStatsView(
    uiState: ProfileUiState,
    mediaType: MediaType,
) {
    val isLoading = if (mediaType == MediaType.MANGA) uiState.isLoadingManga else uiState.isLoading
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
            stats = if (mediaType == MediaType.ANIME) uiState.animeStats else uiState.mangaStats,
            centerContent = {
                Text(
                    text = stringResource(R.string.total_entries).format(
                        if (mediaType == MediaType.ANIME)
                            uiState.animeStats.sumOf { it.value.toInt() }
                        else uiState.mangaStats.sumOf { it.value.toInt() }
                    ),
                    modifier = Modifier
                        .width(100.dp)
                        .defaultPlaceholder(visible = isLoading),
                    textAlign = TextAlign.Center
                )
            }
        )

        Column {
            (if (mediaType == MediaType.ANIME) uiState.animeStats else uiState.mangaStats)
                .forEach {
                    SuggestionChip(
                        onClick = { },
                        label = { Text(text = it.type.localized()) },
                        modifier = Modifier.padding(horizontal = 8.dp),
                        icon = {
                            Text(
                                text = String.format("%.0f", it.value),
                                modifier = Modifier.defaultPlaceholder(visible = isLoading)
                            )
                        },
                        border = SuggestionChipDefaults.suggestionChipBorder(
                            enabled = true,
                            borderColor = it.type.primaryColor()
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
                uiState.user?.animeStatistics?.numDays.toStringOrZero()
            else uiState.userMangaStats?.days.toStringOrZero(),
            icon = R.drawable.ic_round_event_24,
            tooltip = stringResource(R.string.days),
            isLoading = isLoading
        )
        if (mediaType == MediaType.ANIME) {
            TextIconVertical(
                text = uiState.user?.animeStatistics?.numEpisodes.toStringOrZero(),
                icon = R.drawable.play_circle_outline_24,
                tooltip = stringResource(R.string.episodes),
                isLoading = isLoading
            )
        } else {
            TextIconVertical(
                text = uiState.userMangaStats?.chaptersRead.toStringOrZero(),
                icon = R.drawable.ic_round_menu_book_24,
                tooltip = stringResource(R.string.chapters),
                isLoading = isLoading
            )
            TextIconVertical(
                text = uiState.userMangaStats?.volumesRead.toStringOrZero(),
                icon = R.drawable.ic_outline_book_24,
                tooltip = stringResource(R.string.volumes),
                isLoading = isLoading
            )
        }

        TextIconVertical(
            text = if (mediaType == MediaType.ANIME)
                uiState.user?.animeStatistics?.meanScore.toStringOrZero()
            else uiState.userMangaStats?.meanScore.toStringOrZero(),
            icon = R.drawable.ic_round_details_star_24,
            tooltip = stringResource(R.string.mean_score),
            isLoading = isLoading
        )
        TextIconVertical(
            text = if (mediaType == MediaType.ANIME)
                uiState.user?.animeStatistics?.numTimesRewatched.toStringOrZero()
            else uiState.userMangaStats?.repeat.toStringOrZero(),
            icon = R.drawable.round_repeat_24,
            tooltip = if (mediaType == MediaType.ANIME) stringResource(R.string.rewatched)
            else stringResource(R.string.total_rereads),
            isLoading = isLoading
        )
    }
}