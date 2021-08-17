package com.axiel7.moelist.data.model.anime

import com.axiel7.moelist.data.model.Paging

data class UserAnimeListResponse(
    val data: MutableList<UserAnimeList>,
    val paging: Paging
)