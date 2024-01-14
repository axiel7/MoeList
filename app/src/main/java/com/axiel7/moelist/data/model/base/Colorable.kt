package com.axiel7.moelist.data.model.base

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

interface Colorable {
    @Composable
    fun primaryColor(): Color
    @Composable
    fun onPrimaryColor(): Color
}