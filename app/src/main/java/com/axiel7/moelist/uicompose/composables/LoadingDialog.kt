package com.axiel7.moelist.uicompose.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.axiel7.moelist.R
import com.axiel7.moelist.uicompose.theme.MoeListTheme

@Composable
fun LoadingDialog(
    text: String = stringResource(R.string.loading),
    onDismiss: () -> Unit = {},
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        title = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(progress = 0.7f)
                Text(text = text)
            }
        }
    )
}

@Preview
@Composable
fun LoadingDialogPreview() {
    MoeListTheme {
        LoadingDialog()
    }
}