package com.axiel7.moelist.data.model.manga

import com.axiel7.moelist.data.model.media.BaseMyListStatus
import com.axiel7.moelist.data.model.media.ListStatus
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MyMangaListStatus(
    override val status: ListStatus,
    override val score: Int = 0,
    @SerialName("updated_at")
    override val updatedAt: String? = null,
    @SerialName("start_date")
    override val startDate: String? = null,
    @SerialName("end_date")
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

fun MyMangaListStatus.isUsingVolumeProgress() =
    numVolumesRead > 0 && (progress == null || progress == 0)