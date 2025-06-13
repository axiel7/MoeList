package com.axiel7.moelist.ui.editmedia.composables

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.axiel7.moelist.R
import com.axiel7.moelist.ui.theme.MoeListTheme
import com.axiel7.moelist.utils.DateUtils.toLocalized
import java.time.LocalDate

@Composable
fun EditMediaDateField(
    date: LocalDate?,
    label: String,
    @DrawableRes icon: Int? = null,
    modifier: Modifier = Modifier,
    removeDate: () -> Unit,
    onClick: () -> Unit,
) {
    val dateLocalized = date?.toLocalized()
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .clickable(onClick = onClick),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = label,
                    modifier = Modifier.padding(start = 16.dp),
                )
            } else {
                Spacer(
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .size(24.dp)
                )
            }
            Column(
                modifier = Modifier.padding(
                    horizontal = 16.dp,
                    vertical = if (dateLocalized != null) 8.dp else 16.dp
                )
            ) {
                Text(
                    text = label,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (dateLocalized != null) {
                    Text(
                        text = dateLocalized,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 13.sp
                    )
                }
            }//: Column
        }//: Row
        if (date != null) {
            IconButton(
                onClick = removeDate,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.round_close_24),
                    contentDescription = stringResource(R.string.delete)
                )
            }
        }
    }//: Row
}

@Preview
@Composable
fun EditMediaDateFieldPreview() {
    MoeListTheme {
        Surface {
            EditMediaDateField(
                date = LocalDate.now(),
                label = "Start date",
                icon = R.drawable.round_calendar_today_24,
                removeDate = {},
                onClick = {}
            )
        }
    }
}