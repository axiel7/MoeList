package com.axiel7.moelist.uicompose.base

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.axiel7.moelist.R

enum class ListStyle(val value: String) {
    STANDARD("standard"),
    COMPACT("compact"),
    MINIMAL("minimal"),
    GRID("grid");

    override fun toString(): String {
        return value
    }

    companion object {
        fun forValue(value: String) = entries.firstOrNull { it.value == value }
    }
}

val ListStyle.stringRes
    get() = when (this) {
        ListStyle.STANDARD -> R.string.list_mode_standard
        ListStyle.COMPACT -> R.string.list_mode_compact
        ListStyle.MINIMAL -> R.string.list_mode_minimal
        ListStyle.GRID -> R.string.list_mode_grid
    }

@Composable
fun ListStyle.localized() = stringResource(stringRes)