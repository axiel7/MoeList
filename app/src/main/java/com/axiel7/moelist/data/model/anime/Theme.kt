package com.axiel7.moelist.data.model.anime

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Theme(
    @SerialName("id")
    val id: Int,
    @SerialName("anime_id")
    val animeId: Int,
    @SerialName("text")
    val text: String
)
