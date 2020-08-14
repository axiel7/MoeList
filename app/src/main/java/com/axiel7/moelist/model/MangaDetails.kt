package com.axiel7.moelist.model

import androidx.room.Entity
import androidx.room.Index

@Entity(tableName = "manga_details", primaryKeys = ["id"],
    indices = [Index(value = ["id"], unique = true)])
data class MangaDetails(
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
    var my_list_status: MyMangaListStatus?,
    val num_volumes: Int?,
    val num_chapters: Int?,
    val authors: List<Author>?,
    val pictures: List<MainPicture>?,
    val background: String?,
    val related_anime: List<Related>?,
    val related_manga: List<Related>?,
    val recommendations: List<Recommendations>?,
    val serialization: List<Serialization>?
)