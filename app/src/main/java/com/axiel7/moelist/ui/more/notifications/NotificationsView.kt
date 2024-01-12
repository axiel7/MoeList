package com.axiel7.moelist.ui.more.notifications

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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.ui.composables.DefaultScaffoldWithTopAppBar
import com.axiel7.moelist.ui.theme.MoeListTheme
import org.koin.androidx.compose.koinViewModel

const val NOTIFICATIONS_DESTINATION = "notifications"

@Composable
fun NotificationsView(
    viewModel: NotificationsViewModel = koinViewModel(),
    navigateBack: () -> Unit,
    navigateToMediaDetails: (MediaType, Int) -> Unit,
) {
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()

    DefaultScaffoldWithTopAppBar(
        title = stringResource(R.string.notifications),
        navigateBack = navigateBack,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    viewModel.removeAllNotifications()
                }
            ) {
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
            items(notifications?.asMap()?.keys?.toTypedArray().orEmpty()) { key ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            navigateToMediaDetails(
                                MediaType.ANIME,
                                key
                                    .toString()
                                    .toInt()
                            )
                        },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = (notifications?.get(key) as? String).orEmpty(),
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    IconButton(
                        onClick = {
                            viewModel.removeNotification(key.name.toIntOrNull() ?: 0)
                        }
                    ) {
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