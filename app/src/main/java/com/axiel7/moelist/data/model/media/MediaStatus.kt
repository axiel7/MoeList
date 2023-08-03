package com.axiel7.moelist.data.model.media

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.base.Localizable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class MediaStatus : Localizable {
    @SerialName("currently_airing")
    AIRING,

    @SerialName("finished_airing")
    FINISHED_AIRING,

    @SerialName("not_yet_aired")
    NOT_AIRED,

    @SerialName("currently_publishing")
    PUBLISHING,

    @SerialName("finished")
    FINISHED,

    @SerialName("on_hiatus")
    HIATUS,

    @SerialName("discontinued")
    DISCONTINUED;

    @Composable
    override fun localized() = when (this) {
        AIRING -> stringResource(R.string.airing)
        FINISHED_AIRING -> stringResource(R.string.finished)
        NOT_AIRED -> stringResource(R.string.not_yet_aired)
        PUBLISHING -> stringResource(R.string.publishing)
        FINISHED -> stringResource(R.string.finished)
        HIATUS -> stringResource(R.string.on_hiatus)
        DISCONTINUED -> stringResource(R.string.discontinued)
    }
}