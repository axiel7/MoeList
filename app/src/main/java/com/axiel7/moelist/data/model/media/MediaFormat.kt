package com.axiel7.moelist.data.model.media

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.base.Localizable
import kotlinx.serialization.SerialName

enum class MediaFormat : Localizable {
    @SerialName("tv")
    TV,

    @SerialName("ova")
    OVA,

    @SerialName("ona")
    ONA,

    @SerialName("movie")
    MOVIE,

    @SerialName("special")
    SPECIAL,

    @SerialName("music")
    MUSIC,

    @SerialName("manga")
    MANGA,

    @SerialName("one_shot")
    ONE_SHOT,

    @SerialName("manhwa")
    MANHWA,

    @SerialName("manhua")
    MANHUA,

    @SerialName("novel")
    NOVEL,

    @SerialName("light_novel")
    LIGHT_NOVEL,

    @SerialName("doujinshi")
    DOUJINSHI,

    @SerialName("unknown")
    UNKNOWN;

    @Composable
    override fun localized() = when (this) {
        TV -> stringResource(R.string.tv)
        OVA -> stringResource(R.string.ova)
        ONA -> stringResource(R.string.ona)
        MOVIE -> stringResource(R.string.movie)
        SPECIAL -> stringResource(R.string.special)
        MUSIC -> stringResource(R.string.music)
        UNKNOWN -> stringResource(R.string.unknown)
        MANGA -> stringResource(R.string.manga)
        ONE_SHOT -> stringResource(R.string.one_shot)
        MANHWA -> stringResource(R.string.manhwa)
        MANHUA -> stringResource(R.string.manhua)
        NOVEL -> stringResource(R.string.novel)
        LIGHT_NOVEL -> stringResource(R.string.light_novel)
        DOUJINSHI -> stringResource(R.string.doujinshi)
    }
}