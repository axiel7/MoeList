package com.axiel7.moelist.data.model.anime

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.axiel7.moelist.data.model.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Entity @Serializable
data class AnimeDetails(
    @PrimaryKey @SerialName("id")
    val id: Int = 0,
    @SerialName("title")
    val title: String? = null,
    @SerialName("main_picture")
    val mainPicture: MainPicture? = null,
    @SerialName("alternative_titles")
    val alternativeTitles: AlternativeTitles? = null,
    @SerialName("start_date")
    val startDate: String? = null,
    @SerialName("end_date")
    val endDate: String? = null,
    @SerialName("synopsis")
    val synopsis: String? = null,
    @SerialName("mean")
    val mean: Float? = null,
    @SerialName("rank")
    val rank: Int? = null,
    @SerialName("popularity")
    val popularity: Int? = null,
    @SerialName("num_list_users")
    val numListUsers: Int? = null,
    @SerialName("num_scoring_users")
    val numScoringUsers: Int? = null,
    @SerialName("nsfw")
    val nsfw: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null,
    @SerialName("media_type")
    val mediaType: String? = null,
    @SerialName("status")
    val status: String? = null,
    @SerialName("genres")
    val genres: List<Genre>? = null,
    @SerialName("my_list_status")
    val myListStatus: MyAnimeListStatus? = null,
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
    @SerialName("pictures")
    val pictures: List<MainPicture>? = null,
    @SerialName("background")
    val background: String? = null,
    @SerialName("related_anime")
    val relatedAnime: List<Related>? = null,
    @SerialName("related_manga")
    val relatedManga: List<Related>? = null,
    @SerialName("recommendations")
    val recommendations: List<Recommendations>? = null,
    @SerialName("studios")
    val studios: List<Studio>? = null,
    @SerialName("opening_themes")
    val openingThemes: List<Theme>? = null,
    @SerialName("ending_themes")
    val endingThemes: List<Theme>? = null,
    @SerialName("statistics")
    val statistics: Statistics? = null,

    @SerialName("message")
    val message: String? = null,
    @SerialName("error")
    val error: String? = null,

    )