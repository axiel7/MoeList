package com.axiel7.moelist.uicompose.userlist.composables

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
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.data.model.media.animeListSortItems
import com.axiel7.moelist.data.model.media.localized
import com.axiel7.moelist.data.model.media.mangaListSortItems
import com.axiel7.moelist.uicompose.userlist.UserMediaListViewModel

@Composable
fun MediaListSortDialog(
    viewModel: UserMediaListViewModel
) {
    val configuration = LocalConfiguration.current
    val sortOptions = remember {
        if (viewModel.mediaType == MediaType.ANIME) animeListSortItems else mangaListSortItems
    }
    var selectedIndex by remember {
        mutableIntStateOf(sortOptions.indexOf(viewModel.listSort))
    }
    AlertDialog(
        onDismissRequest = { viewModel.openSortDialog = false },
        confirmButton = {
            TextButton(onClick = {
                viewModel.setSort(sortOptions[selectedIndex])
                viewModel.openSortDialog = false
            }) {
                Text(text = stringResource(R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = { viewModel.openSortDialog = false }) {
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