package com.axiel7.moelist.model

data class AnimeRankingResponse(
    val data: MutableList<AnimeRanking>,
    val paging: Paging
)

