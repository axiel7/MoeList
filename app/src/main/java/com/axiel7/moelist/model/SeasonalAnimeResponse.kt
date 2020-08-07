package com.axiel7.moelist.model

data class SeasonalAnimeResponse(
    val data: MutableList<AnimeList>,
    val paging: Paging,
    val season: StartSeason
)