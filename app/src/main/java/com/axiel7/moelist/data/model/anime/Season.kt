package com.axiel7.moelist.data.model.anime

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.axiel7.moelist.R

enum class Season {
    WINTER,
    SPRING,
    SUMMER,
    FALL
}

@Composable
fun String.seasonLocalized() = when (this) {
    "winter" -> stringResource(R.string.winter)
    "spring" -> stringResource(R.string.spring)
    "summer" -> stringResource(R.string.summer)
    "fall" -> stringResource(R.string.fall)
    else -> this
}