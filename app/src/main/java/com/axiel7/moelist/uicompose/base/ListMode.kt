package com.axiel7.moelist.uicompose.base

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.axiel7.moelist.R

enum class ListMode(val value: String) {
    STANDARD("standard"),
    COMPACT("compact"),
    MINIMAL("minimal"),
    GRID("grid");

    override fun toString(): String {
        return value
    }

    companion object {
        fun forValue(value: String) = ListMode.values().firstOrNull { it.value == value }
    }
}

val ListMode.stringRes get() = when (this) {
    ListMode.STANDARD -> R.string.list_mode_standard
    ListMode.COMPACT -> R.string.list_mode_compact
    ListMode.MINIMAL -> R.string.list_mode_minimal
    ListMode.GRID -> R.string.list_mode_grid
}

@Composable
fun ListMode.localized() = stringResource(stringRes)