package com.axiel7.moelist.model

data class NodeSeasonal(
    val id: Int,
    val title: String,
    val broadcast: Broadcast?,
    val main_picture: MainPicture,
    val num_episodes: Int?,
    val media_type: String?,
    val start_season: StartSeason?,
    val mean: Float?
)