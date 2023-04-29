package com.axiel7.moelist.data.model.manga

import com.axiel7.moelist.data.model.media.BaseMyListStatus
import com.axiel7.moelist.data.model.media.ListStatus
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MyMangaListStatus (
    override val status: ListStatus,
    override val score: Int = 0,
    override val updatedAt: String? = null,
    override val startDate: String? = null,
    override val endDate: String? = null,
    @SerialName("num_chapters_read")
    override val progress: Int?,
    @SerialName("num_volumes_read")
    val numVolumesRead: Int = 0,
    @SerialName("is_rereading")
    override val isRepeating: Boolean = false,
    @SerialName("num_times_reread")
    override val repeatCount: Int? = 0,

) : BaseMyListStatus()