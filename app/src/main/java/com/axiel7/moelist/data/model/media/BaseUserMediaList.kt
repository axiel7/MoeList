package com.axiel7.moelist.data.model.media

abstract class BaseUserMediaList<T: BaseMediaNode> {
    abstract val node: T
    abstract val listStatus: BaseMyListStatus?
    abstract val status: String?
}

fun calculateProgressBarValue(
    currentProgress: Int?,
    totalProgress: Int?
): Float {
    val total = totalProgress ?: 0
    return if (total == 0) 0f
    else (currentProgress ?: 0).div(total.toFloat())
}