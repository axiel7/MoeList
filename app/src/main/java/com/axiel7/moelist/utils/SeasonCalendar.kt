package com.axiel7.moelist.utils

import java.util.*

object SeasonCalendar {
    private var calendar: Calendar = Calendar.getInstance()
    private val month = calendar.get(Calendar.MONTH)

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
}