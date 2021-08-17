package com.axiel7.moelist.data.model.anime

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Broadcast(
    @SerialName("day_of_the_week")
    val dayOfTheWeek: String? = null,
    @SerialName("start_time")
    val startTime: String? = null
)