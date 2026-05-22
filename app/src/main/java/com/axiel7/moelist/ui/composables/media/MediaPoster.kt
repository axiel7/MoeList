package com.axiel7.moelist.ui.composables.media

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.axiel7.moelist.ui.theme.MoeListTheme

const val MEDIA_POSTER_RATIO = 2f / 3f

const val MEDIA_POSTER_COMPACT_WIDTH = 100
const val MEDIA_POSTER_COMPACT_HEIGHT = 150

const val MEDIA_POSTER_SMALL_WIDTH = 100
const val MEDIA_POSTER_SMALL_HEIGHT = 150

const val MEDIA_POSTER_MEDIUM_WIDTH = 110
const val MEDIA_POSTER_MEDIUM_HEIGHT = 165

const val MEDIA_POSTER_BIG_WIDTH = 150
const val MEDIA_POSTER_BIG_HEIGHT = 225

@Composable
fun MediaPoster(
    url: String?,
    showShadow: Boolean = true,
    contentScale: ContentScale = ContentScale.Crop,
    modifier: Modifier
) {
    AsyncImage(
        model = url,
        contentDescription = "poster",
        placeholder = ColorPainter(MaterialTheme.colorScheme.surfaceContainerHighest),
        error = ColorPainter(MaterialTheme.colorScheme.surfaceContainerHighest),
        fallback = ColorPainter(MaterialTheme.colorScheme.surfaceContainerHighest),
        contentScale = contentScale,
        modifier = modifier
            .clip(RoundedCornerShape(20.dp)) // Updated for M3/ColorOS consistency
    )
}

@Preview(showBackground = true)
@Composable
fun MediaPosterPreview() {
    MoeListTheme {
        MediaPoster(
            url = "https://cdn.myanimelist.net/images/anime/1170/124312l.jpg",
            modifier = Modifier
                .size(width = MEDIA_POSTER_SMALL_WIDTH.dp, height = MEDIA_POSTER_SMALL_HEIGHT.dp)
        )
    }
}
