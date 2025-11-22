package com.axiel7.moelist.ui.base

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Stable

@Stable
data class TabRowItem<T>(
    val value: T,
    @param:StringRes val title: Int?,
    @param:DrawableRes val icon: Int? = null,
)
