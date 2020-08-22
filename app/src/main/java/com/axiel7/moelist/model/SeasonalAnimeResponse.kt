package com.axiel7.moelist.model

data class SeasonalAnimeResponse(
    val data: MutableList<SeasonalList>,
    val paging: Paging,
    val season: StartSeason
)