package com.axiel7.moelist.data.model.anime

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.media.WeekDay
import com.axiel7.moelist.data.model.media.localized
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Broadcast(
    @SerialName("day_of_the_week")
    val dayOfTheWeek: WeekDay? = null,
    @SerialName("start_time")
    val startTime: String? = null
)

@Composable
fun Broadcast?.dayTimeText() = buildString {
    if (this@dayTimeText != null) {
        if (dayOfTheWeek != null) append(dayOfTheWeek.localized())
        if (startTime != null) append(" $startTime")
        if (dayOfTheWeek == null && startTime == null)
            append(stringResource(R.string.unknown))
    }
    else append(stringResource(R.string.unknown))
}