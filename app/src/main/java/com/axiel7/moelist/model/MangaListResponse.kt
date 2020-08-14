package com.axiel7.moelist.model

data class MangaListResponse (
    val data: MutableList<MangaList>,
    val paging: Paging
)