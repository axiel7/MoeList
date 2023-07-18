package com.axiel7.moelist.data.model.anime

import androidx.annotation.IntRange
import com.axiel7.moelist.data.model.media.BaseMyListStatus
import com.axiel7.moelist.data.model.media.ListStatus
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MyAnimeListStatus(

    @SerialName("status")
    override val status: ListStatus,

    @SerialName("score")
    @IntRange(0, 10)
    override val score: Int = 0,

    @SerialName("updated_at")
    override val updatedAt: String? = null,

    @SerialName("start_date")
    override val startDate: String? = null,

    @SerialName("end_date")
    override val endDate: String? = null,

    @SerialName("num_episodes_watched")
    override val progress: Int? = 0,

    @SerialName("is_rewatching")
    override val isRepeating: Boolean = false,

    @SerialName("num_times_rewatched")
    override val repeatCount: Int? = 0,

    @SerialName("rewatch_value")
    @IntRange(0, 5)
    override val repeatValue: Int? = 0,

    @SerialName("priority")
    @IntRange(0, 2)
    override val priority: Int = 0,

    @SerialName("tags")
    override val tags: List<String>? = null,

    @SerialName("comments")
    override val comments: String? = null

) : BaseMyListStatus()