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
    private val month = calendar[Calendar.MONTH]
    private val weekDay = calendar[Calendar.DAY_OF_WEEK]

    val currentYear = calendar[Calendar.YEAR]

    val currentSeason = when (month) {
        0, 1, 11 -> Season.WINTER
        2, 3, 4 -> Season.SPRING
        5, 6, 7 -> Season.SUMMER
        8, 9, 10 -> Season.FALL
        else -> Season.SPRING
    }

    private val nextSeason = when (currentSeason) {
        Season.WINTER -> Season.SPRING
        Season.SPRING -> Season.SUMMER
        Season.SUMMER -> Season.FALL
        Season.FALL -> Season.WINTER
    }

    private val prevSeason = when (currentSeason) {
        Season.WINTER -> Season.FALL
        Season.SPRING -> Season.WINTER
        Season.SUMMER -> Season.SPRING
        Season.FALL -> Season.SUMMER
    }

    val currentStartSeason = StartSeason(
        // if december, the season is next year winter
        year = if (month == 11) currentYear + 1 else currentYear,
        season = currentSeason
    )

    val nextStartSeason = StartSeason(
        year = if (nextSeason == Season.WINTER) currentYear + 1 else currentYear,
        season = nextSeason
    )

    val prevStartSeason = StartSeason(
        year = if (prevSeason == Season.FALL) currentYear - 1 else currentYear,
        season = prevSeason
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

    val currentJapanWeekday = when (jpCalendar[Calendar.DAY_OF_WEEK]) {
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