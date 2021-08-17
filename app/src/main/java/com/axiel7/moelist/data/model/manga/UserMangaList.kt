package com.axiel7.moelist.data.model.manga

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Entity
@Serializable
data class UserMangaList(
    @SerialName("node") @PrimaryKey
    val node: MangaNode,
    @SerialName("list_status")
    val listStatus: MyMangaListStatus? = null,
    @SerialName("status")
    var status: String? = null
)