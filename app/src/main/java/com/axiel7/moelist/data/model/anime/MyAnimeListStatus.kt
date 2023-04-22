package com.axiel7.moelist.data.model.anime

import com.axiel7.moelist.data.model.media.BaseMyListStatus
import com.axiel7.moelist.data.model.media.ListStatus
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MyAnimeListStatus (
    override val status: ListStatus,
    override val score: Int = 0,
    override val updatedAt: String? = null,
    override val startDate: String? = null,
    override val endDate: String? = null,
    @SerialName("num_episodes_watched")
    override val progress: Int? = 0,
    @SerialName("is_rewatching")
    override val isRepeating: Boolean = false,
    @SerialName("num_times_rewatched")
    override val repeatCount: Int? = 0,

) : BaseMyListStatus()