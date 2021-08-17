package com.axiel7.moelist.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StatisticsStatus(
    @SerialName("watching")
    val watching: String,
    @SerialName("completed")
    val completed: String,
    @SerialName("on_hold")
    val onHold: String,
    @SerialName("dropped")
    val dropped: String,
    @SerialName("plan_to_watch")
    val planToWatch: String
)