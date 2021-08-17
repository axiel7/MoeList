package com.axiel7.moelist.data.model.anime

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AnimeRanking(
    @SerialName("node")
    val node: AnimeNode,
    @SerialName("ranking")
    val ranking: Ranking? = null,
    @SerialName("ranking_type")
    var rankingType: String? = null,
)

