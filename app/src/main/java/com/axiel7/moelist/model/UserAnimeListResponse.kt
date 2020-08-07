package com.axiel7.moelist.model

data class UserAnimeListResponse(
    val data: MutableList<UserAnimeList>,
    val paging: Paging
)