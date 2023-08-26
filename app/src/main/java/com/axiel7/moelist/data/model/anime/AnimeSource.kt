package com.axiel7.moelist.data.model.anime

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.base.Localizable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class AnimeSource : Localizable {
    @SerialName("original")
    ORIGINAL,

    @SerialName("manga")
    MANGA,

    @SerialName("novel")
    NOVEL,

    @SerialName("light_novel")
    LIGHT_NOVEL,

    @SerialName("visual_novel")
    VISUAL_NOVEL,

    @SerialName("game")
    GAME,

    @SerialName("web_manga")
    WEB_MANGA,

    @SerialName("web_novel")
    WEB_NOVEL,

    @SerialName("music")
    MUSIC,

    @SerialName("mixed_media")
    MIXED_MEDIA,

    @SerialName("4_koma_manga")
    YONKOMA_MANGA;

    @Composable
    override fun localized() = when (this) {
        ORIGINAL -> stringResource(R.string.original)
        MANGA -> stringResource(R.string.manga)
        NOVEL -> stringResource(R.string.novel)
        LIGHT_NOVEL -> stringResource(R.string.light_novel)
        VISUAL_NOVEL -> stringResource(R.string.visual_novel)
        GAME -> stringResource(R.string.game)
        WEB_MANGA -> stringResource(R.string.web_manga)
        WEB_NOVEL -> stringResource(R.string.web_novel)
        MUSIC -> stringResource(R.string.music)
        MIXED_MEDIA -> stringResource(R.string.mixed_media)
        YONKOMA_MANGA -> "4-Koma ${stringResource(R.string.manga)}"
    }
}