package com.axiel7.moelist.data.model.anime

import com.axiel7.moelist.data.model.media.BaseMediaNode
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Recommendations<T: BaseMediaNode>(
    @SerialName("node")
    val node: T,
    @SerialName("num_recommendations")
    val numRecommendations: Int
)