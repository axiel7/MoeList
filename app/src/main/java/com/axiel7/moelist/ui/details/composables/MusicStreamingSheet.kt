package com.axiel7.moelist.ui.details.composables

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.base.Localizable
import com.axiel7.moelist.ui.theme.MoeListTheme
import com.axiel7.moelist.utils.ContextExtensions.openAction
import com.axiel7.moelist.utils.StringExtensions.buildQueryFromThemeText

private enum class MusicStreaming(
    val searchUrl: String,
) : Localizable {
    YouTube("https://www.youtube.com/results?search_query="),
    Spotify("https://open.spotify.com/search/"),
    AppleMusic("https://music.apple.com/search?term="),
    YouTubeMusic("https://music.youtube.com/search?q="),
    Deezer("https://www.deezer.com/search/"),
    ;

    @Composable
    override fun localized() = when (this) {
        YouTube -> "YouTube"
        Spotify -> "Spotify"
        AppleMusic -> "Apple Music"
        YouTubeMusic -> "YouTube Music"
        Deezer -> "Deezer"
    }

    val icon: Int
        @DrawableRes
        get() = when (this) {
            YouTube -> R.drawable.youtube
            Spotify -> R.drawable.spotify
            AppleMusic -> R.drawable.apple_music
            YouTubeMusic -> R.drawable.youtube_music
            Deezer -> R.drawable.deezer
        }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicStreamingSheet(
    songTitle: String,
    bottomPadding: Dp,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) }
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(bottom = 8.dp + bottomPadding)
        ) {
            MusicStreaming.entries.forEach { service ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            context.openAction(
                                service.searchUrl + songTitle.buildQueryFromThemeText()
                            )
                            onDismiss()
                        }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(service.icon),
                        contentDescription = service.localized(),
                        modifier = Modifier.size(24.dp),
                        tint = Color.Unspecified
                    )

                    Text(
                        text = service.localized(),
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun MusicStreamingSheetPreview() {
    MoeListTheme {
        MusicStreamingSheet(
            songTitle = "",
            bottomPadding = 0.dp,
            onDismiss = {}
        )
    }
}