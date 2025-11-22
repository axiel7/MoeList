package com.axiel7.moelist.ui.season.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.anime.Season
import com.axiel7.moelist.data.model.anime.SeasonType
import com.axiel7.moelist.data.model.media.MediaSort
import com.axiel7.moelist.ui.composables.ChipWithMenu
import com.axiel7.moelist.ui.composables.SelectableIconToggleButton
import com.axiel7.moelist.ui.season.SeasonChartEvent
import com.axiel7.moelist.ui.season.SeasonChartUiState
import com.axiel7.moelist.ui.theme.MoeListTheme
import com.axiel7.moelist.ui.userlist.composables.SortChip

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SeasonChartFilterSheet(
    uiState: SeasonChartUiState,
    event: SeasonChartEvent?,
    onApply: () -> Unit,
    onDismiss: () -> Unit,
    sheetState: SheetState,
    bottomPadding: Dp = 0.dp
) {
    val scrollState = rememberLazyListState()

    LaunchedEffect(sheetState.isVisible) {
        val index = SeasonChartUiState.years.indexOf(uiState.season.year)
        if (index != -1) scrollState.scrollToItem(index)
    }

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = onDismiss,
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp + bottomPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(
                    onClick = onDismiss,
                    shapes = ButtonDefaults.shapes(),
                ) {
                    Text(text = stringResource(R.string.cancel))
                }

                Button(
                    onClick = onApply,
                    shapes = ButtonDefaults.shapes(),
                ) {
                    Text(text = stringResource(R.string.apply))
                }
            }

            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                SeasonType.entries.forEach {
                    SegmentedButton(
                        selected = it == uiState.seasonType,
                        onClick = { event?.setSeason(it) },
                        shape = SegmentedButtonDefaults.itemShape(
                            index = it.ordinal,
                            count = SeasonType.entries.size
                        )
                    ) {
                        Text(text = it.localized())
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Season.entries.forEach { season ->
                    SelectableIconToggleButton(
                        icon = season.icon,
                        tooltipText = season.localized(),
                        value = season,
                        selectedValue = uiState.season.season,
                        onClick = {
                            event?.setSeason(season = season)
                        }
                    )
                }
            }

            LazyRow(
                contentPadding = PaddingValues(horizontal = 8.dp),
                state = scrollState
            ) {
                items(SeasonChartUiState.years) {
                    FilterChip(
                        selected = uiState.season.year == it,
                        onClick = { event?.setSeason(year = it) },
                        label = { Text(text = it.toString()) },
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ChipWithMenu(
                    title = uiState.sort.localized(),
                    values = listOf(MediaSort.ANIME_NUM_USERS, MediaSort.ANIME_SCORE, MediaSort.ANIME_START_DATE),
                    selectedValue = uiState.sort,
                    onValueSelected = { value ->
                        if (value != null) event?.onChangeSort(value)
                    },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_round_sort_24),
                            contentDescription = stringResource(R.string.sort_by)
                        )
                    },
                    valueString = { it.localized() }
                )
                AssistChip(
                    onClick = {
                        event?.onChangeIsNew(!uiState.isNew)
                    },
                    label = {
                        Text(
                            text = stringResource(
                                id = if (uiState.isNew) R.string.new_anime else R.string.continuing_anime
                            )
                        )
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun SeasonChartFilterSheetPreview() {
    MoeListTheme {
        Surface {
            SeasonChartFilterSheet(
                uiState = SeasonChartUiState(),
                event = null,
                onApply = {},
                onDismiss = {},
                sheetState = rememberModalBottomSheetState(),
            )
        }
    }
}