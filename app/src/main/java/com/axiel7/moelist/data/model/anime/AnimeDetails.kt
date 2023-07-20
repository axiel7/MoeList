package com.axiel7.moelist.data.model.anime

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.manga.RelatedManga
import com.axiel7.moelist.data.model.media.AlternativeTitles
import com.axiel7.moelist.data.model.media.BaseMediaDetails
import com.axiel7.moelist.data.model.media.Genre
import com.axiel7.moelist.data.model.media.MainPicture
import com.axiel7.moelist.data.model.media.Statistics
import com.axiel7.moelist.data.model.media.localized
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@Serializable
data class AnimeDetails(
    override val id: Int = 0,
    override val title: String? = null,
    @SerialName("main_picture")
    override val mainPicture: MainPicture? = null,
    @SerialName("alternative_titles")
    override val alternativeTitles: AlternativeTitles? = null,
    @SerialName("start_date")
    override val startDate: String? = null,
    @SerialName("end_date")
    override val endDate: String? = null,
    override val synopsis: String? = null,
    override val mean: Float? = null,
    override val rank: Int? = null,
    override val popularity: Int? = null,
    @SerialName("num_list_users")
    override val numListUsers: Int? = null,
    @SerialName("num_scoring_users")
    override val numScoringUsers: Int? = null,
    override val nsfw: String? = null,
    @SerialName("created_at")
    override val createdAt: String? = null,
    @SerialName("updated_at")
    override val updatedAt: String? = null,
    @SerialName("media_type")
    override val mediaType: String? = null,
    override val status: String? = null,
    override val genres: List<Genre>? = null,
    override val pictures: List<MainPicture>? = null,
    override val background: String? = null,
    @SerialName("related_anime")
    override val relatedAnime: List<RelatedAnime>? = null,
    @SerialName("related_manga")
    override val relatedManga: List<RelatedManga>? = null,
    override val recommendations: List<Recommendations<AnimeNode>>? = null,
    @SerialName("my_list_status")
    override val myListStatus: MyAnimeListStatus? = null,
    @SerialName("num_episodes")
    val numEpisodes: Int? = null,
    @SerialName("start_season")
    val startSeason: StartSeason? = null,
    @SerialName("broadcast")
    val broadcast: Broadcast? = null,
    @SerialName("source")
    val source: String? = null,
    @SerialName("average_episode_duration")
    val averageEpisodeDuration: Int? = null,
    @SerialName("rating")
    val rating: String? = null,
    @SerialName("studios")
    val studios: List<Studio>? = null,
    @SerialName("opening_themes")
    val openingThemes: List<Theme>? = null,
    @SerialName("ending_themes")
    val endingThemes: List<Theme>? = null,
    @SerialName("statistics")
    val statistics: Statistics? = null,
) : BaseMediaDetails()

fun AnimeDetails.toAnimeNode() = AnimeNode(
    id = id,
    title = title ?: "",
    alternativeTitles = alternativeTitles,
    mainPicture = mainPicture,
    startSeason = startSeason,
    numEpisodes = numEpisodes,
    numListUsers = numListUsers,
    mediaType = mediaType,
    status = status,
    mean = mean,
)

@Composable
fun AnimeDetails.sourceLocalized() = when (this.source) {
    "original" -> stringResource(R.string.original)
    "manga" -> stringResource(R.string.manga)
    "novel" -> stringResource(R.string.novel)
    "light_novel" -> stringResource(R.string.light_novel)
    "visual_novel" -> stringResource(R.string.visual_novel)
    "game" -> stringResource(R.string.game)
    "web_manga" -> stringResource(R.string.web_manga)
    "music" -> stringResource(R.string.music)
    "4_koma_manga" -> "4-Koma ${stringResource(R.string.manga)}"
    else -> this.source
}

@Composable
fun AnimeDetails.broadcastTimeText() = buildString {
    if (broadcast?.dayOfTheWeek != null) {
        append(broadcast.dayOfTheWeek.localized())
        append(" ")
        if (broadcast.startTime != null) {
            append(broadcast.startTime)
            append(" (JST)")
        }
    } else append(stringResource(R.string.unknown))
}

@Composable
fun AnimeDetails.episodeDurationLocalized() = when {
    averageEpisodeDuration == null || averageEpisodeDuration <= 0 -> stringResource(R.string.unknown)
    averageEpisodeDuration > 3600 -> {
        val duration = averageEpisodeDuration.toDuration(DurationUnit.SECONDS)
        duration.toComponents { hours, minutes, _, _ ->
            "$hours ${stringResource(R.string.hour_abbreviation)} $minutes ${stringResource(R.string.minutes_abbreviation)}"
        }
    }
    averageEpisodeDuration == 3600 -> "1 ${stringResource(R.string.hour_abbreviation)}"
    averageEpisodeDuration >= 60 -> "${averageEpisodeDuration / 60} ${stringResource(R.string.minutes_abbreviation)}"
    averageEpisodeDuration < 60 -> "<1 ${stringResource(R.string.minutes_abbreviation)}"
    else -> stringResource(R.string.unknown)
}