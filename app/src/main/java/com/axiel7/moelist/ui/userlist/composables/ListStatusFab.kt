package com.axiel7.moelist.ui.userlist.composables

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.material3.ToggleFloatingActionButtonDefaults.animateIcon
import androidx.compose.material3.animateFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.media.ListStatus
import com.axiel7.moelist.data.model.media.ListStatus.Companion.listStatusValues
import com.axiel7.moelist.data.model.media.MediaType

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ListStatusFab(
    mediaType: MediaType,
    status: ListStatus,
    onStatusChanged: (ListStatus) -> Unit,
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    FloatingActionButtonMenu(
        modifier = modifier,
        expanded = expanded,
        button = {
            ToggleFloatingActionButton(
                modifier =
                    Modifier
                        .animateFloatingActionButton(
                            visible = isVisible || expanded,
                            alignment = Alignment.BottomEnd,
                        ),
                checked = expanded,
                onCheckedChange = { expanded = !expanded },
            ) {
                val icon by remember {
                    derivedStateOf {
                        if (checkedProgress > 0.5f) R.drawable.round_close_24 else status.icon
                    }
                }
                Icon(
                    painter = painterResource(icon),
                    contentDescription = null,
                    modifier = Modifier
                        .animateIcon({ checkedProgress }),
                )
            }
        },
    ) {
        listStatusValues(mediaType).forEach { item ->
            FloatingActionButtonMenuItem(
                onClick = {
                    onStatusChanged(item)
                    expanded = false
                },
                icon = {
                    Icon(
                        painter = painterResource(item.icon),
                        contentDescription = null
                    )
                },
                text = { Text(text = item.localized()) },
            )
        }
    }
}