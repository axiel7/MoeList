package com.axiel7.moelist.data.model.anime

import android.text.format.DateFormat
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.media.WeekDay
import com.axiel7.moelist.data.model.media.localized
import com.axiel7.moelist.data.model.media.numeric
import com.axiel7.moelist.utils.DateUtils
import com.axiel7.moelist.utils.DateUtils.getNextDayOfWeek
import com.axiel7.moelist.utils.DateUtils.secondsToLegibleText
import com.axiel7.moelist.utils.SeasonCalendar
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.absoluteValue

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
    } else append(stringResource(R.string.unknown))
}

@Composable
fun Broadcast.remainingText() = if (startTime != null && dayOfTheWeek != null) {
    val remaining = remaining()
    if (remaining > 0) remaining.secondsToLegibleText()
    else remaining.absoluteValue.secondsToLegibleText()
} else ""

@Composable
fun Broadcast.airingInString() = if (startTime != null && dayOfTheWeek != null) {
    val remaining = remaining()
    if (remaining > 0) {
        stringResource(R.string.airing_in).format(remaining.secondsToLegibleText())
    } else stringResource(R.string.aired_ago).format(remaining.absoluteValue.secondsToLegibleText())
} else ""

@Composable
fun Broadcast.airingInShortString() = if (startTime != null && dayOfTheWeek != null) {
    val remaining = remaining()
    if (remaining > 0) remaining.secondsToLegibleText()
    else stringResource(R.string.ago).format(remaining.absoluteValue.secondsToLegibleText())
} else ""

fun Broadcast.nextAiringDayFormatted() =
    dateTimeUntilNextBroadcast()?.format(
        DateTimeFormatter.ofPattern(
            DateFormat.getBestDateTimePattern(Locale.getDefault(), "EE, d MMM HH:mm")
        )
    )

fun Broadcast.remaining() =
    secondsUntilNextBroadcast() - LocalDateTime.now().toEpochSecond(DateUtils.defaultZoneOffset)

fun Broadcast.secondsUntilNextBroadcast() =
    dateTimeUntilNextBroadcast()?.toEpochSecond(DateUtils.defaultZoneOffset) ?: 0

fun Broadcast.dateTimeUntilNextBroadcast(): LocalDateTime? =
    if (startTime != null && dayOfTheWeek != null) {
        val airingDay = LocalDate.now().getNextDayOfWeek(DayOfWeek.of(dayOfTheWeek.numeric()))
        val airingTime = LocalTime.parse(startTime)
        LocalDateTime.of(airingDay, airingTime)
            .atZone(SeasonCalendar.japanZoneId)
            .withZoneSameInstant(ZoneId.systemDefault())
            .toLocalDateTime()
    } else null