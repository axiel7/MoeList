package com.axiel7.moelist.model

import androidx.room.Entity

@Entity(primaryKeys = ["id"])
data class Node(
    val id: Int,
    val title: String,
    val main_picture: MainPicture?,
    val start_season: StartSeason?,
    val num_episodes: Int?,
    val num_list_users: Int?,
    val media_type: String?,
    val status: String?,
    val mean: Float?
)