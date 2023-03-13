package com.axiel7.moelist.data.model.anime

import com.axiel7.moelist.data.model.media.BaseUserMediaList
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserAnimeList(
    @SerialName("node")
    override val node: AnimeNode,
    @SerialName("list_status")
    var listStatus: MyAnimeListStatus? = null,
    @SerialName("status")
    override var status: String? = null
) : BaseUserMediaList<AnimeNode>()