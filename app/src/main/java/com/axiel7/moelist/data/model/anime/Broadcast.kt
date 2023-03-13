package com.axiel7.moelist.data.model.anime

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.axiel7.moelist.R
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Broadcast(
    @SerialName("day_of_the_week")
    val dayOfTheWeek: String? = null,
    @SerialName("start_time")
    val startTime: String? = null
)

@Composable
fun String.weekdayLocalized() = when (this) {
    "monday" -> stringResource(R.string.monday)
    "tuesday" -> stringResource(R.string.tuesday)
    "wednesday" -> stringResource(R.string.wednesday)
    "thursday" -> stringResource(R.string.thursday)
    "friday" -> stringResource(R.string.friday)
    "saturday" -> stringResource(R.string.saturday)
    "sunday" -> stringResource(R.string.sunday)
    else -> this
}