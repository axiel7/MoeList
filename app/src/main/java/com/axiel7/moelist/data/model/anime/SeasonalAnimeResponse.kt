package com.axiel7.moelist.data.model.anime

import com.axiel7.moelist.data.model.Paging

data class SeasonalAnimeResponse(
    val data: MutableList<AnimeSeasonal>,
    val paging: Paging?,
    val season: StartSeason?
)