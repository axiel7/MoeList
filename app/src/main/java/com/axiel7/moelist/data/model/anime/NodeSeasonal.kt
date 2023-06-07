package com.axiel7.moelist.data.model.anime

import com.axiel7.moelist.data.model.media.AlternativeTitles
import com.axiel7.moelist.data.model.media.BaseMediaNode
import com.axiel7.moelist.data.model.media.MainPicture
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NodeSeasonal(
    @SerialName("id")
    override val id: Int,
    @SerialName("title")
    override val title: String,
    @SerialName("alternative_titles")
    override val alternativeTitles: AlternativeTitles? = null,
    @SerialName("broadcast")
    val broadcast: Broadcast? = null,
    @SerialName("main_picture")
    override val mainPicture: MainPicture? = null,
    @SerialName("num_episodes")
    val numEpisodes: Int? = null,
    @SerialName("media_type")
    override val mediaType: String? = null,
    @SerialName("start_season")
    val startSeason: StartSeason? = null,
    @SerialName("status")
    override val status: String? = null,
    @SerialName("mean")
    override val mean: Float? = null,
): BaseMediaNode()