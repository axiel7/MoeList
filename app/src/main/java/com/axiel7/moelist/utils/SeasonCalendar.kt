package com.axiel7.moelist.utils

import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.anime.Season
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

    val currentSeason: Season by lazy {
        val isWinter = winterMonths.contains(month)
        val isSpring = springMonths.contains(month)
        val isSummer = summerMonths.contains(month)
        val isFall = fallMonths.contains(month)

        when {
            isSpring -> Season.SPRING
            isSummer -> Season.SUMMER
            isWinter -> Season.WINTER
            isFall -> Season.FALL
            else -> Season.SPRING
        }
    }

    val currentSeasonStr get() = currentSeason.name.lowercase()

    val currentWeekday = when(weekDay) {
        2 -> Constants.MONDAY
        3 -> Constants.TUESDAY
        4 -> Constants.WEDNESDAY
        5 -> Constants.THURSDAY
        6 -> Constants.FRIDAY
        7 -> Constants.SATURDAY
        1 -> Constants.SUNDAY
        else -> Constants.MONDAY
    }

    val currentJapanHour = jpCalendar.get(Calendar.HOUR_OF_DAY)

    val currentJapanWeekday = when(jpCalendar.get(Calendar.DAY_OF_WEEK)) {
        2 -> Constants.MONDAY
        3 -> Constants.TUESDAY
        4 -> Constants.WEDNESDAY
        5 -> Constants.THURSDAY
        6 -> Constants.FRIDAY
        7 -> Constants.SATURDAY
        1 -> Constants.SUNDAY
        else -> Constants.MONDAY
    }

    val seasonIcon = when (currentSeason) {
        Season.WINTER -> R.drawable.ic_winter_24
        Season.SPRING -> R.drawable.ic_spring_24
        Season.SUMMER -> R.drawable.ic_summer_24
        Season.FALL -> R.drawable.ic_fall_24
    }
}