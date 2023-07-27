package com.axiel7.moelist.uicompose.details.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.axiel7.moelist.uicompose.theme.MoeListTheme

@Composable
fun AnimeThemeItem(text: String, onClick: () -> Unit) {
    Text(
        text = text,
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.primary,
        overflow = TextOverflow.Ellipsis,
        maxLines = 1
    )
}

@Preview(showBackground = true)
@Composable
fun AnimeThemeItemPreview() {
    MoeListTheme {
        AnimeThemeItem(text = "Opening 1: A large title to test the preview", onClick = {})
    }
}