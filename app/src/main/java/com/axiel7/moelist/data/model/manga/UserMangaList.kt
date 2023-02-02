package com.axiel7.moelist.data.model.manga

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.axiel7.moelist.data.model.media.BaseUserMediaList
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Entity
@Serializable
data class UserMangaList(
    @SerialName("node") @PrimaryKey
    override val node: MangaNode,
    @SerialName("list_status")
    var listStatus: MyMangaListStatus? = null,
    @SerialName("status")
    override var status: String? = null
) : BaseUserMediaList<MangaNode>()