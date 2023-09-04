package com.axiel7.moelist.uicompose.more.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.axiel7.moelist.R
import com.axiel7.moelist.utils.Constants
import com.axiel7.moelist.utils.ContextExtensions.openAction

@Composable
fun FeedbackDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.cancel))
            }
        },
        text = {
            Column {
                MoreItem(
                    title = stringResource(R.string.github),
                    icon = R.drawable.ic_github,
                    onClick = {
                        context.openAction(Constants.GITHUB_ISSUES_URL)
                    }
                )
                MoreItem(
                    title = stringResource(R.string.discord),
                    icon = R.drawable.ic_discord,
                    onClick = {
                        context.openAction(Constants.DISCORD_SERVER_URL)
                    }
                )
            }
        }
    )
}