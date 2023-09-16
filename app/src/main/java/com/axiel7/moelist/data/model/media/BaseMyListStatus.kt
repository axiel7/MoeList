package com.axiel7.moelist.data.model.media

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.BaseResponse
import com.axiel7.moelist.utils.NumExtensions.isGreaterThanZero

abstract class BaseMyListStatus : BaseResponse() {
    abstract val status: ListStatus
    abstract val score: Int
    abstract val updatedAt: String?
    abstract val startDate: String?
    abstract val finishDate: String?
    abstract val progress: Int?
    abstract val repeatCount: Int?
    abstract val repeatValue: Int?
    abstract val isRepeating: Boolean
    abstract val priority: Int
    abstract val tags: List<String>?
    abstract val comments: String?

    override val error: String? = null
    override val message: String? = null

    fun hasRepeated() = isRepeating || repeatCount.isGreaterThanZero()

    fun hasNotes() = !comments.isNullOrBlank() || !tags.isNullOrEmpty()
}

@Composable
fun Int.scoreText() = when (this) {
    0 -> "─"
    1 -> stringResource(R.string.score_apalling)
    2 -> stringResource(R.string.score_horrible)
    3 -> stringResource(R.string.score_very_bad)
    4 -> stringResource(R.string.score_bad)
    5 -> stringResource(R.string.score_average)
    6 -> stringResource(R.string.score_fine)
    7 -> stringResource(R.string.score_good)
    8 -> stringResource(R.string.score_very_good)
    9 -> stringResource(R.string.score_great)
    10 -> stringResource(R.string.score_masterpiece)
    else -> "─"
}

@Composable
fun Int.priorityLocalized() = when (this) {
    0 -> stringResource(R.string.low_value)
    1 -> stringResource(R.string.medium_value)
    2 -> stringResource(R.string.high_value)
    else -> stringResource(R.string.unknown)
}

@Composable
fun Int.repeatValueLocalized() = when (this) {
    0 -> "─"
    1 -> stringResource(R.string.very_low_value)
    2 -> stringResource(R.string.low_value)
    3 -> stringResource(R.string.medium_value)
    4 -> stringResource(R.string.high_value)
    5 -> stringResource(R.string.very_high_value)
    else -> stringResource(R.string.unknown)
}
