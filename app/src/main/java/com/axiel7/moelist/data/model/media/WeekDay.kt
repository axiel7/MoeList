package com.axiel7.moelist.data.model.media

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.axiel7.moelist.R
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class WeekDay(val value: String) {
    @SerialName("monday")
    MONDAY("monday"),

    @SerialName("tuesday")
    TUESDAY("tuesday"),

    @SerialName("wednesday")
    WEDNESDAY("wednesday"),

    @SerialName("thursday")
    THURSDAY("thursday"),

    @SerialName("friday")
    FRIDAY("friday"),

    @SerialName("saturday")
    SATURDAY("saturday"),

    @SerialName("sunday")
    SUNDAY("sunday")
}

fun WeekDay.numeric() = when (this) {
    WeekDay.MONDAY -> 1
    WeekDay.TUESDAY -> 2
    WeekDay.WEDNESDAY -> 3
    WeekDay.THURSDAY -> 4
    WeekDay.FRIDAY -> 5
    WeekDay.SATURDAY -> 6
    WeekDay.SUNDAY -> 7
}

@Composable
fun WeekDay.localized() = when (this) {
    WeekDay.MONDAY -> stringResource(R.string.monday)
    WeekDay.TUESDAY -> stringResource(R.string.tuesday)
    WeekDay.WEDNESDAY -> stringResource(R.string.wednesday)
    WeekDay.THURSDAY -> stringResource(R.string.thursday)
    WeekDay.FRIDAY -> stringResource(R.string.friday)
    WeekDay.SATURDAY -> stringResource(R.string.saturday)
    WeekDay.SUNDAY -> stringResource(R.string.sunday)
}