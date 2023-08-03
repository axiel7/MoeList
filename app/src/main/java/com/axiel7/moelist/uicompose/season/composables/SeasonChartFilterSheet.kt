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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.anime.Season
import com.axiel7.moelist.uicompose.composables.SelectableIconToggleButton
import com.axiel7.moelist.uicompose.season.SeasonChartViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeasonChartFilterSheet(
    coroutineScope: CoroutineScope,
    sheetState: SheetState,
    viewModel: SeasonChartViewModel,
    bottomPadding: Dp = 0.dp
) {
    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = { coroutineScope.launch { sheetState.hide() } },
        windowInsets = WindowInsets(0, 0, 0, 0)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp + bottomPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = {
                    coroutineScope.launch { sheetState.hide() }
                }) {
                    Text(text = stringResource(R.string.cancel))
                }

                Button(onClick = {
                    viewModel.getSeasonalAnime()
                    coroutineScope.launch { sheetState.hide() }
                }) {
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
        }
    }
}