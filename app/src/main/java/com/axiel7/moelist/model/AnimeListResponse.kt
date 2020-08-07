package com.axiel7.moelist.model

data class AnimeListResponse (
    val data: MutableList<AnimeList>,
    val paging: Paging
)