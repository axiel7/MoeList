package com.axiel7.moelist.data.model.media

import androidx.compose.runtime.Stable
import com.axiel7.moelist.data.model.anime.Ranking

@Stable
interface BaseRanking {
    val node: BaseMediaNode
    val ranking: Ranking?
    val rankingType: RankingType?
}