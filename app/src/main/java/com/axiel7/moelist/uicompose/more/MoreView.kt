package com.axiel7.moelist.uicompose.more

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.axiel7.moelist.R
import com.axiel7.moelist.uicompose.composables.collapsable
import com.axiel7.moelist.uicompose.more.composables.FeedbackDialog
import com.axiel7.moelist.uicompose.more.composables.MoreItem
import com.axiel7.moelist.uicompose.theme.MoeListTheme
import com.axiel7.moelist.utils.ContextExtensions.openLink
import com.axiel7.moelist.utils.MAL_ANNOUNCEMENTS_URL
import com.axiel7.moelist.utils.MAL_NEWS_URL
import org.koin.androidx.compose.koinViewModel

const val MORE_DESTINATION = "more"

@Composable
fun MoreView(
    viewModel: MoreViewModel = koinViewModel(),
    navigateToSettings: () -> Unit,
    navigateToNotifications: () -> Unit,
    navigateToAbout: () -> Unit,
    topBarHeightPx: Float,
    topBarOffsetY: Animatable<Float, AnimationVector1D>,
    padding: PaddingValues,
) {
    val context = LocalContext.current
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

        HorizontalDivider()

        MoreItem(
            title = stringResource(R.string.anime_manga_news),
            subtitle = stringResource(R.string.news_summary),
            icon = R.drawable.ic_new_releases,
            onClick = { context.openLink(MAL_NEWS_URL) }
        )

        MoreItem(
            title = stringResource(R.string.mal_announcements),
            subtitle = stringResource(R.string.mal_announcements_summary),
            icon = R.drawable.ic_campaign,
            onClick = { context.openLink(MAL_ANNOUNCEMENTS_URL) }
        )

        HorizontalDivider()

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

        HorizontalDivider()

        MoreItem(
            title = stringResource(R.string.logout),
            subtitle = stringResource(R.string.logout_summary),
            icon = R.drawable.ic_round_power_settings_new_24,
            onClick = {
                viewModel.logOut()
            }
        )
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
