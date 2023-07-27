package com.axiel7.moelist.uicompose.userlist.item

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
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
                        .size(
                            width = MEDIA_POSTER_SMALL_WIDTH.dp,
                            height = MEDIA_POSTER_SMALL_HEIGHT.dp
                        )
                )

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(topEnd = 8.dp))
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