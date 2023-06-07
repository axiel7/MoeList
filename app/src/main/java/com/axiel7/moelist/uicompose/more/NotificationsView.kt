package com.axiel7.moelist.uicompose.more

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.axiel7.moelist.R
import com.axiel7.moelist.data.datastore.PreferencesDataStore.notificationsDataStore
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.uicompose.composables.DefaultScaffoldWithTopAppBar
import com.axiel7.moelist.uicompose.theme.MoeListTheme
import com.axiel7.moelist.utils.NotificationWorker
import kotlinx.coroutines.launch

const val NOTIFICATIONS_DESTINATION = "notifications"

@Composable
fun NotificationsView(
    navigateBack: () -> Unit,
    navigateToMediaDetails: (MediaType, Int) -> Unit,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val notifications = remember {
        context.notificationsDataStore.data
    }.collectAsState(initial = null)

    DefaultScaffoldWithTopAppBar(
        title = stringResource(R.string.notifications),
        navigateBack = navigateBack,
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = {
                coroutineScope.launch {
                    NotificationWorker.removeAllNotifications(context)
                }
            }) {
                Icon(
                    painter = painterResource(R.drawable.round_delete_sweep_24),
                    contentDescription = stringResource(R.string.delete_all)
                )
                Text(
                    text = stringResource(R.string.delete_all),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxWidth(),
            contentPadding = PaddingValues(8.dp)
        ) {
            items(notifications.value?.asMap()?.keys?.toTypedArray() ?: emptyArray()) { key ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            navigateToMediaDetails(MediaType.ANIME, key.toString().toInt())
                        },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notifications.value?.get(key) as? String ?: "",
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    IconButton(onClick = {
                        coroutineScope.launch {
                            NotificationWorker.removeAiringAnimeNotification(
                                context = context,
                                animeId = key.name.toIntOrNull() ?: 0
                            )
                        }
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.delete_outline_24),
                            contentDescription = stringResource(R.string.delete)
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NotificationsPreview() {
    MoeListTheme {
        NotificationsView(
            navigateBack = {},
            navigateToMediaDetails = { _, _ -> },
        )
    }
}