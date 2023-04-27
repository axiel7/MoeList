package com.axiel7.moelist.utils

import com.axiel7.moelist.data.model.anime.Season
import com.axiel7.moelist.data.model.anime.StartSeason
import com.axiel7.moelist.data.model.media.WeekDay
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

object SeasonCalendar {
    private val calendar: Calendar by lazy {
        Calendar.getInstance(Locale.getDefault())
    }
    private val jpCalendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Tokyo"), Locale.ENGLISH)
    private val month = calendar.get(Calendar.MONTH)
    private val weekDay = calendar.get(Calendar.DAY_OF_WEEK)
    private val winterMonths = intArrayOf(0, 1, 2)
    private val springMonths = intArrayOf(3, 4, 5)
    private val summerMonths = intArrayOf(6, 7, 8)
    private val fallMonths = intArrayOf(9, 10, 11)

    val currentYear = calendar.get(Calendar.YEAR)

    val currentSeason = when {
        springMonths.contains(month) -> Season.SPRING
        summerMonths.contains(month) -> Season.SUMMER
        winterMonths.contains(month) -> Season.WINTER
        fallMonths.contains(month) -> Season.FALL
        else -> Season.SPRING
    }

    val currentStartSeason = StartSeason(currentYear, currentSeason)

    val currentWeekday = when(weekDay) {
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

    val currentJapanWeekday = when(jpCalendar.get(Calendar.DAY_OF_WEEK)) {
        2 -> WeekDay.MONDAY
        3 -> WeekDay.TUESDAY
        4 -> WeekDay.WEDNESDAY
        5 -> WeekDay.THURSDAY
        6 -> WeekDay.FRIDAY
        7 -> WeekDay.SATURDAY
        1 -> WeekDay.SUNDAY
        else -> WeekDay.MONDAY
    }
}