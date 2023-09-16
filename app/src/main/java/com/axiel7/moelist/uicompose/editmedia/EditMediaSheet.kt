package com.axiel7.moelist.uicompose.editmedia

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.manga.MangaNode
import com.axiel7.moelist.data.model.media.ListStatus.Companion.listStatusValues
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.data.model.media.priorityLocalized
import com.axiel7.moelist.data.model.media.repeatValueLocalized
import com.axiel7.moelist.data.model.media.scoreText
import com.axiel7.moelist.uicompose.base.BaseMediaViewModel
import com.axiel7.moelist.uicompose.composables.ClickableOutlinedTextField
import com.axiel7.moelist.uicompose.composables.SelectableIconToggleButton
import com.axiel7.moelist.uicompose.composables.TextCheckBox
import com.axiel7.moelist.uicompose.details.MediaDetailsViewModel
import com.axiel7.moelist.uicompose.editmedia.composables.DeleteMediaEntryDialog
import com.axiel7.moelist.uicompose.editmedia.composables.EditMediaDatePicker
import com.axiel7.moelist.uicompose.editmedia.composables.EditMediaProgressRow
import com.axiel7.moelist.uicompose.theme.MoeListTheme
import com.axiel7.moelist.utils.ContextExtensions.showToast
import com.axiel7.moelist.utils.DateUtils
import com.axiel7.moelist.utils.DateUtils.toEpochMillis
import com.axiel7.moelist.utils.DateUtils.toLocalized
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.ZoneOffset
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMediaSheet(
    coroutineScope: CoroutineScope,
    sheetState: SheetState,
    mediaViewModel: BaseMediaViewModel,
    bottomPadding: Dp = 0.dp
) {
    val context = LocalContext.current
    val statusValues = listStatusValues(mediaViewModel.mediaType)
    val datePickerState = rememberDatePickerState()
    val viewModel = viewModel {
        EditMediaViewModel(
            mediaType = mediaViewModel.mediaType,
            mediaInfo = mediaViewModel.mediaInfo
        )
    }
    val isNewEntry by remember {
        derivedStateOf { mediaViewModel.myListStatus == null }
    }

    if (viewModel.openDatePicker) {
        EditMediaDatePicker(
            viewModel = viewModel,
            datePickerState = datePickerState,
            onDateSelected = {
                when (viewModel.selectedDateType) {
                    1 -> {
                        viewModel.startDate = DateUtils.getLocalDateFromMillis(it)
                    }

                    2 -> {
                        viewModel.finishDate = DateUtils.getLocalDateFromMillis(it)
                    }
                }
            }
        )
    }

    if (viewModel.openDeleteDialog) {
        DeleteMediaEntryDialog(viewModel = viewModel)
    }

    LaunchedEffect(viewModel.message) {
        if (viewModel.showMessage) {
            context.showToast(viewModel.message)
            viewModel.showMessage = false
        }
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

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = { coroutineScope.launch { sheetState.hide() } },
        windowInsets = WindowInsets(0, 0, 0, 0)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp + bottomPadding)
                .imePadding(),
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
                    SelectableIconToggleButton(
                        icon = status.icon,
                        tooltipText = status.localized(),
                        value = status,
                        selectedValue = viewModel.status,
                        onClick = {
                            viewModel.onChangeStatus(status, isNewEntry)
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
                onMinusClick = { viewModel.onChangeProgress(viewModel.progress?.minus(1)) },
                onPlusClick = { viewModel.onChangeProgress(viewModel.progress?.plus(1)) }
            )

            if (viewModel.mediaType == MediaType.MANGA) {
                EditMediaProgressRow(
                    label = stringResource(R.string.volumes),
                    progress = viewModel.volumeProgress,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp),
                    totalProgress = (viewModel.mediaInfo as? MangaNode)?.numVolumes,
                    onValueChange = { viewModel.onChangeVolumeProgress(it.toIntOrNull()) },
                    onMinusClick = {
                        viewModel.onChangeVolumeProgress(
                            viewModel.volumeProgress?.minus(
                                1
                            )
                        )
                    },
                    onPlusClick = {
                        viewModel.onChangeVolumeProgress(
                            viewModel.volumeProgress?.plus(
                                1
                            )
                        )
                    }
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
                onValueChange = { viewModel.score = it.roundToInt() },
                modifier = Modifier.padding(horizontal = 16.dp),
                valueRange = 0f..10f,
                steps = 9
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            ClickableOutlinedTextField(
                value = viewModel.startDate.toLocalized(),
                onValueChange = { },
                label = { Text(text = stringResource(R.string.start_date)) },
                trailingIcon = {
                    if (viewModel.startDate != null) {
                        IconButton(onClick = { viewModel.startDate = null }) {
                            Icon(
                                painter = painterResource(R.drawable.outline_cancel_24),
                                contentDescription = stringResource(R.string.delete)
                            )
                        }
                    }
                },
                onClick = {
                    datePickerState.selectedDateMillis = viewModel.startDate?.toEpochMillis(
                        offset = ZoneOffset.UTC
                    )
                    viewModel.selectedDateType = 1
                    viewModel.openDatePicker = true
                }
            )
            ClickableOutlinedTextField(
                value = viewModel.finishDate.toLocalized(),
                onValueChange = { },
                modifier = Modifier.padding(vertical = 8.dp),
                label = { Text(text = stringResource(R.string.end_date)) },
                trailingIcon = {
                    if (viewModel.finishDate != null) {
                        IconButton(onClick = { viewModel.finishDate = null }) {
                            Icon(
                                painter = painterResource(R.drawable.outline_cancel_24),
                                contentDescription = stringResource(R.string.delete)
                            )
                        }
                    }
                },
                onClick = {
                    datePickerState.selectedDateMillis = viewModel.finishDate?.toEpochMillis(
                        offset = ZoneOffset.UTC
                    )
                    viewModel.selectedDateType = 2
                    viewModel.openDatePicker = true
                }
            )

            OutlinedTextField(
                value = viewModel.tags ?: "",
                onValueChange = {
                    viewModel.tags = it
                },
                modifier = Modifier.padding(16.dp),
                label = { Text(text = stringResource(R.string.tags)) }
            )

            Text(
                text = stringResource(R.string.priority_value).format(viewModel.priority.priorityLocalized()),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp),
                fontWeight = FontWeight.Bold
            )
            Slider(
                value = viewModel.priority.toFloat(),
                onValueChange = { viewModel.priority = it.roundToInt() },
                modifier = Modifier.padding(horizontal = 16.dp),
                valueRange = 0f..2f,
                steps = 1
            )

            TextCheckBox(
                text = stringResource(
                    if (viewModel.mediaType == MediaType.ANIME) R.string.rewatching
                    else R.string.rereading
                ),
                checked = viewModel.isRepeating,
                onCheckedChange = {
                    viewModel.isRepeating = it
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            EditMediaProgressRow(
                label = stringResource(
                    if (viewModel.mediaType == MediaType.ANIME) R.string.total_rewatches
                    else R.string.total_rereads
                ),
                progress = viewModel.repeatCount,
                modifier = Modifier.padding(16.dp),
                totalProgress = null,
                onValueChange = { viewModel.onChangeRepeatCount(it.toIntOrNull()) },
                onMinusClick = { viewModel.onChangeRepeatCount(viewModel.repeatCount - 1) },
                onPlusClick = { viewModel.onChangeRepeatCount(viewModel.repeatCount + 1) }
            )

            Text(
                text = stringResource(
                    if (viewModel.mediaType == MediaType.ANIME) R.string.rewatch_value
                    else R.string.reread_value
                ).format(viewModel.repeatValue.repeatValueLocalized()),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp),
                fontWeight = FontWeight.Bold
            )
            Slider(
                value = viewModel.repeatValue.toFloat(),
                onValueChange = { viewModel.repeatValue = it.roundToInt() },
                modifier = Modifier.padding(horizontal = 16.dp),
                valueRange = 0f..5f,
                steps = 4
            )

            OutlinedTextField(
                value = viewModel.comments ?: "",
                onValueChange = {
                    viewModel.comments = it
                },
                modifier = Modifier.padding(16.dp),
                label = { Text(text = stringResource(R.string.notes)) },
                minLines = 2
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