package com.axiel7.moelist.uicompose.composables

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import io.github.fornewid.placeholder.foundation.PlaceholderHighlight
import io.github.fornewid.placeholder.material3.fade
import io.github.fornewid.placeholder.material3.placeholder

fun Modifier.defaultPlaceholder(
    visible: Boolean
) = composed {
    placeholder(
        visible = visible,
        color = MaterialTheme.colorScheme.outline,
        highlight = PlaceholderHighlight.fade()
    )
}