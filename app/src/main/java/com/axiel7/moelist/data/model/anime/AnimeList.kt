package com.axiel7.moelist.data.model.anime

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AnimeList(
    @SerialName("node")
    val node: AnimeNode
)
