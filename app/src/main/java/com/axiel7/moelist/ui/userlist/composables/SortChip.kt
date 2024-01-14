package com.axiel7.moelist.ui.userlist.composables

import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.axiel7.moelist.R
import com.axiel7.moelist.ui.userlist.UserMediaListEvent
import com.axiel7.moelist.ui.userlist.UserMediaListUiState

@Composable
fun SortChip(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AssistChip(
        onClick = onClick,
        label = { Text(text = text) },
        modifier = modifier,
        leadingIcon = {
            Icon(
                painter = painterResource(R.drawable.ic_round_sort_24),
                contentDescription = stringResource(R.string.sort_by)
            )
        }
    )
}

@Composable
fun SortChip(
    uiState: UserMediaListUiState,
    event: UserMediaListEvent?,
    modifier: Modifier = Modifier
) {
    SortChip(
        text = uiState.listSort?.localized() ?: stringResource(R.string.sort_by),
        onClick = { event?.toggleSortDialog(true) },
        modifier = modifier,
    )
}