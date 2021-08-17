package com.axiel7.moelist.data.model.manga

import com.axiel7.moelist.data.model.Paging

data class MangaRankingResponse(
    val data: MutableList<MangaRanking>,
    val paging: Paging
)