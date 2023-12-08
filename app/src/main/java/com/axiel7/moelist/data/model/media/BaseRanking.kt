package com.axiel7.moelist.data.model.media

import com.axiel7.moelist.data.model.anime.Ranking

interface BaseRanking {
    val node: BaseMediaNode
    val ranking: Ranking?
    val rankingType: RankingType?
}