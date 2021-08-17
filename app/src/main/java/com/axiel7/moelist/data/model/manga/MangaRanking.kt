package com.axiel7.moelist.data.model.manga

import com.axiel7.moelist.data.model.anime.Ranking
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MangaRanking(
    @SerialName("node")
    val node: MangaNode,
    @SerialName("ranking")
    val ranking: Ranking? = null,
    @SerialName("ranking_type")
    var rankingType: String? = null,
)

