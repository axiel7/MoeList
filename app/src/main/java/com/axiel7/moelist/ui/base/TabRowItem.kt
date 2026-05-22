package com.axiel7.moelist.ui.base

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector

data class TabRowItem<T>(
    val value: T,
    @StringRes val title: Int?,
    val icon: ImageVector? = null,
)
