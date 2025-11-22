package com.axiel7.moelist.ui.composables

import androidx.annotation.DrawableRes
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconToggleButtonShapes
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.painterResource
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
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
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
            positioning = TooltipAnchorPosition.Above
        ),
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
            },
            shapes = IconButtonDefaults.toggleableShapes(),
        ) {
            Icon(painter = painterResource(icon), contentDescription = tooltipText)
        }
    }
}