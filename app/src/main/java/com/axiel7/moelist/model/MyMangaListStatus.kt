package com.axiel7.moelist.model

data class MyMangaListStatus (
    val status: String,
    val score: Int,
    val num_chapters_read: Int,
    val num_volumes_read: Int,
    val is_rereading: Boolean,
    val updated_at: String
)