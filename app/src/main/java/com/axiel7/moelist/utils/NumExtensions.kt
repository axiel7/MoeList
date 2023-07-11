package com.axiel7.moelist.utils

import com.axiel7.moelist.utils.StringExtensions.toStringOrNull
import java.text.NumberFormat

object NumExtensions {

    val numberFormat: NumberFormat = NumberFormat.getInstance()

    /**
     * @return if true 1 else 0
     */
    fun Boolean?.toInt(): Int = if (this == true) 1 else 0

    /**
     * Returns a string representation of the Integer. If the Integer is `<= 0` returns `null`.
     * Can be called with a null receiver, in which case it returns `null`.
     */
    fun Int?.toStringPositiveValueOrNull() = if (this == 0) null else this.toStringOrNull()

    fun Int?.toStringOrZero() = this?.toString() ?: "0"

    fun Int?.toStringOrUnknown() = this?.toString() ?: Constants.UNKNOWN_CHAR

    /**
     * Returns a string representation of the Integer.
     * If the Integer is `<= 0` or `null` returns `"─"`.
     */
    fun Int?.toStringPositiveValueOrUnknown() =
        if (this == 0) Constants.UNKNOWN_CHAR else this.toStringOrUnknown()

    fun Float?.toStringOrZero() = this?.toString() ?: "0.0"

    fun Float?.toStringOrUnknown() = this?.toString() ?: Constants.UNKNOWN_CHAR

    /**
     * Returns a string representation of the Float.
     * If the Float is `<= 0` or `null` returns `"─"`.
     */
    fun Float?.toStringPositiveValueOrUnknown() =
        if (this == 0f) Constants.UNKNOWN_CHAR else this.toStringOrUnknown()
}