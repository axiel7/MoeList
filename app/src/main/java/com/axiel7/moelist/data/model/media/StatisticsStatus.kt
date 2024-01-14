package com.axiel7.moelist.data.model.media

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
) {
    fun toStats() = listOf(
        Stat(
            type = ListStatus.WATCHING,
            value = watching.toFloatOrNull() ?: 0f
        ),
        Stat(
            type = ListStatus.COMPLETED,
            value = completed.toFloatOrNull() ?: 0f
        ),
        Stat(
            type = ListStatus.ON_HOLD,
            value = onHold.toFloatOrNull() ?: 0f
        ),
        Stat(
            type = ListStatus.DROPPED,
            value = dropped.toFloatOrNull() ?: 0f
        ),
        Stat(
            type = ListStatus.PLAN_TO_WATCH,
            value = planToWatch.toFloatOrNull() ?: 0f
        )
    )
}