package com.axiel7.moelist.uicompose.details.composables

import android.Manifest
import android.os.Build
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.datastore.preferences.core.stringPreferencesKey
import com.axiel7.moelist.R
import com.axiel7.moelist.data.datastore.PreferencesDataStore.notificationsDataStore
import com.axiel7.moelist.data.model.anime.AnimeDetails
import com.axiel7.moelist.data.model.media.MediaStatus
import com.axiel7.moelist.uicompose.composables.BackIconButton
import com.axiel7.moelist.uicompose.composables.ShareButton
import com.axiel7.moelist.uicompose.composables.ViewInBrowserButton
import com.axiel7.moelist.uicompose.details.MediaDetailsViewModel
import com.axiel7.moelist.utils.ContextExtensions.openLink
import com.axiel7.moelist.utils.ContextExtensions.showToast
import com.axiel7.moelist.utils.DateUtils.parseDate
import com.axiel7.moelist.utils.NotificationWorker
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun MediaDetailsTopAppBar(
    viewModel: MediaDetailsViewModel,
    mediaUrl: String,
    scrollBehavior: TopAppBarScrollBehavior,
    navigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val savedForNotification = when (viewModel.mediaDetails?.status) {
        MediaStatus.AIRING -> remember {
            context.notificationsDataStore.data.map {
                it[stringPreferencesKey(viewModel.mediaDetails!!.id.toString())]
            }
        }.collectAsState(initial = null)

        MediaStatus.NOT_AIRED -> remember {
            context.notificationsDataStore.data.map {
                it[stringPreferencesKey("start_${viewModel.mediaDetails!!.id}")]
            }
        }.collectAsState(initial = null)

        else -> remember { mutableStateOf(null) }
    }
    val notificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberPermissionState(
            Manifest.permission.POST_NOTIFICATIONS
        )
    } else null

    TopAppBar(
        title = { Text(stringResource(R.string.title_details)) },
        navigationIcon = {
            BackIconButton(onClick = navigateBack)
        },
        actions = {
            if (viewModel.mediaDetails?.status == MediaStatus.AIRING
                || viewModel.mediaDetails?.status == MediaStatus.NOT_AIRED
            ) {
                IconButton(
                    onClick = {
                        val enable = savedForNotification.value == null
                        (viewModel.mediaDetails as? AnimeDetails)?.let { details ->
                            if (enable) {
                                if (notificationPermission == null
                                    || notificationPermission.status.isGranted
                                ) {
                                    scope.launch {
                                        if (details.status != MediaStatus.NOT_AIRED
                                            && details.broadcast?.dayOfTheWeek != null
                                            && details.broadcast.startTime != null
                                        ) {
                                            NotificationWorker.scheduleAiringAnimeNotification(
                                                context = context,
                                                title = details.title ?: "",
                                                animeId = details.id,
                                                weekDay = details.broadcast.dayOfTheWeek,
                                                jpHour = LocalTime.parse(details.broadcast.startTime)
                                            )
                                            context.showToast(R.string.airing_notification_enabled)
                                        } else if (details.status == MediaStatus.NOT_AIRED
                                            && details.startDate != null
                                        ) {
                                            val startDate = details.startDate.parseDate()
                                            if (startDate != null) {
                                                NotificationWorker.scheduleAnimeStartNotification(
                                                    context = context,
                                                    title = details.title ?: "",
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
                                    }
                                } else {
                                    notificationPermission.launchPermissionRequest()
                                }
                            } else {
                                scope.launch {
                                    NotificationWorker.removeAiringAnimeNotification(
                                        context = context,
                                        animeId = details.id
                                    )
                                    context.showToast("Notification disabled")
                                }
                            }
                        }
                    }
                ) {
                    Icon(
                        painter = painterResource(
                            if (savedForNotification.value != null) R.drawable.round_notifications_active_24
                            else R.drawable.round_notifications_off_24
                        ),
                        contentDescription = "notification"
                    )
                }
            }
            ViewInBrowserButton(onClick = { context.openLink(mediaUrl) })

            ShareButton(url = mediaUrl)
        },
        scrollBehavior = scrollBehavior
    )
}