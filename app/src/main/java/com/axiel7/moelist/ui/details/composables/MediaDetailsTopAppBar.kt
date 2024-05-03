package com.axiel7.moelist.ui.details.composables

import android.Manifest
import android.os.Build
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.anime.AnimeDetails
import com.axiel7.moelist.data.model.media.MediaStatus
import com.axiel7.moelist.ui.composables.BackIconButton
import com.axiel7.moelist.ui.composables.ShareButton
import com.axiel7.moelist.ui.composables.ViewInBrowserButton
import com.axiel7.moelist.ui.details.MediaDetailsEvent
import com.axiel7.moelist.ui.details.MediaDetailsUiState
import com.axiel7.moelist.utils.ContextExtensions.openLink
import com.axiel7.moelist.utils.ContextExtensions.showToast
import com.axiel7.moelist.utils.DateUtils.parseDate
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun MediaDetailsTopAppBar(
    uiState: MediaDetailsUiState,
    event: MediaDetailsEvent?,
    scrollBehavior: TopAppBarScrollBehavior,
    navigateBack: () -> Unit
) {
    val context = LocalContext.current
    val isPreview = LocalInspectionMode.current
    val savedForNotification = when (uiState.mediaDetails?.status) {
        MediaStatus.AIRING -> uiState.notification
        MediaStatus.NOT_AIRED -> uiState.startNotification
        else -> null
    }

    fun onClickNotification(permissionGranted: Boolean) {
        val enable = savedForNotification == null
        (uiState.mediaDetails as? AnimeDetails)?.let { details ->
            if (enable && permissionGranted) {
                if (details.status != MediaStatus.NOT_AIRED
                    && details.broadcast?.dayOfTheWeek != null
                    && details.broadcast.startTime != null
                ) {
                    event?.scheduleAiringAnimeNotification(
                        title = details.title.orEmpty(),
                        animeId = details.id,
                        weekDay = details.broadcast.dayOfTheWeek,
                        jpHour = LocalTime.parse(details.broadcast.startTime)
                    )
                    context.showToast(R.string.airing_notification_enabled)
                } else if (details.status == MediaStatus.NOT_AIRED && details.startDate != null) {
                    val startDate = details.startDate.parseDate()
                    if (startDate != null) {
                        event?.scheduleAnimeStartNotification(
                            title = details.title.orEmpty(),
                            animeId = details.id,
                            startDate = startDate
                        )
                        context.showToast(R.string.start_airing_notification_enabled)
                    } else {
                        context.showToast(R.string.invalid_start_date)
                    }
                } else {
                    if (details.broadcast?.dayOfTheWeek == null
                        || details.broadcast.startTime == null
                    ) {
                        context.showToast(R.string.invalid_broadcast)
                    } else if (details.startDate == null) {
                        context.showToast(R.string.invalid_start_date)
                    }
                }
            } else {
                event?.removeAiringAnimeNotification(animeId = details.id)
                context.showToast("Notification disabled")
            }
        }
    }

    val notificationPermission =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !isPreview) {
        rememberPermissionState(
            permission = Manifest.permission.POST_NOTIFICATIONS,
            onPermissionResult = { onClickNotification(it) }
        )
    } else null

    TopAppBar(
        title = { },
        navigationIcon = {
            BackIconButton(onClick = navigateBack)
        },
        actions = {
            if (uiState.mediaDetails?.status == MediaStatus.AIRING
                || uiState.mediaDetails?.status == MediaStatus.NOT_AIRED
            ) {
                IconButton(
                    onClick = {
                        if (notificationPermission == null || notificationPermission.status.isGranted) {
                            onClickNotification(true)
                        } else {
                            notificationPermission.launchPermissionRequest()
                        }
                    }
                ) {
                    Icon(
                        painter = painterResource(
                            if (savedForNotification != null) R.drawable.round_notifications_active_24
                            else R.drawable.round_notifications_off_24
                        ),
                        contentDescription = "notification"
                    )
                }
            }
            ViewInBrowserButton(onClick = { context.openLink(uiState.mediaDetails?.malUrl.orEmpty()) })

            ShareButton(url = uiState.mediaDetails?.malUrl.orEmpty())
        },
        scrollBehavior = scrollBehavior
    )
}