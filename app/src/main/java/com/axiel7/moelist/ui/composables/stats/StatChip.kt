package com.axiel7.moelist.ui.composables.stats

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.axiel7.moelist.data.model.base.LocalizableAndColorable
import com.axiel7.moelist.data.model.media.Stat
import com.axiel7.moelist.utils.NumExtensions.format
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T : LocalizableAndColorable> StatChip(
    stat: Stat<T>,
    tooltipText: String? = null,
    scope: CoroutineScope = rememberCoroutineScope(),
) {
    val tooltipState = rememberTooltipState()
    TooltipBox(
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
        tooltip = {
            if (tooltipText != null) {
                PlainTooltip {
                    Text(text = tooltipText)
                }
            }
        },
        state = tooltipState
    ) {
        ElevatedAssistChip(
            onClick = { scope.launch { tooltipState.show() } },
            label = { Text(text = stat.type.localized()) },
            modifier = Modifier.padding(horizontal = 8.dp),
            leadingIcon = {
                Text(
                    text = stat.value.format() ?: stat.value.toString(),
                    color = stat.type.onPrimaryColor()
                )
            },
            colors = AssistChipDefaults.elevatedAssistChipColors(
                containerColor = stat.type.primaryColor(),
                labelColor = stat.type.onPrimaryColor(),
                leadingIconContentColor = stat.type.onPrimaryColor()
            ),
        )
    }
}