package com.axiel7.moelist.utils

import com.axiel7.moelist.data.model.Season
import java.util.*

object SeasonCalendar {
    private val calendar = Calendar.getInstance(Locale.ENGLISH)
    private val jpCalendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Tokyo"), Locale.ENGLISH)
    private val month = calendar.get(Calendar.MONTH)
    private val weekDay = calendar.get(Calendar.DAY_OF_WEEK)
    private val winterMonths = intArrayOf(0, 1, 2)
    private val springMonths = intArrayOf(3, 4, 5)
    private val summerMonths = intArrayOf(6, 7, 8)
    private val fallMonths = intArrayOf(9, 10, 11)

    val currentYear = calendar.get(Calendar.YEAR)

    val currentSeason: Season get() {
        val isWinter = winterMonths.contains(month)
        val isSpring = springMonths.contains(month)
        val isSummer = summerMonths.contains(month)
        val isFall = fallMonths.contains(month)

        return when {
            isSpring -> Season.SPRING
            isSummer -> Season.SUMMER
            isWinter -> Season.WINTER
            isFall -> Season.FALL
            else -> Season.SPRING
        }
    }

    val currentSeasonStr get() = currentSeason.name.lowercase()

    val currentWeekday = when(weekDay) {
        2 -> "monday"
        3 -> "tuesday"
        4 -> "wednesday"
        5 -> "thursday"
        6 -> "friday"
        7 -> "saturday"
        1 -> "sunday"
        else -> "monday"
    }

    val currentJapanHour = jpCalendar.get(Calendar.HOUR_OF_DAY)

    val currentJapanWeekday = when(jpCalendar.get(Calendar.DAY_OF_WEEK)) {
        2 -> "monday"
        3 -> "tuesday"
        4 -> "wednesday"
        5 -> "thursday"
        6 -> "friday"
        7 -> "saturday"
        1 -> "sunday"
        else -> "monday"
    }

}