package com.axiel7.moelist.model

data class MangaRankingResponse(
    val data: MutableList<MangaRanking>,
    val paging: Paging
)