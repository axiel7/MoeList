package com.axiel7.moelist.data.model.anime

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.res.stringResource
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.manga.RelatedManga
import com.axiel7.moelist.data.model.media.AlternativeTitles
import com.axiel7.moelist.data.model.media.BaseMediaDetails
import com.axiel7.moelist.data.model.media.Genre
import com.axiel7.moelist.data.model.media.MainPicture
import com.axiel7.moelist.data.model.media.MediaFormat
import com.axiel7.moelist.data.model.media.MediaStatus
import com.axiel7.moelist.data.model.media.Statistics
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@Immutable
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
    override val mediaFormat: MediaFormat? = null,
    override val status: MediaStatus? = null,
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
    val source: AnimeSource? = null,
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
) : BaseMediaDetails() {

    fun toAnimeNode() = AnimeNode(
        id = id,
        title = title.orEmpty(),
        alternativeTitles = alternativeTitles,
        mainPicture = mainPicture,
        startSeason = startSeason,
        numEpisodes = numEpisodes,
        numListUsers = numListUsers,
        mediaFormat = mediaFormat,
        status = status,
        mean = mean,
    )

    @Composable
    fun episodeDurationLocalized() = when {
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
}