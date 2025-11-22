package com.axiel7.moelist.ui.base

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

data class TabRowItem<T>(
    val value: T,
    @param:StringRes val title: Int?,
    @param:DrawableRes val icon: Int? = null,
)
