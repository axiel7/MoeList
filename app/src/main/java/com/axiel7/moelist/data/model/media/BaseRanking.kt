package com.axiel7.moelist.data.model.media

import com.axiel7.moelist.data.model.anime.Ranking

abstract class BaseRanking {
    abstract val node: BaseMediaNode
    abstract val ranking: Ranking?
    abstract val rankingType: RankingType?
}