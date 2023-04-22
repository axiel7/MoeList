package com.axiel7.moelist.utils

import java.time.DateTimeException
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
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

    fun String.toISOformat(inputFormat: DateTimeFormatter) = LocalDate.parse(this, inputFormat).toString()

    fun LocalDate.toEpochMillis() = this.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()
}