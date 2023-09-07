package com.axiel7.moelist.uicompose.composables

import androidx.annotation.DrawableRes
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.painterResource
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> SelectableIconToggleButton(
    @DrawableRes icon: Int,
    tooltipText: String,
    value: T,
    selectedValue: T,
    onClick: (Boolean) -> Unit
) {
    val tooltipState = rememberTooltipState()
    val scope = rememberCoroutineScope()

    TooltipBox(
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
        tooltip = {
            PlainTooltip {
                Text(tooltipText)
            }
        },
        focusable = false,
        state = tooltipState
    ) {
        FilledIconToggleButton(
            checked = value == selectedValue,
            onCheckedChange = {
                scope.launch { tooltipState.show() }
                onClick(it)
            }
        ) {
            Icon(painter = painterResource(icon), contentDescription = tooltipText)
        }
    }
}