package com.axiel7.moelist.uicompose.composables

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.fade
import com.google.accompanist.placeholder.material.placeholder

fun Modifier.defaultPlaceholder(
    visible: Boolean
) = composed { placeholder(
    visible = visible,
    color = MaterialTheme.colorScheme.outline,
    highlight = PlaceholderHighlight.fade()
) }