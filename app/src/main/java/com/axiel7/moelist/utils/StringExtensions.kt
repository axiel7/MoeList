package com.axiel7.moelist.utils

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

    /**
     * Format the opening/ending text from MAL to use it on YouTube search
     */
    fun String.buildQueryFromThemeText() = this
        .replace(" ", "+")
        .replace("\"", "")
        .replaceFirst(Regex("#?\\w+:"), "") // theme number
        .replace(Regex("\\(ep.*\\)"), "") // episodes
        .trim()
}