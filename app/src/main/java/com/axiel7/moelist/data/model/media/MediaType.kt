package com.axiel7.moelist.data.model.media

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.axiel7.moelist.R

enum class MediaType(val value: String) {
    ANIME("ANIME"),
    MANGA("MANGA")
}

@Composable
fun MediaType.localized() = when (this) {
    MediaType.ANIME -> stringResource(R.string.anime)
    MediaType.MANGA -> stringResource(R.string.manga)
}