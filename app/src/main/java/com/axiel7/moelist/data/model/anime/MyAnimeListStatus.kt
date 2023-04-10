package com.axiel7.moelist.data.model.anime

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MyAnimeListStatus (
    @SerialName("status")
    val status: String = "",
    @SerialName("score")
    val score: Int = 0,
    @SerialName("num_episodes_watched")
    val numEpisodesWatched: Int = 0,
    @SerialName("is_rewatching")
    val isRewatching: Boolean = false,
    @SerialName("updated_at")
    val updatedAt: String? = null,
    @SerialName("start_date")
    val startDate: String? = null,
    @SerialName("finish_date")
    val endDate: String? = null,
    @SerialName("num_times_rewatched")
    val numTimesRewatched: Int = 0,

    @SerialName("message")
    val message: String? = null,
    @SerialName("error")
    val error: String? = null
)