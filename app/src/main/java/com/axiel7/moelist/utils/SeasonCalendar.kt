package com.axiel7.moelist.utils

import java.util.*

object SeasonCalendar {
    private val calendar: Calendar = Calendar.getInstance(Locale.ENGLISH)
    private val jpCalendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Tokyo"), Locale.ENGLISH)
    private val month = calendar.get(Calendar.MONTH)
    private val weekDay = calendar.get(Calendar.DAY_OF_WEEK)

    fun getCurrentYear(): Int {
        return calendar.get(Calendar.YEAR)
    }

    fun getCurrentSeason(): String {
        val winterMonths = intArrayOf(0, 1, 2)
        val springMonths = intArrayOf(3, 4, 5)
        val summerMonths = intArrayOf(6, 7, 8)
        val fallMonths = intArrayOf(9, 10, 11)

        val isWinter = winterMonths.contains(month)
        val isSpring = springMonths.contains(month)
        val isSummer = summerMonths.contains(month)
        val isFall = fallMonths.contains(month)

        return when {
            isSpring -> { "spring" }
            isSummer -> { "summer" }
            isWinter -> { "winter" }
            isFall -> { "fall" }
            else -> { "error" }
        }
    }
    fun getCurrentWeekday(): String {
        return when(weekDay) {
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
    fun getCurrentJapanHour(): Int {
        val jpCalendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Tokyo"))
        return jpCalendar.get(Calendar.HOUR_OF_DAY)
    }
    fun getCurrentJapanWeekday(): String {
        return when(jpCalendar.get(Calendar.DAY_OF_WEEK)) {
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
}