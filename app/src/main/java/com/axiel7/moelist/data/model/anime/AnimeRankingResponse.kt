package com.axiel7.moelist.data.model.anime

import com.axiel7.moelist.data.model.Paging

data class AnimeRankingResponse(
    val data: MutableList<AnimeRanking>,
    val paging: Paging
)

