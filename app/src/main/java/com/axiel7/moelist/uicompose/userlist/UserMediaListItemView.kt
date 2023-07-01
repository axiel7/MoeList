package com.axiel7.moelist.uicompose.userlist

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.anime.Broadcast
import com.axiel7.moelist.data.model.anime.airingInString
import com.axiel7.moelist.data.model.anime.remainingText
import com.axiel7.moelist.data.model.media.ListStatus
import com.axiel7.moelist.data.model.media.WeekDay
import com.axiel7.moelist.data.model.media.calculateProgressBarValue
import com.axiel7.moelist.data.model.media.isCurrent
import com.axiel7.moelist.data.model.media.mediaFormatLocalized
import com.axiel7.moelist.uicompose.composables.MEDIA_POSTER_COMPACT_HEIGHT
import com.axiel7.moelist.uicompose.composables.MEDIA_POSTER_MEDIUM_HEIGHT
import com.axiel7.moelist.uicompose.composables.MEDIA_POSTER_MEDIUM_WIDTH
import com.axiel7.moelist.uicompose.composables.MEDIA_POSTER_SMALL_HEIGHT
import com.axiel7.moelist.uicompose.composables.MEDIA_POSTER_SMALL_WIDTH
import com.axiel7.moelist.uicompose.composables.MediaPoster
import com.axiel7.moelist.uicompose.composables.defaultPlaceholder
import com.axiel7.moelist.uicompose.theme.MoeListTheme
import com.axiel7.moelist.utils.Constants
import com.axiel7.moelist.utils.NumExtensions.toStringPositiveValueOrUnknown

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
    isVolumeProgress: Boolean,
    listStatus: ListStatus,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onClickPlus: () -> Unit,
) {
    val isAiring = remember { broadcast != null && mediaStatus == "currently_airing" }
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
                        text = if ((score ?: 0) == 0) Constants.UNKNOWN_CHAR else "$score",
                        modifier = Modifier.padding(start = 8.dp, top = 4.dp, end = 2.dp, bottom = 4.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Icon(
                        painter = painterResource(R.drawable.ic_round_star_16),
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
                        lineHeight = 22.sp,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 2
                    )
                    Text(
                        text = if (isAiring) broadcast!!.airingInString()
                            else mediaFormat?.mediaFormatLocalized() ?: "",
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = if (isAiring) MaterialTheme.colorScheme.primary
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${userProgress ?: 0}/${totalProgress.toStringPositiveValueOrUnknown()}",
                            )
                            if (isVolumeProgress) {
                                Icon(
                                    painter = painterResource(R.drawable.round_bookmark_24),
                                    contentDescription = stringResource(R.string.volumes),
                                    modifier = Modifier
                                        .padding(start = 4.dp)
                                        .size(16.dp)
                                )
                            }
                        }

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
                        trackColor = MaterialTheme.colorScheme.surfaceColorAtElevation(94.dp),
                        strokeCap = StrokeCap.Round
                    )
                }//:Column
            }//:Column
        }//:Row
    }//:Card
}

@Composable
fun StandardUserMediaListItemPlaceholder() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
    ) {
        Row(
            modifier = Modifier.height(MEDIA_POSTER_SMALL_HEIGHT.dp)
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
                Spacer(modifier = Modifier.size(16.dp))

                Text(
                    text = "??/??",
                    modifier = Modifier.defaultPlaceholder(visible = true)
                )
            }//:Column
        }//:Row
    }//:Column
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CompactUserMediaListItem(
    imageUrl: String?,
    title: String,
    score: Int?,
    mediaStatus: String?,
    userProgress: Int?,
    totalProgress: Int?,
    isVolumeProgress: Boolean,
    broadcast: Broadcast?,
    listStatus: ListStatus,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onClickPlus: () -> Unit,
) {
    val isAiring = remember { broadcast != null && mediaStatus == "currently_airing" }
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
                        .size(width = MEDIA_POSTER_SMALL_WIDTH.dp, height = MEDIA_POSTER_SMALL_WIDTH.dp)
                )

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(topEnd = 8.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if ((score ?: 0) == 0) Constants.UNKNOWN_CHAR else "$score",
                        modifier = Modifier.padding(start = 8.dp, top = 4.dp, end = 2.dp, bottom = 4.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Icon(
                        painter = painterResource(R.drawable.ic_round_star_16),
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
                    fontSize = 16.sp,
                    lineHeight = 19.sp,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = if (isAiring) 1 else 2
                )

                if (isAiring) {
                    Text(
                        text = broadcast!!.airingInString(),
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 16.sp,
                        lineHeight = 19.sp,
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${userProgress ?: 0}/${totalProgress.toStringPositiveValueOrUnknown()}",
                            fontSize = 16.sp,
                            lineHeight = 19.sp,
                        )
                        if (isVolumeProgress) {
                            Icon(
                                painter = painterResource(R.drawable.round_bookmark_24),
                                contentDescription = stringResource(R.string.volumes),
                                modifier = Modifier
                                    .padding(start = 4.dp)
                                    .size(16.dp)
                            )
                        }
                    }

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

@Composable
fun CompactUserMediaListItemPlaceholder() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
    ) {
        Row(
            modifier = Modifier.height(MEDIA_POSTER_SMALL_WIDTH.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(
                        width = MEDIA_POSTER_SMALL_WIDTH.dp,
                        height = MEDIA_POSTER_SMALL_WIDTH.dp
                    )
                    .clip(RoundedCornerShape(8.dp))
                    .defaultPlaceholder(visible = true)
            )

            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(horizontal = 16.dp),
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
        }//:Row
    }//:Column
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MinimalUserMediaListItem(
    title: String,
    score: Int?,
    userProgress: Int?,
    totalProgress: Int?,
    isVolumeProgress: Boolean,
    mediaStatus: String?,
    broadcast: Broadcast?,
    listStatus: ListStatus,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onClickPlus: () -> Unit,
) {
    val isAiring = remember { broadcast != null && mediaStatus == "currently_airing" }
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
                    fontSize = 16.sp,
                    lineHeight = 19.sp,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = if (isAiring) 1 else 2
                )

                if (isAiring) {
                    Text(
                        text = broadcast!!.airingInString(),
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
                            text = "${userProgress ?: 0}/${totalProgress.toStringPositiveValueOrUnknown()}",
                            fontSize = 16.sp,
                            lineHeight = 19.sp,
                        )
                        if (isVolumeProgress) {
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
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if ((score ?: 0) == 0) Constants.UNKNOWN_CHAR else "$score",
                            modifier = Modifier.padding(start = 8.dp, top = 4.dp, end = 2.dp, bottom = 4.dp),
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GridUserMediaListItem(
    imageUrl: String?,
    title: String,
    score: Int?,
    userProgress: Int?,
    totalProgress: Int?,
    isVolumeProgress: Boolean,
    mediaStatus: String?,
    broadcast: Broadcast?,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val isAiring = remember { broadcast != null && mediaStatus == "currently_airing" }
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
                    url = imageUrl,
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
                        text = if ((score ?: 0) == 0) Constants.UNKNOWN_CHAR else "$score",
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
                            .align(Alignment.TopCenter)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp))
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
                            text = broadcast!!.remainingText(),
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
                text = title,
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
                    text = "${userProgress ?: 0}/${totalProgress.toStringPositiveValueOrUnknown()}",
                    fontSize = 16.sp,
                    lineHeight = 19.sp,
                )
                if (isVolumeProgress) {
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
fun StandardUserMediaListItemPreview() {
    MoeListTheme {
        Column {
            StandardUserMediaListItem(
                imageUrl = null,
                title = "This is a large anime or manga title",
                score = null,
                mediaFormat = "tv",
                mediaStatus = "currently_airing",
                broadcast = Broadcast(WeekDay.SUNDAY, "12:00"),
                userProgress = 4,
                totalProgress = 24,
                isVolumeProgress = false,
                listStatus = ListStatus.WATCHING,
                onClick = { },
                onLongClick = { },
                onClickPlus = { }
            )
            StandardUserMediaListItemPlaceholder()
        }
    }
}

@Preview
@Composable
fun CompactUserMediaListItemPreview() {
    MoeListTheme {
        Column {
            CompactUserMediaListItem(
                imageUrl = null,
                title = "This is a very very very very large anime or manga title",
                score = null,
                mediaStatus = "currently_airing",
                userProgress = 4,
                totalProgress = 12,
                isVolumeProgress = true,
                broadcast = Broadcast(WeekDay.SUNDAY, "12:00"),
                listStatus = ListStatus.WATCHING,
                onClick = { },
                onLongClick = { },
                onClickPlus = { }
            )
            CompactUserMediaListItemPlaceholder()
        }
    }
}

@Preview
@Composable
fun MinimalUserMediaListItemPreview() {
    MoeListTheme {
        Column {
            MinimalUserMediaListItem(
                title = "This is a very very very very large anime or manga title",
                score = null,
                userProgress = 4,
                totalProgress = 12,
                isVolumeProgress = false,
                mediaStatus = "currently_airing",
                broadcast = Broadcast(WeekDay.SUNDAY, "12:00"),
                listStatus = ListStatus.WATCHING,
                onClick = { },
                onLongClick = { },
                onClickPlus = { }
            )
            MinimalUserMediaListItemPlaceholder()
        }
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
                    imageUrl = null,
                    title = "This is a very very very very large anime or manga title",
                    score = null,
                    userProgress = 4,
                    totalProgress = 12,
                    isVolumeProgress = true,
                    mediaStatus = "currently_airing",
                    broadcast = Broadcast(WeekDay.SUNDAY, "12:00"),
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