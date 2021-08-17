package com.axiel7.moelist.data.model.anime

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Ranking(
    @SerialName("rank")
    val rank: Int
)

