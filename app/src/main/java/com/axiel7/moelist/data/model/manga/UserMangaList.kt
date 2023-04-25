package com.axiel7.moelist.data.model.manga

import com.axiel7.moelist.data.model.media.BaseUserMediaList
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserMangaList(
    @SerialName("node")
    override val node: MangaNode,
    @SerialName("list_status")
    override var listStatus: MyMangaListStatus? = null,
    @SerialName("status")
    override var status: String? = null
) : BaseUserMediaList<MangaNode>()