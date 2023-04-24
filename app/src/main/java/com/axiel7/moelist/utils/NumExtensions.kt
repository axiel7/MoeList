package com.axiel7.moelist.utils

import com.axiel7.moelist.utils.StringExtensions.toStringOrNull

object NumExtensions {
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

    fun Float?.toStringOrZero() = this?.toString() ?: "0.0"
}