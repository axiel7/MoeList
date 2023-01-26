package com.axiel7.moelist.data.model.anime

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.axiel7.moelist.R
import com.axiel7.moelist.utils.SeasonCalendar
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.absoluteValue

@Serializable
data class AnimeSeasonal(
    @SerialName("node")
    val node: NodeSeasonal
)
@Composable
fun AnimeSeasonal.airingInValue(): String {
    val startTime = node.broadcast?.startTime
    val weekDay = node.broadcast?.dayOfTheWeek

    return if (startTime != null && weekDay != null) {
        val startHour = LocalTime.parse(startTime, DateTimeFormatter.ISO_TIME).hour
        val remaining = startHour - SeasonCalendar.currentJapanHour
        if (SeasonCalendar.currentJapanWeekday == weekDay && remaining > 0)
            stringResource(R.string.airing_in).format(remaining)
        else stringResource(R.string.aired_ago).format(remaining.absoluteValue)
    } else {
        stringResource(R.string.airing_in).format("??")
    }
}

