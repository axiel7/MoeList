package com.axiel7.moelist.ui.composables.media

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.axiel7.moelist.R
import com.axiel7.moelist.ui.composables.TextIconHorizontal
import com.axiel7.moelist.ui.composables.defaultPlaceholder
import com.axiel7.moelist.ui.theme.MoeListTheme

@Composable
fun MediaItemDetailed(
    title: String,
    imageUrl: String?,
    badgeContent: @Composable (RowScope.() -> Unit)? = null,
    subtitle1: @Composable RowScope.() -> Unit,
    subtitle2: @Composable RowScope.() -> Unit,
    subtitle3: @Composable RowScope.() -> Unit,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.height(MEDIA_POSTER_SMALL_HEIGHT.dp)
        ) {
            Box(
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
                )

                if (badgeContent != null) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(topEnd = 8.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        content = badgeContent
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 17.sp,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    content = subtitle1
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    content = subtitle2
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    content = subtitle3
                )
            }
        }
    }
}

@Composable
fun MediaItemDetailedPlaceholder() {
    Row(
        modifier = Modifier
            .padding(8.dp)
            .height(MEDIA_POSTER_SMALL_HEIGHT.dp)
    ) {
        Box(
            modifier = Modifier
                .size(
                    width = MEDIA_POSTER_SMALL_WIDTH.dp,
                    height = MEDIA_POSTER_SMALL_HEIGHT.dp
                )
                .clip(RoundedCornerShape(8.dp))
                .defaultPlaceholder(visible = true)
        )

        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Text(
                text = "This is a placeholder text",
                modifier = Modifier.defaultPlaceholder(visible = true),
                fontSize = 17.sp,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )

            Text(
                text = "This is a placeholder",
                modifier = Modifier.defaultPlaceholder(visible = true)
            )

            Text(
                text = "Placeholder",
                modifier = Modifier.defaultPlaceholder(visible = true)
            )

            Text(
                text = "Placeholder",
                modifier = Modifier.defaultPlaceholder(visible = true)
            )
        }
    }
}

@Preview
@Composable
fun MediaItemDetailedPreview() {
    MoeListTheme {
        MediaItemDetailed(
            title = "Boku no Hero Academia and a very very large title to preview",
            imageUrl = "https://cdn.myanimelist.net/images/anime/1170/124312l.jpg",
            badgeContent = {
                Text(text = "#12")
            },
            subtitle1 = {
                Text(text = "TV (13 Episodes)", color = MaterialTheme.colorScheme.onSurfaceVariant)
            },
            subtitle2 = { Text(text = "2017", color = MaterialTheme.colorScheme.onSurfaceVariant) },
            subtitle3 = {
                TextIconHorizontal(
                    text = "8.61",
                    icon = R.drawable.ic_round_details_star_24,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            onClick = { }
        )
    }
}