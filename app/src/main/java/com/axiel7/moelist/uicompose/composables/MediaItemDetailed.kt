package com.axiel7.moelist.uicompose.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.axiel7.moelist.R
import com.axiel7.moelist.uicompose.theme.MoeListTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaItemDetailed(
    title: String,
    imageUrl: String?,
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
            MediaPoster(url = imageUrl, modifier = Modifier
                .size(width = MEDIA_POSTER_SMALL_WIDTH.dp, height = MEDIA_POSTER_SMALL_HEIGHT.dp)
            )

            Column(
                modifier = Modifier.fillMaxHeight().padding(horizontal = 16.dp),
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

@Preview
@Composable
fun MediaItemDetailedPreview() {
    MoeListTheme {
        MediaItemDetailed(
            title = "Boku no Hero Academia and a very very large title to preview",
            imageUrl = "https://cdn.myanimelist.net/images/anime/1170/124312l.jpg",
            subtitle1 = {
                Text(text = "TV (13 Episodes)", color = MaterialTheme.colorScheme.onSurfaceVariant)
            },
            subtitle2 = { Text(text = "2017", color = MaterialTheme.colorScheme.onSurfaceVariant) },
            subtitle3 = {
                Icon(
                    painter = painterResource(R.drawable.ic_round_star_24),
                    contentDescription = "star",
                    modifier = Modifier.padding(end = 4.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(text = "8.61", color = MaterialTheme.colorScheme.onSurfaceVariant)
            },
            onClick = { }
        )
    }
}