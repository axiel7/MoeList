package com.axiel7.moelist.data.model.media

abstract class BaseUserMediaList<T> {
    abstract val node: T
    abstract var status: String?
}

fun calculateProgressBarValue(
    currentProgress: Int?,
    totalProgress: Int?
): Float {
    val total = totalProgress ?: 0
    return if (total == 0) 1f
    else ((currentProgress ?: 0) / total).toFloat()
}