package com.axiel7.moelist.uicompose.userlist

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.anime.Broadcast
import com.axiel7.moelist.data.model.anime.airingInString
import com.axiel7.moelist.data.model.media.ListStatus
import com.axiel7.moelist.data.model.media.WeekDay
import com.axiel7.moelist.data.model.media.calculateProgressBarValue
import com.axiel7.moelist.data.model.media.isCurrent
import com.axiel7.moelist.data.model.media.mediaFormatLocalized
import com.axiel7.moelist.data.model.media.statusLocalized
import com.axiel7.moelist.uicompose.composables.MEDIA_POSTER_COMPACT_HEIGHT
import com.axiel7.moelist.uicompose.composables.MEDIA_POSTER_SMALL_HEIGHT
import com.axiel7.moelist.uicompose.composables.MEDIA_POSTER_SMALL_WIDTH
import com.axiel7.moelist.uicompose.composables.MediaPoster
import com.axiel7.moelist.uicompose.theme.MoeListTheme

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StandardUserMediaListItem(
    imageUrl: String?,
    title: String,
    score: Int?,
    mediaFormat: String?,
    mediaStatus: String?,
    broadcast: Broadcast?,
    userProgress: Int?,
    totalProgress: Int?,
    listStatus: ListStatus,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onClickPlus: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
            .combinedClickable(onLongClick = onLongClick, onClick = onClick),
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
                        .size(width = MEDIA_POSTER_SMALL_WIDTH.dp, height = MEDIA_POSTER_SMALL_HEIGHT.dp)
                )

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(topEnd = 8.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if ((score ?: 0) == 0) "─" else "$score",
                        modifier = Modifier.padding(start = 8.dp, top = 4.dp, end = 2.dp, bottom = 4.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Icon(
                        painter = painterResource(R.drawable.ic_round_star_24),
                        contentDescription = "star",
                        modifier = Modifier.padding(end = 4.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }//:Row
            }//:Box

            Column(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = title,
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 17.sp,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 2
                    )
                    Text(
                        text = buildString {
                            if (broadcast != null) {
                                append(broadcast.airingInString())
                            } else {
                                append(mediaFormat?.mediaFormatLocalized())
                                if (mediaStatus?.startsWith("currently") == true) {
                                    append(" • ")
                                    append(mediaStatus.statusLocalized())
                                }
                            }
                        },
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = if (broadcast != null) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface
                    )
                }//:Column

                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, bottom = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = "${userProgress ?: 0}/${totalProgress ?: 0}",
                        )

                        if (listStatus.isCurrent()) {
                            OutlinedButton(onClick = onClickPlus) {
                                Text(text = stringResource(R.string.plus_one))
                            }
                        }
                    }

                    LinearProgressIndicator(
                        progress = calculateProgressBarValue(userProgress, totalProgress),
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }//:Column
            }//:Column
        }//:Row
    }//:Card
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CompactUserMediaListItem(
    imageUrl: String?,
    title: String,
    score: Int?,
    userProgress: Int?,
    totalProgress: Int?,
    listStatus: ListStatus,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onClickPlus: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 4.dp)
            .combinedClickable(onLongClick = onLongClick, onClick = onClick),
    ) {
        Row(
            modifier = Modifier.height(MEDIA_POSTER_COMPACT_HEIGHT.dp)
        ) {
            Box(
                contentAlignment = Alignment.BottomStart
            ) {
                MediaPoster(
                    url = imageUrl,
                    showShadow = false,
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier
                        .size(width = MEDIA_POSTER_SMALL_WIDTH.dp, height = MEDIA_POSTER_SMALL_HEIGHT.dp)
                )

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(topEnd = 8.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if ((score ?: 0) == 0) "─" else "$score",
                        modifier = Modifier.padding(start = 8.dp, top = 4.dp, end = 2.dp, bottom = 4.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Icon(
                        painter = painterResource(R.drawable.ic_round_star_24),
                        contentDescription = "star",
                        modifier = Modifier.padding(end = 4.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }//:Box

            Column(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    modifier = Modifier
                        .padding(start = 16.dp, end = 16.dp, top = 8.dp),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 17.sp,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 2
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "${userProgress ?: 0}/${totalProgress ?: 0}",
                    )

                    if (listStatus.isCurrent()) {
                        OutlinedButton(onClick = onClickPlus) {
                            Text(text = stringResource(R.string.plus_one))
                        }
                    }
                }
            }//:Column
        }//:Row
    }//:Card
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MinimalUserMediaListItem(
    title: String,
    userProgress: Int?,
    totalProgress: Int?,
    listStatus: ListStatus,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onClickPlus: () -> Unit,
) {
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
                    text = title,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 17.sp,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 2
                )

                Text(
                    text = "${userProgress ?: 0}/${totalProgress ?: 0}",
                )
            }//:Column

            if (listStatus.isCurrent()) {
                OutlinedButton(onClick = onClickPlus) {
                    Text(text = stringResource(R.string.plus_one))
                }
            }
        }//:Row
    }//:Card
}

@Preview
@Composable
fun StandardUserMediaListItemPreview() {
    MoeListTheme {
        StandardUserMediaListItem(
            imageUrl = null,
            title = "This is a large anime or manga title",
            score = null,
            mediaFormat = "tv",
            mediaStatus = "currently_airing",
            broadcast = Broadcast(WeekDay.SUNDAY, "12:00"),
            userProgress = 4,
            totalProgress = 24,
            listStatus = ListStatus.WATCHING,
            onClick = { },
            onLongClick = { },
            onClickPlus = { }
        )
    }
}

@Preview
@Composable
fun CompactUserMediaListItemPreview() {
    MoeListTheme {
        CompactUserMediaListItem(
            imageUrl = null,
            title = "This is a very very very very large anime or manga title",
            score = null,
            userProgress = 4,
            totalProgress = 12,
            listStatus = ListStatus.WATCHING,
            onClick = { },
            onLongClick = { },
            onClickPlus = { }
        )
    }
}

@Preview
@Composable
fun MinimalUserMediaListItemPreview() {
    MoeListTheme {
        MinimalUserMediaListItem(
            title = "This is a very very very very large anime or manga title",
            userProgress = 4,
            totalProgress = 12,
            listStatus = ListStatus.WATCHING,
            onClick = { },
            onLongClick = { },
            onClickPlus = { }
        )
    }
}