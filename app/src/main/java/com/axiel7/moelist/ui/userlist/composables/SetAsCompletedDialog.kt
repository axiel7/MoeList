package com.axiel7.moelist.ui.userlist.composables

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.axiel7.moelist.R
import com.axiel7.moelist.ui.userlist.UserMediaListViewModel

@Composable
fun SetAsCompletedDialog(
    viewModel: UserMediaListViewModel
) {
    AlertDialog(
        onDismissRequest = { viewModel.openSetAtCompletedDialog = false },
        confirmButton = {
            TextButton(
                onClick = {
                    viewModel.setAsCompleted(viewModel.lastItemUpdatedId)
                    viewModel.openSetAtCompletedDialog = false
                }
            ) {
                Text(text = stringResource(R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = { viewModel.openSetAtCompletedDialog = false }) {
                Text(text = stringResource(R.string.cancel))
            }
        },
        title = {
            Text(text = stringResource(R.string.set_as_completed))
        }
    )
}