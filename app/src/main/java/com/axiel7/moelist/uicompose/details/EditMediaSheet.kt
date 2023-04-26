package com.axiel7.moelist.uicompose.details

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.manga.MangaNode
import com.axiel7.moelist.data.model.media.*
import com.axiel7.moelist.uicompose.base.BaseMediaViewModel
import com.axiel7.moelist.uicompose.composables.ClickableOutlinedTextField
import com.axiel7.moelist.uicompose.theme.MoeListTheme
import com.axiel7.moelist.utils.ContextExtensions.showToast
import com.axiel7.moelist.utils.DateUtils
import com.axiel7.moelist.utils.DateUtils.toEpochMillis
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMediaSheet(
    coroutineScope: CoroutineScope,
    sheetState: SheetState,
    mediaViewModel: BaseMediaViewModel
) {
    val context = LocalContext.current
    val statusValues = if (mediaViewModel.mediaType == MediaType.ANIME) listStatusAnimeValues else listStatusMangaValues
    val datePickerState = rememberDatePickerState()
    val viewModel: EditMediaViewModel = viewModel {
        EditMediaViewModel(
            mediaType = mediaViewModel.mediaType,
            mediaInfo = mediaViewModel.mediaInfo
        )
    }
    val isNewEntry by remember {
        derivedStateOf { mediaViewModel.myListStatus == null }
    }

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

                if (viewModel.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }

                Button(onClick = { viewModel.updateListItem() }) {
                    Text(text = stringResource(if (isNewEntry) R.string.add else R.string.apply))
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
                        selectedStatus = viewModel.status,
                        onClick = {
                            viewModel.status = status
                            if (isNewEntry && status.isCurrent()) {
                                viewModel.startDate = LocalDate.now()
                            }
                        }
                    )
                }
            }

            EditMediaProgressRow(
                label = if (viewModel.mediaType == MediaType.ANIME) stringResource(R.string.episodes)
                else stringResource(R.string.chapters),
                progress = viewModel.progress,
                modifier = Modifier.padding(horizontal = 16.dp),
                totalProgress = viewModel.mediaInfo?.totalDuration(),
                onValueChange = { viewModel.onChangeProgress(it.toIntOrNull()) },
                onMinusClick = { viewModel.onChangeProgress(viewModel.progress - 1) },
                onPlusClick = { viewModel.onChangeProgress(viewModel.progress + 1) }
            )

            if (viewModel.mediaType == MediaType.MANGA) {
                EditMediaProgressRow(
                    label = stringResource(R.string.volumes),
                    progress = viewModel.volumeProgress,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    totalProgress = (viewModel.mediaInfo as MangaNode).numVolumes,
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
                value = viewModel.startDate?.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)) ?: "",
                onValueChange = {  },
                label = { Text(text = stringResource(R.string.start_date)) },
                onClick = {
                    datePickerState.setSelection(viewModel.startDate?.toEpochMillis())
                    viewModel.selectedDateType = 1
                    viewModel.openDatePicker = true
                }
            )
            ClickableOutlinedTextField(
                value = viewModel.endDate?.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)) ?: "",
                onValueChange = {  },
                modifier = Modifier.padding(vertical = 8.dp),
                label = { Text(text = stringResource(R.string.end_date)) },
                onClick = {
                    datePickerState.setSelection(viewModel.endDate?.toEpochMillis())
                    viewModel.selectedDateType = 2
                    viewModel.openDatePicker = true
                }
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
                onClick = { viewModel.openDeleteDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 16.dp),
                enabled = !isNewEntry,
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
        EditMediaDatePicker(
            viewModel = viewModel,
            datePickerState = datePickerState,
            onDateSelected = {
                when (viewModel.selectedDateType) {
                    1 -> { viewModel.startDate = DateUtils.getLocalDateFromMillis(it) }
                    2 -> { viewModel.endDate = DateUtils.getLocalDateFromMillis(it) }
                }
            }
        )
    }

    if (viewModel.openDeleteDialog) {
        DeleteMediaEntryDialog(viewModel = viewModel)
    }

    if (viewModel.showMessage) {
        context.showToast(viewModel.message)
        viewModel.showMessage = false
    }

    LaunchedEffect(mediaViewModel.mediaInfo) {
        viewModel.mediaInfo = mediaViewModel.mediaInfo
        mediaViewModel.myListStatus?.let {
            viewModel.setEditVariables(it)
        }
    }

    LaunchedEffect(viewModel.updateSuccess) {
        if (viewModel.updateSuccess) {
            mediaViewModel.myListStatus = viewModel.myListStatus
            viewModel.updateSuccess = false
            coroutineScope.launch { sheetState.hide() }
        }
    }
}

@Composable
fun DeleteMediaEntryDialog(viewModel: EditMediaViewModel) {
    AlertDialog(
        onDismissRequest = { viewModel.openDeleteDialog = false },
        confirmButton = {
            TextButton(
                onClick = {
                    viewModel.deleteEntry()
                    viewModel.openDeleteDialog = false
                }
            ) {
                Text(text = stringResource(R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = { viewModel.openDeleteDialog = false }) {
                Text(text = stringResource(R.string.cancel))
            }
        },
        title = { Text(text = stringResource(R.string.delete)) },
        text = { Text(text = stringResource(R.string.delete_confirmation)) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMediaDatePicker(
    viewModel: EditMediaViewModel,
    datePickerState: DatePickerState,
    onDateSelected: (Long) -> Unit
) {
    val dateConfirmEnabled by remember {
        derivedStateOf { datePickerState.selectedDateMillis != null }
    }

    DatePickerDialog(
        onDismissRequest = { viewModel.openDatePicker = false },
        confirmButton = {
            TextButton(
                onClick = {
                    viewModel.openDatePicker = false
                    onDateSelected(datePickerState.selectedDateMillis!!)
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
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
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
    selectedStatus: ListStatus?,
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
            mediaViewModel = viewModel { MediaDetailsViewModel(MediaType.ANIME) }
        )
    }
}