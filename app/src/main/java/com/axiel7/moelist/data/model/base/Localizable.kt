package com.axiel7.moelist.data.model.base

import androidx.compose.runtime.Composable

fun interface Localizable {
    @Composable
    fun localized(): String
}