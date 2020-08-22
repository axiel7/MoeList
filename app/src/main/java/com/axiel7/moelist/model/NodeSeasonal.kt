package com.axiel7.moelist.model

data class NodeSeasonal(
    val id: Int,
    val title: String,
    val broadcast: Broadcast?,
    val main_picture: MainPicture,
    val start_season: StartSeason?,
    val mean: Float?
)