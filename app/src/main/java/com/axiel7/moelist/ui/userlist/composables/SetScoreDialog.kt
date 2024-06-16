package com.axiel7.moelist.ui.userlist.composables

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.media.scoreText
import com.axiel7.moelist.ui.composables.score.ScoreSlider

@Composable
fun SetScoreDialog(
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit,
) {
    var score by remember { mutableIntStateOf(0) }
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onConfirm(score) }) {
                Text(text = stringResource(R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.cancel))
            }
        },
        title = {
            Text(text = stringResource(id = R.string.score_value, score.scoreText()))
        },
        text = {
            ScoreSlider(
                score = score,
                onValueChange = { score = it }
            )
        }
    )
}