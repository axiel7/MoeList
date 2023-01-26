package com.axiel7.moelist.uicompose.composables

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.axiel7.moelist.uicompose.theme.MoeListTheme
import com.axiel7.moelist.uicompose.theme.placeholder_color

const val MEDIA_POSTER_SMALL_HEIGHT = 140
const val MEDIA_POSTER_SMALL_WIDTH = 100

@Composable
fun MediaPosterSmall(url: String?) {
    AsyncImage(
        model = url,
        contentDescription = "poster",
        placeholder = ColorPainter(placeholder_color),
        contentScale = ContentScale.FillBounds,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .size(width = MEDIA_POSTER_SMALL_WIDTH.dp, height = MEDIA_POSTER_SMALL_HEIGHT.dp)
    )
}

@Preview
@Composable
fun MediaPosterSmallPreview() {
    MoeListTheme {
        MediaPosterSmall("https://cdn.myanimelist.net/images/anime/1170/124312l.jpg")
    }
}