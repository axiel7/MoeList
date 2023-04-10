package com.axiel7.moelist.data.model.manga

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MyMangaListStatus (
    @SerialName("status")
    val status: String = "",
    @SerialName("score")
    val score: Int = 0,
    @SerialName("num_chapters_read")
    val numChaptersRead: Int = 0,
    @SerialName("num_volumes_read")
    val numVolumesRead: Int = 0,
    @SerialName("is_rereading")
    val isRereading: Boolean = false,
    @SerialName("updated_at")
    val updatedAt: String? = null,
    @SerialName("start_date")
    val startDate: String? = null,
    @SerialName("finish_date")
    val endDate: String? = null,
    @SerialName("num_times_reread")
    val numTimesReread: Int = 0,

    @SerialName("message")
    val message: String? = null,
    @SerialName("error")
    val error: String? = null
)