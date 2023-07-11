package com.axiel7.moelist.uicompose.more

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.axiel7.moelist.R
import com.axiel7.moelist.uicompose.composables.collapsable
import com.axiel7.moelist.uicompose.theme.MoeListTheme
import com.axiel7.moelist.utils.Constants
import com.axiel7.moelist.utils.Constants.DISCORD_SERVER_URL
import com.axiel7.moelist.utils.Constants.GITHUB_ISSUES_URL
import com.axiel7.moelist.utils.ContextExtensions.openAction
import com.axiel7.moelist.utils.ContextExtensions.openLink
import com.axiel7.moelist.utils.UseCases.logOut
import kotlinx.coroutines.launch

const val MORE_TAB_DESTINATION = "more_tab"
const val MORE_DESTINATION = "more"

@Composable
fun MoreView(
    navigateToSettings: () -> Unit,
    navigateToNotifications: () -> Unit,
    navigateToAbout: () -> Unit,
    topBarHeightPx: Float,
    topBarOffsetY: Animatable<Float, AnimationVector1D>,
    padding: PaddingValues,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var openFeedbackDialog by remember { mutableStateOf(false) }

    if (openFeedbackDialog) {
        FeedbackDialog(
            onDismiss = { openFeedbackDialog = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .collapsable(
                state = scrollState,
                topBarHeightPx = topBarHeightPx,
                topBarOffsetY = topBarOffsetY,
            )
            .verticalScroll(scrollState)
            .padding(padding)
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_moelist_logo),
            contentDescription = stringResource(R.string.app_name),
            modifier = Modifier
                .padding(vertical = 24.dp)
                .fillMaxWidth()
                .height(60.dp),
            tint = MaterialTheme.colorScheme.onSurface
        )

        Divider()

        MoreItem(
            title = stringResource(R.string.anime_manga_news),
            subtitle = stringResource(R.string.news_summary),
            icon = R.drawable.ic_new_releases,
            onClick = { context.openLink(Constants.MAL_NEWS_URL) }
        )

        MoreItem(
            title = stringResource(R.string.mal_announcements),
            subtitle = stringResource(R.string.mal_announcements_summary),
            icon = R.drawable.ic_campaign,
            onClick = { context.openLink(Constants.MAL_ANNOUNCEMENTS_URL) }
        )

        Divider()

        MoreItem(
            title = stringResource(R.string.notifications),
            icon = R.drawable.round_notifications_24,
            onClick = navigateToNotifications
        )

        MoreItem(
            title = stringResource(R.string.settings),
            icon = R.drawable.ic_round_settings_24,
            onClick = navigateToSettings
        )

        MoreItem(
            title = stringResource(R.string.about),
            icon = R.drawable.ic_info,
            onClick = navigateToAbout
        )

        MoreItem(
            title = stringResource(R.string.feedback),
            icon = R.drawable.ic_round_feedback_24,
            onClick = {
                openFeedbackDialog = true
            }
        )

        Divider()

        MoreItem(
            title = stringResource(R.string.logout),
            subtitle = stringResource(R.string.logout_summary),
            icon = R.drawable.ic_round_power_settings_new_24,
            onClick = {
                coroutineScope.launch { context.logOut() }
            }
        )
    }
}

@Composable
fun FeedbackDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.cancel))
            }
        },
        text = {
            Column() {
                MoreItem(
                    title = stringResource(R.string.github),
                    icon = R.drawable.ic_github,
                    onClick = {
                        context.openAction(GITHUB_ISSUES_URL)
                    }
                )
                MoreItem(
                    title = stringResource(R.string.discord),
                    icon = R.drawable.ic_discord,
                    onClick = {
                        context.openAction(DISCORD_SERVER_URL)
                    }
                )
            }
        }
    )
}

@Composable
fun MoreItem(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    @DrawableRes icon: Int? = null,
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                painter = painterResource(icon),
                contentDescription = "",
                modifier = Modifier.padding(16.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        } else {
            Spacer(
                modifier = Modifier
                    .padding(16.dp)
                    .size(24.dp)
            )
        }

        Column(
            modifier = if (subtitle != null)
                Modifier.padding(16.dp)
            else Modifier.padding(horizontal = 16.dp)
        ) {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (subtitle != null) {
                Text(
                    text = subtitle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MorePreview() {
    MoeListTheme {
        MoreView(
            navigateToSettings = {},
            navigateToNotifications = {},
            navigateToAbout = {},
            padding = PaddingValues(),
            topBarHeightPx = 0f,
            topBarOffsetY = remember { Animatable(0f) }
        )
    }
}
