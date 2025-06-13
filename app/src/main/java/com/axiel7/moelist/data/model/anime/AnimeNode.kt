package com.axiel7.moelist.data.model.anime

import com.axiel7.moelist.data.model.media.AlternativeTitles
import com.axiel7.moelist.data.model.media.BaseMediaNode
import com.axiel7.moelist.data.model.media.BasicMyListStatus
import com.axiel7.moelist.data.model.media.MainPicture
import com.axiel7.moelist.data.model.media.MediaFormat
import com.axiel7.moelist.data.model.media.MediaStatus
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AnimeNode(
    @SerialName("id")
    override val id: Int,
    @SerialName("title")
    override val title: String,
    @SerialName("alternative_titles")
    override val alternativeTitles: AlternativeTitles? = null,
    @SerialName("main_picture")
    override val mainPicture: MainPicture? = null,
    @SerialName("start_season")
    val startSeason: StartSeason? = null,
    @SerialName("broadcast")
    val broadcast: Broadcast? = null,
    @SerialName("num_episodes")
    val numEpisodes: Int? = null,
    @SerialName("num_list_users")
    override val numListUsers: Int? = null,
    @SerialName("media_type")
    override val mediaFormat: MediaFormat? = null,
    @SerialName("status")
    override val status: MediaStatus? = null,
    @SerialName("mean")
    override val mean: Float? = null,
    @SerialName("my_list_status")
    override val myListStatus: BasicMyListStatus? = null,
) : BaseMediaNode()