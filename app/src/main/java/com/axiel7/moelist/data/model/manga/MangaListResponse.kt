package com.axiel7.moelist.data.model.manga

import com.axiel7.moelist.data.model.Paging

data class MangaListResponse (
    val data: MutableList<MangaList>,
    val paging: Paging
)