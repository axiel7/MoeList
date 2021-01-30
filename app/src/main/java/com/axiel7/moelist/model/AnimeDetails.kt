package com.axiel7.moelist.model

import androidx.room.Entity
import androidx.room.Index

@Entity(tableName = "anime_details", primaryKeys = ["id"],
    indices = [Index(value = ["id"], unique = true)])
data class AnimeDetails(
    val id: Int,
    val title: String?,
    val main_picture: MainPicture?,
    val alternative_titles: AlternativeTitles?,
    val start_date: String?,
    val end_date: String?,
    val synopsis: String?,
    val mean: Float?,
    val rank: Int?,
    val popularity: Int?,
    val num_list_users: Int?,
    val num_scoring_users: Int?,
    val nsfw: String?,
    val created_at: String?,
    val updated_at: String?,
    val media_type: String?,
    val status: String?,
    val genres: List<Genre>?,
    var my_list_status: MyListStatus?,
    val num_episodes: Int?,
    val start_season: StartSeason?,
    val broadcast: Broadcast?,
    val source: String?,
    val average_episode_duration: Int?,
    val rating: String?,
    val pictures: List<MainPicture>?,
    val background: String?,
    val related_anime: List<Related>?,
    val related_manga: List<Related>?,
    val recommendations: List<Recommendations>?,
    val studios: List<Studio>?,
    val opening_themes: List<Theme>?,
    val ending_themes: List<Theme>?,
    val statistics: Statistics?
)