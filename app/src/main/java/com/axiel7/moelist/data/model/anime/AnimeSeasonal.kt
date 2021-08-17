package com.axiel7.moelist.data.model.anime

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AnimeSeasonal(
    @SerialName("node")
    val node: NodeSeasonal
)

