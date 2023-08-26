package com.axiel7.moelist.uicompose.userlist.composables

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
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
import com.axiel7.moelist.data.model.media.ListStatus
import com.axiel7.moelist.data.model.media.MediaStatus
import com.axiel7.moelist.uicompose.composables.defaultPlaceholder
import com.axiel7.moelist.uicompose.theme.MoeListTheme
import com.axiel7.moelist.utils.Constants
import com.axiel7.moelist.utils.NumExtensions.toStringPositiveValueOrUnknown

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MinimalUserMediaListItem(
    item: BaseUserMediaList<out BaseMediaNode>,
    listStatus: ListStatus,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onClickPlus: () -> Unit,
) {
    val broadcast = remember { (item.node as? AnimeNode)?.broadcast }
    val isAiring = remember { broadcast != null && item.node.status == MediaStatus.AIRING }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 4.dp)
            .combinedClickable(onLongClick = onLongClick, onClick = onClick),
    ) {
        Row(
            modifier = Modifier
                .height(84.dp)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = item.node.userPreferredTitle(),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp,
                    lineHeight = 19.sp,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = if (isAiring) 1 else 2
                )

                if (isAiring) {
                    Text(
                        text = broadcast?.airingInString() ?: stringResource(R.string.airing),
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 16.sp,
                        lineHeight = 19.sp,
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
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

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        if (item.listStatus?.hasRepeated() == true) {
                            Icon(
                                painter = painterResource(R.drawable.round_repeat_24),
                                contentDescription = stringResource(R.string.rewatching),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        if (item.listStatus?.hasNotes() == true) {
                            Icon(
                                painter = painterResource(R.drawable.round_notes_24),
                                contentDescription = stringResource(R.string.notes),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if ((item.listStatus?.score
                                        ?: 0) == 0
                                ) Constants.UNKNOWN_CHAR
                                else "${item.listStatus?.score}",
                                modifier = Modifier.padding(start = 8.dp, end = 2.dp),
                                color = MaterialTheme.colorScheme.secondary,
                                fontSize = 16.sp,
                            )
                            Icon(
                                painter = painterResource(R.drawable.ic_round_star_16),
                                contentDescription = "star",
                                modifier = Modifier.padding(end = 16.dp),
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }//:Row
            }//:Column

            if (listStatus.isCurrent()) {
                OutlinedButton(onClick = onClickPlus) {
                    Text(text = stringResource(R.string.plus_one))
                }
            }
        }//:Row
    }//:Card
}

@Composable
fun MinimalUserMediaListItemPlaceholder() {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .height(84.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "This is a loading placeholder",
            modifier = Modifier.defaultPlaceholder(visible = true),
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 17.sp,
            lineHeight = 22.sp,
            overflow = TextOverflow.Ellipsis,
            maxLines = 2
        )
        Text(
            text = "Placeholder",
            modifier = Modifier.defaultPlaceholder(visible = true),
        )

        Text(
            text = "??/??",
            modifier = Modifier.defaultPlaceholder(visible = true)
        )
    }//:Column
}

@Preview
@Composable
fun MinimalUserMediaListItemPreview() {
    MoeListTheme {
        Column {
            MinimalUserMediaListItem(
                item = exampleUserAnimeList,
                listStatus = ListStatus.WATCHING,
                onClick = { },
                onLongClick = { },
                onClickPlus = { }
            )
            MinimalUserMediaListItemPlaceholder()
        }
    }
}