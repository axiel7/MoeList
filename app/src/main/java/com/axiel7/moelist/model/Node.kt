package com.axiel7.moelist.model

data class Node(
    val id: Int,
    val title: String,
    val main_picture: MainPicture,
    val start_season: StartSeason?,
    val num_episodes: Int?,
    val media_type: String?,
    val status: String?
)