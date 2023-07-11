package com.axiel7.moelist.data.model.media

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.BaseResponse
import kotlinx.serialization.SerialName

abstract class BaseMyListStatus : BaseResponse() {
    @SerialName("status")
    abstract val status: ListStatus

    @SerialName("score")
    abstract val score: Int

    @SerialName("updated_at")
    abstract val updatedAt: String?

    @SerialName("start_date")
    abstract val startDate: String?

    @SerialName("end_date")
    abstract val endDate: String?

    abstract val progress: Int?
    abstract val repeatCount: Int?
    abstract val isRepeating: Boolean

    override val error: String? = null
    override val message: String? = null
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