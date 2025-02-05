package com.axiel7.moelist.ui.editmedia.composables

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.axiel7.moelist.R

@Composable
fun EditMediaValueRow(
    label: String,
    @DrawableRes icon: Int? = null,
    modifier: Modifier = Modifier,
    minusEnabled: Boolean = true,
    onMinusClick: () -> Unit,
    plusEnabled: Boolean = true,
    onPlusClick: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = label,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            Text(
                text = label,
                color = MaterialTheme.colorScheme.onSurface,
                overflow = TextOverflow.Ellipsis,
            )
        }

        FilledTonalIconButton(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onMinusClick()
            },
            enabled = minusEnabled,
        ) {
            Icon(
                painter = painterResource(R.drawable.round_remove_24),
                contentDescription = stringResource(R.string.minus_one)
            )
        }
        FilledTonalIconButton(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onPlusClick()
            },
            enabled = plusEnabled,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_round_add_24),
                contentDescription = stringResource(R.string.plus_one)
            )
        }
    }
}