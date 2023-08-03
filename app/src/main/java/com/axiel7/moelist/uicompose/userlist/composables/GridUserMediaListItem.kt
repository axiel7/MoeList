package com.axiel7.moelist.uicompose.userlist.composables

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.anime.AnimeNode
import com.axiel7.moelist.data.model.anime.exampleUserAnimeList
import com.axiel7.moelist.data.model.manga.UserMangaList
import com.axiel7.moelist.data.model.media.BaseMediaNode
import com.axiel7.moelist.data.model.media.BaseUserMediaList
import com.axiel7.moelist.data.model.media.MediaStatus
import com.axiel7.moelist.uicompose.composables.defaultPlaceholder
import com.axiel7.moelist.uicompose.composables.media.MEDIA_POSTER_MEDIUM_HEIGHT
import com.axiel7.moelist.uicompose.composables.media.MEDIA_POSTER_MEDIUM_WIDTH
import com.axiel7.moelist.uicompose.composables.media.MediaPoster
import com.axiel7.moelist.uicompose.theme.MoeListTheme
import com.axiel7.moelist.utils.Constants
import com.axiel7.moelist.utils.NumExtensions.toStringPositiveValueOrUnknown

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GridUserMediaListItem(
    item: BaseUserMediaList<out BaseMediaNode>,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val broadcast = remember { (item.node as? AnimeNode)?.broadcast }
    val isAiring = remember { broadcast != null && item.node.status == MediaStatus.AIRING }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onLongClick = onLongClick, onClick = onClick),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box {
                MediaPoster(
                    url = item.node.mainPicture?.large,
                    showShadow = false,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(MEDIA_POSTER_MEDIUM_HEIGHT.dp)
                )

                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .clip(RoundedCornerShape(topEnd = 8.dp, bottomStart = 8.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if ((item.listStatus?.score ?: 0) == 0) Constants.UNKNOWN_CHAR
                        else "${item.listStatus?.score}",
                        modifier = Modifier.padding(
                            start = 8.dp,
                            top = 4.dp,
                            end = 2.dp,
                            bottom = 4.dp
                        ),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Icon(
                        painter = painterResource(R.drawable.ic_round_star_16),
                        contentDescription = "star",
                        modifier = Modifier.padding(end = 4.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }//:Row
                if (isAiring) {
                    Row(
                        modifier = Modifier
                            .shadow(8.dp)
                            .fillMaxWidth()
                            .align(Alignment.TopCenter)
                            .clip(RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_round_rss_feed_24),
                            contentDescription = stringResource(R.string.airing),
                            modifier = Modifier
                                .padding(start = 8.dp, end = 4.dp)
                                .size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = broadcast!!.airingInShortString(),
                            modifier = Modifier.padding(end = 8.dp),
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }//:Box

            Text(
                text = item.node.userPreferredTitle(),
                modifier = Modifier.padding(start = 8.dp, top = 4.dp, end = 8.dp),
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp,
                overflow = TextOverflow.Ellipsis,
                maxLines = 2,
                minLines = 2,
            )

            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${item.userProgress() ?: 0}/${
                        item.totalProgress().toStringPositiveValueOrUnknown()
                    }",
                    fontSize = 16.sp,
                    lineHeight = 19.sp,
                )
                if ((item as? UserMangaList)?.listStatus?.isUsingVolumeProgress() == true) {
                    Icon(
                        painter = painterResource(R.drawable.round_bookmark_24),
                        contentDescription = stringResource(R.string.volumes),
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .size(16.dp)
                    )
                }
            }
        }//:Column
    }//:Card
}

@Composable
fun GridUserMediaListItemPlaceholder() {
    Column(
        modifier = Modifier.width(MEDIA_POSTER_MEDIUM_WIDTH.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(
                    width = MEDIA_POSTER_MEDIUM_WIDTH.dp,
                    height = MEDIA_POSTER_MEDIUM_HEIGHT.dp
                )
                .clip(RoundedCornerShape(8.dp))
                .defaultPlaceholder(visible = true)
        )
        Text(
            text = "This is a loading placeholder",
            modifier = Modifier
                .padding(top = 8.dp)
                .defaultPlaceholder(visible = true),
            fontSize = 16.sp,
            lineHeight = 18.sp,
        )
    }
}

@Preview
@Composable
fun GridUserMediaListItemPreview() {
    MoeListTheme {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = (MEDIA_POSTER_MEDIUM_WIDTH + 8).dp),
            contentPadding = PaddingValues(vertical = 8.dp, horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
        ) {
            items(3) {
                GridUserMediaListItem(
                    item = exampleUserAnimeList,
                    onClick = { },
                    onLongClick = { }
                )
            }
            items(3) {
                GridUserMediaListItemPlaceholder()
            }
        }
    }
}