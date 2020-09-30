package com.axiel7.moelist.model

import androidx.room.Entity

@Entity(primaryKeys = ["id"])
data class NodeManga(
    val id: Int,
    val title: String,
    val main_picture: MainPicture?,
    val mean: Float?,
    val media_type: String?,
    val num_volumes: Int?,
    val num_chapters: Int?,
    val num_list_users: Int?,
    val status: String?,
    val start_date: String?
)