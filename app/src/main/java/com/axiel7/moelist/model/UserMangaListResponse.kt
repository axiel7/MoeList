package com.axiel7.moelist.model

data class UserMangaListResponse(
    val data: MutableList<UserMangaList>,
    val paging: Paging
)