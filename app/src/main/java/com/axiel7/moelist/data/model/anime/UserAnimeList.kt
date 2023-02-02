package com.axiel7.moelist.data.model.anime

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.axiel7.moelist.data.model.media.BaseUserMediaList
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Entity
@Serializable
data class UserAnimeList(
    @SerialName("node") @PrimaryKey
    override val node: AnimeNode,
    @SerialName("list_status")
    var listStatus: MyAnimeListStatus? = null,
    @SerialName("status")
    override var status: String? = null
) : BaseUserMediaList<AnimeNode>()