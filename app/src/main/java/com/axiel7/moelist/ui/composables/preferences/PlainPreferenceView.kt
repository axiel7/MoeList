package com.axiel7.moelist.ui.composables.preferences

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PlainPreferenceView(
    title: String,
    titleTint: Color = MaterialTheme.colorScheme.onSurface,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    @DrawableRes icon: Int? = null,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    iconPadding: PaddingValues = PaddingValues(16.dp),
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = title,
                    modifier = Modifier.padding(iconPadding),
                    tint = if (enabled) iconTint else iconTint.copy(alpha = 0.38f)
                )
            } else {
                Spacer(
                    modifier = Modifier
                        .padding(iconPadding)
                        .size(24.dp)
                )
            }

            Column(
                modifier = if (subtitle != null)
                    Modifier.padding(16.dp)
                else Modifier.padding(horizontal = 16.dp)
            ) {
                Text(
                    text = title,
                    color = if (enabled) titleTint else titleTint.copy(alpha = 0.38f)
                )

                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                }
            }//: Column
        }//: Row
    }//: Row
}