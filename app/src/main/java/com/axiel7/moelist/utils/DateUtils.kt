package com.axiel7.moelist.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.axiel7.moelist.R
import java.time.DateTimeException
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.FormatStyle
import java.time.temporal.TemporalAdjusters
import java.util.TimeZone

object DateUtils {

    fun unixtimeToStringDate(
        time: Long?,
        formatter: DateTimeFormatter = DateTimeFormatter.ISO_DATE
    ): String? {
        if (time == null) return null
        return try {
            LocalDateTime.ofInstant(Instant.ofEpochMilli(time), TimeZone.getDefault().toZoneId()).format(formatter)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * @return the date in LocalDate, null if fails
     */
    fun getLocalDateFromDateString(
        date: String?,
        formatter: DateTimeFormatter = DateTimeFormatter.ISO_DATE
    ): LocalDate? {
        if (date == null) return null
        return try {
            LocalDate.parse(date, formatter)
        } catch (e: DateTimeException) {
            null
        }
    }

    /**
     * @return the date in LocalDate, null if fails
     */
    fun getLocalDateFromMillis(millis: Long): LocalDate? {
        return try {
            Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * @return the date in unixtime, null if fails
     */
    fun getTimeInMillisFromDateString(
        date: String?,
        formatter: DateTimeFormatter = DateTimeFormatter.ISO_DATE
    ): Long? {
        if (date == null) return null
        return try {
            getLocalDateFromDateString(date, formatter)?.atStartOfDay()?.toInstant(ZoneOffset.UTC)?.toEpochMilli()
        } catch (e: DateTimeException) {
            null
        }
    }

    /**
     * Formats a date in a string with default format 'Jan 1, 1977'
     */
    fun formatLocalDateToString(
        date: LocalDate?,
        formatter: DateTimeFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
    ): String? {
        if (date == null) return null
        return try {
            date.format(formatter)
        } catch (e: Exception) {
            null
        }
    }

    fun LocalDate?.toLocalized(
        style: FormatStyle = FormatStyle.MEDIUM
    ): String = try {
        this?.format(DateTimeFormatter.ofLocalizedDate(style)) ?: ""
    } catch (e: DateTimeException) {
        ""
    }

    fun String.parseDate(
        inputFormat: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    ): LocalDate? = try {
        LocalDate.parse(this, inputFormat)
    } catch (e: DateTimeParseException) {
        null
    }

    fun String.parseDateAndLocalize(
        inputFormat: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE,
        style: FormatStyle = FormatStyle.MEDIUM
    ): String? = try {
        when (this.count { it == '-' }) {
            0 -> this //only the year (2007)
            1 -> { //year and month (2007-11)
                this
                //TODO: replace with the following code when the bug
                // https://bugs.openjdk.org/browse/JDK-8168532 is fixed
                //YearMonth.parse(this)
                //    ?.format(DateTimeFormatter.ofLocalizedDate(style))
            }
            else -> {
                LocalDate.parse(this, inputFormat)
                    ?.format(DateTimeFormatter.ofLocalizedDate(style))
            }
        }
    } catch (e: DateTimeParseException) {
        null
    } catch (e: DateTimeException) {
        null
    }

    fun String.toIsoFormat(inputFormat: DateTimeFormatter) = LocalDate.parse(this, inputFormat).toString()

    fun LocalDate.toEpochMillis() = this.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()

    fun LocalDate.getNextDayOfWeek(dayOfWeek: DayOfWeek) = with(TemporalAdjusters.nextOrSame(dayOfWeek))

    /**
     * Converts seconds to years, months, weeks, days, hours or minutes.
     * Depending if there is enough time.
     * Eg. If days greater than 1 and less than 6, returns "x days"
     */
    @Composable
    fun Long.secondsToLegibleText(): String {
        val days = this / 86400
        if (days > 6) {
            val weeks = this / 604800
            return if (weeks > 4) {
                val months = this / 2629746
                if (months > 12) {
                    val years = this / 31556952
                    stringResource(R.string.num_years).format(years)
                } else stringResource(R.string.num_months).format(months)
            } else stringResource(R.string.num_weeks).format(weeks)
        }
        else if (days >= 1) return "%s days".format(days)
        else {
            val hours = this / 3600
            return if (hours >= 1) "$hours ${stringResource(R.string.hour_abbreviation)}"
            else {
                val minutes = (this % 3600) / 60
                "$minutes ${stringResource(R.string.minutes_abbreviation)}"
            }
        }
    }
}