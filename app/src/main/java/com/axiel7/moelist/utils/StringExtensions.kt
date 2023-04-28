package com.axiel7.moelist.utils

import android.net.Uri
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object StringExtensions {
    /**
     * Returns a string representation of the object.
     * Can be called with a null receiver, in which case it returns `null`.
     */
    fun Any?.toStringOrNull() = this.toString().let { if (it == "null") null else it }

    /**
     * Returns a string representation of the object.
     * Can be called with a null receiver, in which case it returns an empty String.
     */
    fun Any?.toStringOrEmpty() = this.toString().let { if (it == "null") "" else it }

    fun Array<String>.toNavArgument(): String = Uri.encode(Json.encodeToString(this))
}