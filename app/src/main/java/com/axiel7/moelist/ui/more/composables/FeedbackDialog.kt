package com.axiel7.moelist.ui.more.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.axiel7.moelist.R
import com.axiel7.moelist.utils.ContextExtensions.openAction
import com.axiel7.moelist.utils.DISCORD_SERVER_URL
import com.axiel7.moelist.utils.GITHUB_ISSUES_URL

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
                        context.openAction(GITHUB_ISSUES_URL)
                    }
                )
                MoreItem(
                    title = stringResource(R.string.discord),
                    icon = R.drawable.ic_discord,
                    onClick = {
                        context.openAction(DISCORD_SERVER_URL)
                    }
                )
            }
        }
    )
}