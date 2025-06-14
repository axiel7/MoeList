package com.axiel7.moelist.ui.home.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.axiel7.moelist.data.model.anime.AnimeRanking
import com.axiel7.moelist.ui.composables.media.MEDIA_POSTER_SMALL_HEIGHT
import com.axiel7.moelist.ui.composables.media.MEDIA_POSTER_SMALL_WIDTH
import com.axiel7.moelist.ui.composables.media.MediaPoster
import com.axiel7.moelist.ui.composables.score.SmallScoreIndicator

@Composable
fun AiringAnimeHorizontalItem(
    item: AnimeRanking,
    hideScore: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .sizeIn(maxWidth = 300.dp, minWidth = 250.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
    ) {
        Box(
            contentAlignment = Alignment.BottomStart
        ) {
            MediaPoster(
                url = item.node.mainPicture?.large,
                showShadow = false,
                modifier = Modifier
                    .size(
                        width = MEDIA_POSTER_SMALL_WIDTH.dp,
                        height = MEDIA_POSTER_SMALL_HEIGHT.dp
                    )
            )

            item.node.myListStatus?.status?.let { status ->
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(topEnd = 8.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(status.icon),
                        contentDescription = status.localized(),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        Column(
            modifier = Modifier.padding(start = 16.dp)
        ) {
            Text(
                text = item.node.userPreferredTitle(),
                fontSize = 18.sp,
                overflow = TextOverflow.Ellipsis,
                maxLines = 2,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = item.node.broadcast?.airingInString().orEmpty(),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (!hideScore) {
                SmallScoreIndicator(
                    score = item.node.mean
                )
            }
        }
    }
}