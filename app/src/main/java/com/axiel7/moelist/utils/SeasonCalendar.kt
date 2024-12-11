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


    val currentSeason = when (month)
    {
         0, 1,  -> Season.WINTER
         2, 3, 4 -> Season.SPRING
         5, 6, 7 -> Season.SUMMER
         8, 9, 10,11 -> Season.FALL  /*according to mal,  "12 december 2024" should show -> Fall in SeasonChart */
        else -> Season.SPRING
    }

    /*  Assert
       --- MAL APP --button values  --- expecting the same from moelist.
       ---- tested -- on 12.12.2024

       previous: summer 2024
       current : fall 2024
       next: winter 2025
   */

    private fun nextSeason (seasonx : Season): Season
    {
        var ret = when (seasonx)
        {
            Season.SPRING -> Season.SUMMER
            Season.SUMMER -> Season.FALL
            Season.FALL -> Season.WINTER
            Season.WINTER -> Season.SPRING
        }

        return ret;
    }
    private fun prevSeason (seasonx : Season ):Season
    {
        var ret = when (seasonx)
        {
            Season.SPRING -> Season.WINTER
            Season.SUMMER -> Season.SPRING
            Season.FALL -> Season.SUMMER
            Season.WINTER -> Season.FALL
        }
        return ret;
    }


//    val currentStartSeason = StartSeason(
//        // if december, the season is next year winter
//        year = if (month == 11) currentYear + 1 else currentYear,
//        season = currentSeason
//    )

    fun currentStartSeason() :StartSeason
    {
        var curYear = currentYear;
        var curSeason = currentSeason;

        val ss = StartSeason(
            year = curYear,
            season = curSeason
        )
        return ss;
    }

    fun nextStartSeasonv2() :StartSeason
    {
        var curSS = currentStartSeason();

        val year = if (curSS.season == Season.FALL) currentYear + 1 else currentYear;
        val season = nextSeason(curSS.season);

        val ss = StartSeason(
            year = year,
            season = season
        )
        return ss;
    }
    fun prevStartSeasonv2() :StartSeason
    {
        var curSS = currentStartSeason();

        val year = if (curSS.season == Season.WINTER) currentYear -1 else currentYear;
        val season = prevSeason(curSS.season);

        val ss = StartSeason(
            year = year,
            season = season
        )
        return ss;
    }


//    /**
//     * wrong
//     */
//    val nextStartSeason = StartSeason(
//        year = if (nextSeason == Season.WINTER) currentYear + 1 else currentYear,
//        season = nextSeason
//    )
//    /**
//     * wrong
//     */
//    val prevStartSeason = StartSeason(
//        year = if (prevSeason == Season.FALL) currentYear - 1 else currentYear,
//        season = prevSeason
//    )






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