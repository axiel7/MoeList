package com.axiel7.moelist.utils

import android.net.Uri
import com.axiel7.moelist.utils.StringExtensions.toNavArgument
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object StringExtensions {
    /**
     * Returns a string representation of the object.
     * Can be called with a null receiver, in which case it returns `null`.
     */
    fun Any?.toStringOrNull() : String? {
        val result = this.toString()
        return if (result == "null") null else result
    }

    fun Array<String>.toNavArgument(): String = Uri.encode(Json.encodeToString(this))
}