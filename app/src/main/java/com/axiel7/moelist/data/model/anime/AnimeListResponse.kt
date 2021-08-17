package com.axiel7.moelist.data.model.anime

import com.axiel7.moelist.data.model.Paging

data class AnimeListResponse (
    val data: MutableList<AnimeList>,
    val paging: Paging
)