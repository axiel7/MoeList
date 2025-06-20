package com.axiel7.moelist.ui.more

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
import androidx.compose.material3.Surface
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
import androidx.lifecycle.compose.dropUnlessResumed
import com.axiel7.moelist.R
import com.axiel7.moelist.ui.base.navigation.NavActionManager
import com.axiel7.moelist.ui.composables.collapsable
import com.axiel7.moelist.ui.more.composables.FeedbackDialog
import com.axiel7.moelist.ui.more.composables.MoreItem
import com.axiel7.moelist.ui.theme.MoeListTheme
import com.axiel7.moelist.utils.ContextExtensions.openCustomTab
import com.axiel7.moelist.utils.MAL_ANNOUNCEMENTS_URL
import com.axiel7.moelist.utils.MAL_NEWS_URL
import org.koin.androidx.compose.koinViewModel

@Composable
fun MoreView(
    isLoggedIn: Boolean,
    navActionManager: NavActionManager,
    topBarHeightPx: Float,
    topBarOffsetY: Animatable<Float, AnimationVector1D>,
    padding: PaddingValues,
) {
    val viewModel: MoreViewModel = koinViewModel()

    MoreViewContent(
        event = viewModel,
        navActionManager = navActionManager,
        topBarHeightPx = topBarHeightPx,
        topBarOffsetY = topBarOffsetY,
        padding = padding,
        isLoggedIn = isLoggedIn
    )
}

@Composable
private fun MoreViewContent(
    isLoggedIn: Boolean,
    event: MoreEvent?,
    navActionManager: NavActionManager,
    topBarHeightPx: Float = 0f,
    topBarOffsetY: Animatable<Float, AnimationVector1D> = Animatable(0f),
    padding: PaddingValues = PaddingValues(),
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
            onClick = { context.openCustomTab(MAL_NEWS_URL) }
        )

        MoreItem(
            title = stringResource(R.string.mal_announcements),
            subtitle = stringResource(R.string.mal_announcements_summary),
            icon = R.drawable.ic_campaign,
            onClick = { context.openCustomTab(MAL_ANNOUNCEMENTS_URL) }
        )

        HorizontalDivider()

        MoreItem(
            title = stringResource(R.string.notifications),
            icon = R.drawable.round_notifications_24,
            onClick = dropUnlessResumed { navActionManager.toNotifications() }
        )

        MoreItem(
            title = stringResource(R.string.settings),
            icon = R.drawable.ic_round_settings_24,
            onClick = dropUnlessResumed { navActionManager.toSettings() }
        )

        MoreItem(
            title = stringResource(R.string.about),
            icon = R.drawable.ic_info,
            onClick = dropUnlessResumed { navActionManager.toAbout() }
        )

        MoreItem(
            title = stringResource(R.string.feedback),
            icon = R.drawable.ic_round_feedback_24,
            onClick = {
                openFeedbackDialog = true
            }
        )

        if (isLoggedIn) {
            HorizontalDivider()

            MoreItem(
                title = stringResource(R.string.logout),
                subtitle = stringResource(R.string.logout_summary),
                icon = R.drawable.ic_round_power_settings_new_24,
                onClick = {
                    event?.logOut()
                }
            )
        }
    }
}

@Preview
@Composable
fun MorePreview() {
    MoeListTheme {
        Surface {
            MoreViewContent(
                event = null,
                navActionManager = NavActionManager.rememberNavActionManager(),
                isLoggedIn = true
            )
        }
    }
}
