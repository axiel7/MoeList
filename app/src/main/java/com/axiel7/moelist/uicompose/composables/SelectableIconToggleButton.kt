package com.axiel7.moelist.uicompose.composables

import androidx.annotation.DrawableRes
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.PlainTooltipBox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> SelectableIconToggleButton(
    @DrawableRes icon: Int,
    tooltipText: String,
    value: T,
    selectedValue: T,
    onClick: (Boolean) -> Unit
) {
    PlainTooltipBox(
        tooltip = { Text(tooltipText) },
    ) {
        FilledIconToggleButton(
            checked = value == selectedValue,
            onCheckedChange = onClick,
            modifier = Modifier.tooltipAnchor()
        ) {
            Icon(painter = painterResource(icon), contentDescription = tooltipText)
        }
    }
}