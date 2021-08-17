package com.axiel7.moelist.data.model.anime

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Entity
@Serializable
data class UserAnimeList(
    @SerialName("node") @PrimaryKey
    val node: AnimeNode,
    @SerialName("list_status")
    val listStatus: MyAnimeListStatus? = null,
    @SerialName("status")
    var status: String? = null
)