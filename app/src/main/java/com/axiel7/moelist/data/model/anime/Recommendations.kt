package com.axiel7.moelist.data.model.anime

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Recommendations(
    @SerialName("node")
    val node: AnimeNode,
    @SerialName("num_recommendations")
    val numRecommendations: Int
)