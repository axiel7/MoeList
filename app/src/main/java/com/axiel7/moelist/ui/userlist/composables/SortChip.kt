package com.axiel7.moelist.ui.userlist.composables

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.media.MediaSort
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.ui.composables.ChipWithMenu
import com.axiel7.moelist.ui.userlist.UserMediaListEvent
import com.axiel7.moelist.ui.userlist.UserMediaListUiState

@Composable
fun SortChip(
    uiState: UserMediaListUiState,
    event: UserMediaListEvent?,
    modifier: Modifier = Modifier
) {
    ChipWithMenu(
        title = uiState.listSort?.localized() ?: stringResource(R.string.sort_by),
        values = if (uiState.mediaType == MediaType.MANGA) MediaSort.mangaListSortItems
        else MediaSort.animeListSortItems,
        selectedValue = uiState.listSort,
        onValueSelected = { value ->
            if (value != null) event?.onChangeSort(value)
        },
        modifier = modifier,
        leadingIcon = {
            Icon(
                painter = painterResource(R.drawable.ic_round_sort_24),
                contentDescription = stringResource(R.string.sort_by)
            )
        },
        valueString = { it.localized() }
    )
}