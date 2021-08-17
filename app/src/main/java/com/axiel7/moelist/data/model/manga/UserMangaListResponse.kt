package com.axiel7.moelist.data.model.manga

import com.axiel7.moelist.data.model.Paging

data class UserMangaListResponse(
    val data: MutableList<UserMangaList>,
    val paging: Paging
)