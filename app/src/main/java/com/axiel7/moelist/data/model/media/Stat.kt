package com.axiel7.moelist.data.model.media

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color

data class Stat(
    @StringRes val title: Int,
    val value: Double,
    val color: Color,
)
