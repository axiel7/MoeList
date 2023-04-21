package com.axiel7.moelist.uicompose.details

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.media.*
import com.axiel7.moelist.uicompose.composables.ClickableOutlinedTextField
import com.axiel7.moelist.uicompose.theme.MoeListTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMediaSheet(
    coroutineScope: CoroutineScope,
    sheetState: SheetState,
    viewModel: MediaDetailsViewModel,
) {
    val statusValues = if (viewModel.mediaType == MediaType.ANIME) listStatusAnimeValues() else listStatusMangaValues()
    var selectedStatus by remember { mutableStateOf(statusValues[0]) }

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = { coroutineScope.launch { sheetState.hide() } }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
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

                Button(onClick = { /*TODO*/ }) {
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
                statusValues.forEach { status ->
                    StatusItem(
                        status = status,
                        selectedStatus = selectedStatus,
                        onClick = {
                            selectedStatus = status
                        }
                    )
                }
            }

            EditMediaProgressRow(
                label = if (viewModel.mediaType == MediaType.ANIME) stringResource(R.string.episodes)
                else stringResource(R.string.chapters),
                progress = viewModel.progress,
                modifier = Modifier.padding(horizontal = 16.dp),
                totalProgress = viewModel.basicDetails?.totalDuration(),
                onValueChange = { viewModel.onChangeProgress(it.toIntOrNull()) },
                onMinusClick = { viewModel.onChangeProgress(viewModel.progress - 1) },
                onPlusClick = { viewModel.onChangeProgress(viewModel.progress + 1) }
            )

            if (viewModel.mediaType == MediaType.MANGA) {
                EditMediaProgressRow(
                    label = stringResource(R.string.volumes),
                    progress = viewModel.volumeProgress,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    totalProgress = viewModel.mangaDetails?.numVolumes,
                    onValueChange = { viewModel.onChangeVolumeProgress(it.toIntOrNull()) },
                    onMinusClick = { viewModel.onChangeVolumeProgress(viewModel.volumeProgress - 1) },
                    onPlusClick = { viewModel.onChangeVolumeProgress(viewModel.volumeProgress + 1) }
                )
            }

            Text(
                text = stringResource(R.string.score_value).format(viewModel.score.scoreText()),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp),
                fontWeight = FontWeight.Bold
            )
            Slider(
                value = viewModel.score.toFloat(),
                onValueChange = { viewModel.score = it.toInt() },
                modifier = Modifier.padding(horizontal = 16.dp),
                valueRange = 0f..10f,
                steps = 10
            )

            Divider(modifier = Modifier.padding(vertical = 16.dp))

            ClickableOutlinedTextField(
                value = viewModel.startDate ?: "",
                onValueChange = {  },
                label = { Text(text = stringResource(R.string.start_date)) },
                onClick = { viewModel.openDatePicker = true }
            )
            ClickableOutlinedTextField(
                value = viewModel.endDate ?: "",
                onValueChange = {  },
                modifier = Modifier.padding(vertical = 8.dp),
                label = { Text(text = stringResource(R.string.end_date)) },
                onClick = { viewModel.openDatePicker = true }
            )

            EditMediaProgressRow(
                label = stringResource(if (viewModel.mediaType == MediaType.ANIME) R.string.total_rewatches
                else R.string.total_rereads),
                progress = viewModel.repeatCount,
                modifier = Modifier.padding(16.dp),
                totalProgress = null,
                onValueChange = { viewModel.onChangeRepeatCount(it.toIntOrNull()) },
                onMinusClick = { viewModel.onChangeRepeatCount(viewModel.repeatCount - 1) },
                onPlusClick = { viewModel.onChangeRepeatCount(viewModel.repeatCount + 1) }
            )

            Button(
                onClick = { /*TODO*/ },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(text = stringResource(R.string.delete))
            }
        }//:Column
    }//:Sheet

    if (viewModel.openDatePicker) {
        EditMediaDatePicker(viewModel = viewModel)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMediaDatePicker(
    viewModel: MediaDetailsViewModel
) {
    val datePickerState = rememberDatePickerState()
    val dateConfirmEnabled by remember {
        derivedStateOf { datePickerState.selectedDateMillis != null }
    }

    DatePickerDialog(
        onDismissRequest = { viewModel.openDatePicker = false },
        confirmButton = {
            TextButton(
                onClick = {
                    viewModel.openDatePicker = false

                },
                enabled = dateConfirmEnabled
            ) {
                Text(text = stringResource(R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = { viewModel.openDatePicker = false }) {
                Text(text = stringResource(R.string.cancel))
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

@Composable
fun EditMediaProgressRow(
    label: String,
    progress: Int,
    modifier: Modifier,
    totalProgress: Int?,
    onValueChange: (String) -> Unit,
    onMinusClick: () -> Unit,
    onPlusClick: () -> Unit,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedButton(
            onClick = onMinusClick,
            modifier = Modifier.weight(1f)
        ) {
            Text(text = stringResource(R.string.minus_one))
        }
        OutlinedTextField(
            value = progress.toString(),
            onValueChange = onValueChange,
            modifier = Modifier
                .weight(2f)
                .padding(horizontal = 16.dp),
            label = { Text(text = label) },
            suffix = {
                totalProgress?.let { Text(text = "/$it") }
            }
        )

        OutlinedButton(
            onClick = onPlusClick,
            modifier = Modifier.weight(1f)
        ) {
            Text(text = stringResource(R.string.plus_one))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusItem(
    status: ListStatus,
    selectedStatus: ListStatus,
    onClick: () -> Unit
) {
    val tooltipState = remember { PlainTooltipState() }
    val scope = rememberCoroutineScope()

    PlainTooltipBox(
        tooltip = { Text(status.localized()) },
        tooltipState = tooltipState
    ) {
        FilledIconToggleButton(
            checked = status == selectedStatus,
            onCheckedChange = {
                scope.launch { tooltipState.show() }
                onClick()
            }
        ) {
            Icon(painter = painterResource(status.icon()), contentDescription = "")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun EditMediaSheetPreview() {
    MoeListTheme {
        EditMediaSheet(
            coroutineScope = rememberCoroutineScope(),
            sheetState = rememberModalBottomSheetState(),
            viewModel = MediaDetailsViewModel(MediaType.ANIME)
        )
    }
}