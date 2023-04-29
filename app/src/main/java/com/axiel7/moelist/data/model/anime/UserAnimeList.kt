package com.axiel7.moelist.data.model.anime

import com.axiel7.moelist.data.model.media.BaseUserMediaList
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserAnimeList(
    @SerialName("node")
    override val node: AnimeNode,
    @SerialName("list_status")
    override val listStatus: MyAnimeListStatus? = null,
    @SerialName("status")
    override val status: String? = null
) : BaseUserMediaList<AnimeNode>()