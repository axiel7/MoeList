package com.axiel7.moelist.ui.userlist.composables

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.axiel7.moelist.R
import com.axiel7.moelist.ui.userlist.UserMediaListEvent
import com.axiel7.moelist.ui.userlist.UserMediaListUiState

@Composable
fun SetAsCompletedDialog(
    uiState: UserMediaListUiState,
    event: UserMediaListEvent?
) {
    AlertDialog(
        onDismissRequest = { event?.toggleSetAsCompleteDialog(false) },
        confirmButton = {
            TextButton(
                onClick = {
                    uiState.lastItemUpdatedId?.let { event?.setAsCompleted(it) }
                    event?.toggleSetAsCompleteDialog(false)
                }
            ) {
                Text(text = stringResource(R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = { event?.toggleSetAsCompleteDialog(false) }) {
                Text(text = stringResource(R.string.cancel))
            }
        },
        title = {
            Text(text = stringResource(R.string.set_as_completed))
        }
    )
}