package com.axiel7.moelist.ui.userlist.composables

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.anime.AnimeNode
import com.axiel7.moelist.data.model.anime.exampleUserAnimeList
import com.axiel7.moelist.data.model.manga.UserMangaList
import com.axiel7.moelist.data.model.media.BaseMediaNode
import com.axiel7.moelist.data.model.media.BaseUserMediaList
import com.axiel7.moelist.data.model.media.ListStatus
import com.axiel7.moelist.ui.composables.RollingNumberText
import com.axiel7.moelist.ui.composables.defaultPlaceholder
import com.axiel7.moelist.ui.composables.media.MEDIA_POSTER_MEDIUM_HEIGHT
import com.axiel7.moelist.ui.composables.media.MEDIA_POSTER_MEDIUM_WIDTH
import com.axiel7.moelist.ui.composables.media.MediaPoster
import com.axiel7.moelist.ui.theme.MoeListTheme
import com.axiel7.moelist.utils.NumExtensions.toStringPositiveValueOrUnknown
import com.axiel7.moelist.utils.UNKNOWN_CHAR

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StandardUserMediaListItem(
    item: BaseUserMediaList<out BaseMediaNode>,
    listStatus: ListStatus?,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onClickPlus: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val totalProgress = remember { item.totalProgress() }
    val userProgress = item.userProgress()
    val broadcast = remember { (item.node as? AnimeNode)?.broadcast }
    val isAiring = remember { item.isAiring }
    val haptic = LocalHapticFeedback.current

    val cardScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "cardScale"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .scale(cardScale)
            .combinedClickable(
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongClick()
                },
                onClick = onClick
            ),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .height(IntrinsicSize.Max)
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box {
                MediaPoster(
                    url = item.node.mainPicture?.large,
                    showShadow = true,
                    modifier = Modifier
                        .height(MEDIA_POSTER_MEDIUM_HEIGHT.dp)
                        .width(MEDIA_POSTER_MEDIUM_WIDTH.dp)
                        .clip(RoundedCornerShape(20.dp))
                )

                // Glassmorphism Score Badge
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .clip(CircleShape)
                        .background(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.listStatus?.score?.takeIf { it > 0 }?.toString() ?: UNKNOWN_CHAR,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = Icons.Rounded.Star,
                        contentDescription = "star",
                        modifier = Modifier
                            .padding(start = 2.dp)
                            .size(14.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
                    .fillMaxHeight()
            ) {
                // Title section
                Text(
                    text = item.node.userPreferredTitle(),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Badges row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ExpressiveBadge(
                        text = item.node.mediaFormat?.localized().orEmpty(),
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    if (isAiring) {
                        ExpressiveBadge(
                            text = broadcast?.airingInString() ?: stringResource(R.string.airing),
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Integrated Progress Section
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Progress Container
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        tonalElevation = 1.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(
                                            MaterialTheme.colorScheme.surfaceContainerHighest
                                        )
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    RollingNumberText(
                                        targetValue = userProgress ?: 0,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "/${totalProgress.toStringPositiveValueOrUnknown()}",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                            alpha = 0.6f
                                        )
                                    )
                                }
                                if ((item as? UserMangaList)?.listStatus?.isUsingVolumeProgress() == true) {
                                    Icon(
                                        imageVector = Icons.Rounded.Bookmark,
                                        contentDescription = stringResource(R.string.volumes),
                                        modifier = Modifier
                                            .padding(start = 6.dp)
                                            .size(18.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            if (listStatus?.isCurrent() == true) {
                                FilledTonalButton(
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                                        onClickPlus()
                                    },
                                    modifier = Modifier.height(36.dp),
                                    shape = RoundedCornerShape(18.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Add,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "1",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    if ((totalProgress ?: 0) > 0) {
                        val targetProgressBarColor = when (listStatus) {
                            ListStatus.COMPLETED -> MaterialTheme.colorScheme.tertiary
                            ListStatus.ON_HOLD -> MaterialTheme.colorScheme.outline
                            ListStatus.DROPPED -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.primary
                        }

                        val animatedProgress by animateFloatAsState(
                            targetValue = item.calculateProgressBarValue(),
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness = Spring.StiffnessMedium
                            ),
                            label = "progressBarAnimation"
                        )
                        val animatedProgressBarColor by animateColorAsState(
                            targetValue = targetProgressBarColor,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness = Spring.StiffnessMedium
                            ),
                            label = "progressBarColorAnimation"
                        )

                        LinearProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(CircleShape),
                            color = animatedProgressBarColor,
                            trackColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            strokeCap = StrokeCap.Round
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ExpressiveBadge(
    text: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = MaterialTheme.typography.labelSmall,
    paddingValues: PaddingValues = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
    border: BorderStroke? = null,
    tonalElevation: androidx.compose.ui.unit.Dp = 0.dp,
    content: @Composable (() -> Unit)? = null
) {
    if (text.isEmpty() && content == null) return

    Surface(
        modifier = modifier,
        color = containerColor,
        contentColor = contentColor,
        shape = RoundedCornerShape(8.dp),
        border = border,
        tonalElevation = tonalElevation
    ) {
        Box(modifier = Modifier.padding(paddingValues)) {
            if (content != null) {
                content()
            } else {
                Text(
                    text = text,
                    style = textStyle,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun StandardUserMediaListItemPlaceholder() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Row(
            modifier = Modifier
                .height(IntrinsicSize.Max)
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .height(MEDIA_POSTER_MEDIUM_HEIGHT.dp)
                    .width(MEDIA_POSTER_MEDIUM_WIDTH.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .defaultPlaceholder(visible = true)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
                    .fillMaxHeight()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(20.dp)
                        .clip(CircleShape)
                        .defaultPlaceholder(visible = true)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .width(100.dp)
                        .height(16.dp)
                        .clip(CircleShape)
                        .defaultPlaceholder(visible = true)
                )

                Spacer(modifier = Modifier.weight(1f))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .defaultPlaceholder(visible = true)
                )
            }
        }
    }
}

@Preview
@Composable
fun StandardUserMediaListItemPreview() {
    MoeListTheme {
        StandardUserMediaListItem(
            item = exampleUserAnimeList,
            listStatus = ListStatus.WATCHING,
            onClick = {},
            onLongClick = {},
            onClickPlus = {}
        )
    }
}
