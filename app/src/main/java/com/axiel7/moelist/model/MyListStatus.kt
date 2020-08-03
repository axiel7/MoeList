package com.axiel7.moelist.model

data class MyListStatus (
    val status: String,
    val score: Int,
    val num_episodes_watched: Int,
    val is_rewatching: Boolean,
    val updated_at: String
)