package com.axiel7.moelist.data.model.media

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.base.Localizable

enum class MediaType : Localizable {
    ANIME, MANGA;

    @Composable
    override fun localized() = when (this) {
        ANIME -> stringResource(R.string.anime)
        MANGA -> stringResource(R.string.manga)
    }
}