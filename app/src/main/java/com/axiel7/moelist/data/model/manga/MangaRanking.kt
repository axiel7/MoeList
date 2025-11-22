package com.axiel7.moelist.data.model.manga

import androidx.compose.runtime.Stable
import com.axiel7.moelist.data.model.anime.Ranking
import com.axiel7.moelist.data.model.media.BaseRanking
import com.axiel7.moelist.data.model.media.RankingType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@Stable
data class MangaRanking(
    @SerialName("node")
    override val node: MangaNode,
    @SerialName("ranking")
    override val ranking: Ranking? = null,
    @SerialName("ranking_type")
    override val rankingType: RankingType? = null,
) : BaseRanking

