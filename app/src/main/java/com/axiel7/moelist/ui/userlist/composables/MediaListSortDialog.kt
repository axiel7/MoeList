package com.axiel7.moelist.ui.userlist.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.media.MediaSort
import com.axiel7.moelist.data.model.media.MediaSort.Companion.animeListSortItems
import com.axiel7.moelist.data.model.media.MediaSort.Companion.mangaListSortItems
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.ui.userlist.UserMediaListEvent
import com.axiel7.moelist.ui.userlist.UserMediaListUiState

@Composable
fun MediaListSortDialog(
    uiState: UserMediaListUiState,
    event: UserMediaListEvent?,
) {
    val configuration = LocalConfiguration.current
    val sortOptions = remember {
        if (uiState.mediaType == MediaType.ANIME) animeListSortItems else mangaListSortItems
    }
    var selectedIndex by remember {
        mutableIntStateOf(sortOptions.indexOf(uiState.listSort ?: MediaSort.SCORE))
    }
    AlertDialog(
        onDismissRequest = { event?.toggleSortDialog(false) },
        confirmButton = {
            TextButton(
                onClick = {
                    sortOptions.getOrNull(selectedIndex)?.let {
                        event?.onChangeSort(it)
                        event?.toggleSortDialog(false)
                    }
                }
            ) {
                Text(text = stringResource(R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = { event?.toggleSortDialog(false) }) {
                Text(text = stringResource(R.string.cancel))
            }
        },
        title = { Text(text = stringResource(R.string.sort_by)) },
        text = {
            LazyColumn(
                modifier = Modifier.sizeIn(
                    maxHeight = (configuration.screenHeightDp - 48).dp
                )
            ) {
                itemsIndexed(sortOptions) { index, sort ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedIndex = index },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedIndex == index,
                            onClick = { selectedIndex = index }
                        )
                        Text(text = sort.localized())
                    }
                }
            }
        }
    )
}