package com.axiel7.moelist.data.model.anime

import com.axiel7.moelist.data.model.MainPicture
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NodeSeasonal(
    @SerialName("id")
    val id: Int,
    @SerialName("title")
    val title: String,
    @SerialName("broadcast")
    val broadcast: Broadcast? = null,
    @SerialName("main_picture")
    val mainPicture: MainPicture? = null,
    @SerialName("num_episodes")
    val numEpisodes: Int? = null,
    @SerialName("media_type")
    val mediaType: String? = null,
    @SerialName("start_season")
    val startSeason: StartSeason? = null,
    @SerialName("status")
    val status: String? = null,
    @SerialName("mean")
    val mean: Float? = null,
)