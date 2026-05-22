package com.axiel7.moelist.ui.composables.media

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.axiel7.moelist.ui.theme.MoeListTheme

const val MEDIA_ITEM_VERTICAL_HEIGHT = 220

@Composable
fun MediaItemVertical(
    title: String,
    imageUrl: String?,
    modifier: Modifier = Modifier,
    posterOverlay: @Composable (BoxScope.() -> Unit)? = null,
    badgeContent: @Composable (RowScope.() -> Unit)? = null,
    subtitle: @Composable (() -> Unit)? = null,
    subtitle2: @Composable (() -> Unit)? = null,
    minLines: Int = 1,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.95f else 1f, label = "scale")

    Column(
        modifier = modifier
            .width(MEDIA_POSTER_SMALL_WIDTH.dp)
            .sizeIn(minHeight = MEDIA_ITEM_VERTICAL_HEIGHT.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(24.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        horizontalAlignment = Alignment.Start
    ) {
        Box(
            modifier = Modifier.padding(bottom = 8.dp),
            contentAlignment = Alignment.BottomStart
        ) {
            MediaPoster(
                url = imageUrl,
                showShadow = false,
                modifier = Modifier
                    .size(
                        width = MEDIA_POSTER_SMALL_WIDTH.dp,
                        height = MEDIA_POSTER_SMALL_HEIGHT.dp
                    )
                    .clip(RoundedCornerShape(24.dp))
            )

            posterOverlay?.invoke(this)

            if (badgeContent != null) {
                Surface(
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.TopEnd),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.9f),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        content = badgeContent
                    )
                }
            }
        }

        Text(
            text = title,
            modifier = Modifier
                .width(MEDIA_POSTER_SMALL_WIDTH.dp)
                .padding(horizontal = 4.dp),
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            overflow = TextOverflow.Ellipsis,
            maxLines = 2,
            minLines = minLines
        )

        Column(
            modifier = Modifier.padding(top = 2.dp, start = 4.dp, end = 4.dp),
        ) {
            subtitle?.let { it() }
            subtitle2?.let { it() }
        }
    }
}

fun Modifier.shimmerEffect(): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )

    val shimmerColors = listOf(
        MaterialTheme.colorScheme.surfaceContainerHighest,
        MaterialTheme.colorScheme.surfaceContainerLow,
        MaterialTheme.colorScheme.surfaceContainerHighest,
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = translateAnim, y = translateAnim)
    )
    background(brush)
}

@Composable
fun MediaItemVerticalPlaceholder(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(MEDIA_POSTER_SMALL_WIDTH.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .size(
                    width = MEDIA_POSTER_SMALL_WIDTH.dp,
                    height = MEDIA_POSTER_SMALL_HEIGHT.dp
                )
                .clip(RoundedCornerShape(24.dp))
                .shimmerEffect()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .padding(horizontal = 4.dp)
                .size(width = 80.dp, height = 12.dp)
                .clip(RoundedCornerShape(4.dp))
                .shimmerEffect()
        )
    }
}

@Composable
fun MediaItemDetailedPlaceholder(
    modifier: Modifier = Modifier
) {
    MediaItemVerticalPlaceholder(modifier = modifier)
}

@Preview
@Composable
fun MediaItemVerticalPreview() {
    MoeListTheme {
        Surface {
            Row(modifier = Modifier.padding(16.dp)) {
                MediaItemVertical(
                    imageUrl = null,
                    title = "Modern Anime Title",
                    onClick = {}
                )
            }
        }
    }
}
