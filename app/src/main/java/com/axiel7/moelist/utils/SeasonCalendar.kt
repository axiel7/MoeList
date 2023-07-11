package com.axiel7.moelist.utils

import com.axiel7.moelist.data.model.anime.Season
import com.axiel7.moelist.data.model.anime.StartSeason
import com.axiel7.moelist.data.model.media.WeekDay
import java.time.ZoneId
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

object SeasonCalendar {
    private val calendar: Calendar by lazy {
        Calendar.getInstance(Locale.getDefault())
    }
    private val jpCalendar =
        Calendar.getInstance(TimeZone.getTimeZone("Asia/Tokyo"), Locale.ENGLISH)

    /**
     * The current month from 0 to 11
     */
    private val month = calendar.get(Calendar.MONTH)
    private val weekDay = calendar.get(Calendar.DAY_OF_WEEK)

    val currentYear = calendar.get(Calendar.YEAR)

    val currentSeason = when (month) {
        0, 1, 11 -> Season.WINTER
        2, 3, 4 -> Season.SPRING
        5, 6, 7 -> Season.SUMMER
        8, 9, 10 -> Season.FALL
        else -> Season.SPRING
    }

    val currentStartSeason = StartSeason(
        // if december, the season is next year winter
        year = if (month == 11) currentYear + 1 else currentYear,
        season = currentSeason
    )

    val currentWeekday = when (weekDay) {
        2 -> WeekDay.MONDAY
        3 -> WeekDay.TUESDAY
        4 -> WeekDay.WEDNESDAY
        5 -> WeekDay.THURSDAY
        6 -> WeekDay.FRIDAY
        7 -> WeekDay.SATURDAY
        1 -> WeekDay.SUNDAY
        else -> WeekDay.MONDAY
    }

    val currentJapanHour get() = jpCalendar.get(Calendar.HOUR_OF_DAY)

    val currentJapanWeekday = when (jpCalendar.get(Calendar.DAY_OF_WEEK)) {
        2 -> WeekDay.MONDAY
        3 -> WeekDay.TUESDAY
        4 -> WeekDay.WEDNESDAY
        5 -> WeekDay.THURSDAY
        6 -> WeekDay.FRIDAY
        7 -> WeekDay.SATURDAY
        1 -> WeekDay.SUNDAY
        else -> WeekDay.MONDAY
    }

    val japanZoneId: ZoneId = ZoneId.of("Asia/Tokyo")
}