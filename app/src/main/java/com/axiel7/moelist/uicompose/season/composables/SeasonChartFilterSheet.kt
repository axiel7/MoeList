package com.axiel7.moelist.uicompose.season.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.anime.Season
import com.axiel7.moelist.data.model.media.MediaSort
import com.axiel7.moelist.uicompose.composables.SelectableIconToggleButton
import com.axiel7.moelist.uicompose.season.SeasonChartViewModel
import com.axiel7.moelist.uicompose.theme.MoeListTheme
import com.axiel7.moelist.uicompose.userlist.composables.SortChip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeasonChartFilterSheet(
    onDismiss: () -> Unit,
    sheetState: SheetState,
    viewModel: SeasonChartViewModel,
    bottomPadding: Dp = 0.dp
) {
    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = onDismiss,
        windowInsets = WindowInsets(0, 0, 0, 0)
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
                TextButton(onClick = onDismiss) {
                    Text(text = stringResource(R.string.cancel))
                }

                Button(
                    onClick = {
                        viewModel.getSeasonalAnime()
                        onDismiss()
                    }
                ) {
                    Text(text = stringResource(R.string.apply))
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Season.values().forEach { season ->
                    SelectableIconToggleButton(
                        icon = season.icon,
                        tooltipText = season.localized(),
                        value = season,
                        selectedValue = viewModel.season.season,
                        onClick = {
                            viewModel.setSeason(season = season)
                        }
                    )
                }
            }

            LazyRow(
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                items(viewModel.years) {
                    FilterChip(
                        selected = viewModel.season.year == it,
                        onClick = { viewModel.setSeason(year = it) },
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
                Text(text = stringResource(R.string.sort_by))

                SortChip(
                    text = viewModel.sort.localized(),
                    onClick = {
                        // add a dialog?
                        if (viewModel.sort == MediaSort.ANIME_NUM_USERS) {
                            viewModel.onChangeSort(MediaSort.ANIME_SCORE)
                        } else {
                            viewModel.onChangeSort(MediaSort.ANIME_NUM_USERS)
                        }
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
        SeasonChartFilterSheet(
            onDismiss = {},
            sheetState = rememberModalBottomSheetState(),
            viewModel = viewModel()
        )
    }
}