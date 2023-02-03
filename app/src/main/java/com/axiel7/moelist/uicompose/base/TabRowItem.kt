package com.axiel7.moelist.uicompose.base

import androidx.compose.ui.graphics.vector.ImageVector

data class TabRowItem<T>(
    val value: T,
    val title: String,
    val icon: ImageVector? = null,
)
