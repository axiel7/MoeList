package com.axiel7.moelist.ui.editmedia

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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.manga.MangaNode
import com.axiel7.moelist.data.model.media.BaseMediaNode
import com.axiel7.moelist.data.model.media.BaseMyListStatus
import com.axiel7.moelist.data.model.media.ListStatus.Companion.listStatusValues
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.data.model.media.priorityLocalized
import com.axiel7.moelist.data.model.media.repeatValueLocalized
import com.axiel7.moelist.data.model.media.scoreText
import com.axiel7.moelist.ui.composables.ClickableOutlinedTextField
import com.axiel7.moelist.ui.composables.SelectableIconToggleButton
import com.axiel7.moelist.ui.composables.TextCheckBox
import com.axiel7.moelist.ui.editmedia.composables.DeleteMediaEntryDialog
import com.axiel7.moelist.ui.editmedia.composables.EditMediaDatePicker
import com.axiel7.moelist.ui.editmedia.composables.EditMediaProgressRow
import com.axiel7.moelist.ui.theme.MoeListTheme
import com.axiel7.moelist.utils.ContextExtensions.showToast
import com.axiel7.moelist.utils.DateUtils
import com.axiel7.moelist.utils.DateUtils.toEpochMillis
import com.axiel7.moelist.utils.DateUtils.toLocalized
import org.koin.androidx.compose.koinViewModel
import java.time.ZoneOffset
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMediaSheet(
    sheetState: SheetState,
    mediaInfo: BaseMediaNode,
    myListStatus: BaseMyListStatus?,
    bottomPadding: Dp = 0.dp,
    onEdited: (BaseMyListStatus?, removed: Boolean) -> Unit,
    onDismissed: () -> Unit
) {
    val viewModel: EditMediaViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(mediaInfo) {
        viewModel.setMediaInfo(mediaInfo)
    }
    LaunchedEffect(myListStatus) {
        if (myListStatus != null)
            viewModel.setEditVariables(myListStatus)
    }

    EditMediaSheetContent(
        uiState = uiState,
        event = viewModel,
        sheetState = sheetState,
        bottomPadding = bottomPadding,
        onEdited = onEdited,
        onDismissed = onDismissed,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditMediaSheetContent(
    uiState: EditMediaUiState,
    event: EditMediaEvent?,
    sheetState: SheetState,
    bottomPadding: Dp = 0.dp,
    onEdited: (BaseMyListStatus?, removed: Boolean) -> Unit,
    onDismissed: () -> Unit
) {
    val context = LocalContext.current
    val statusValues = remember(uiState.mediaType) {
        listStatusValues(uiState.mediaType)
    }
    val datePickerState = rememberDatePickerState()

    if (uiState.openStartDatePicker || uiState.openFinishDatePicker) {
        EditMediaDatePicker(
            datePickerState = datePickerState,
            onDateSelected = {
                if (uiState.openStartDatePicker) {
                    event?.onChangeStartDate(DateUtils.getLocalDateFromMillis(it))
                } else {
                    event?.onChangeFinishDate(DateUtils.getLocalDateFromMillis(it))
                }
            },
            onDismiss = {
                event?.closeDatePickers()
            }
        )
    }

    if (uiState.openDeleteDialog) {
        DeleteMediaEntryDialog(
            onConfirm = { event?.deleteEntry() },
            onDismiss = { event?.toggleDeleteDialog(false) }
        )
    }

    LaunchedEffect(uiState.message) {
        if (uiState.message != null) {
            context.showToast(uiState.message)
            event?.onMessageDisplayed()
        }
    }

    LaunchedEffect(uiState.updateSuccess) {
        if (uiState.updateSuccess == true) {
            event?.onDismiss()
            onEdited(uiState.myListStatus, uiState.removed)
        }
    }

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = onDismissed,
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
                TextButton(onClick = onDismissed) {
                    Text(text = stringResource(R.string.cancel))
                }

                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }

                Button(onClick = { event?.updateListItem() }) {
                    Text(text = stringResource(if (uiState.isNewEntry) R.string.add else R.string.apply))
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
                        selectedValue = uiState.status,
                        onClick = { event?.onChangeStatus(status) }
                    )
                }
            }

            EditMediaProgressRow(
                label = if (uiState.mediaType == MediaType.ANIME) stringResource(R.string.episodes)
                else stringResource(R.string.chapters),
                progress = uiState.progress,
                modifier = Modifier.padding(horizontal = 16.dp),
                totalProgress = uiState.mediaInfo?.totalDuration(),
                onValueChange = { event?.onChangeProgress(it.toIntOrNull()) },
                onMinusClick = { event?.onChangeProgress(uiState.progress?.minus(1)) },
                onPlusClick = { event?.onChangeProgress(uiState.progress?.plus(1)) }
            )

            if (uiState.mediaType == MediaType.MANGA) {
                EditMediaProgressRow(
                    label = stringResource(R.string.volumes),
                    progress = uiState.volumeProgress,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp),
                    totalProgress = (uiState.mediaInfo as? MangaNode)?.numVolumes,
                    onValueChange = { event?.onChangeVolumeProgress(it.toIntOrNull()) },
                    onMinusClick = {
                        event?.onChangeVolumeProgress(uiState.volumeProgress?.minus(1))
                    },
                    onPlusClick = {
                        event?.onChangeVolumeProgress(uiState.volumeProgress?.plus(1))
                    }
                )
            }

            Text(
                text = stringResource(R.string.score_value).format(uiState.score.scoreText()),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp),
                fontWeight = FontWeight.Bold
            )
            Slider(
                value = uiState.score.toFloat(),
                onValueChange = { event?.onChangeScore(it.roundToInt()) },
                modifier = Modifier.padding(horizontal = 16.dp),
                valueRange = 0f..10f,
                steps = 9
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            ClickableOutlinedTextField(
                value = uiState.startDate.toLocalized(),
                onValueChange = { },
                label = { Text(text = stringResource(R.string.start_date)) },
                trailingIcon = {
                    if (uiState.startDate != null) {
                        IconButton(onClick = { event?.onChangeStartDate(null) }) {
                            Icon(
                                painter = painterResource(R.drawable.outline_cancel_24),
                                contentDescription = stringResource(R.string.delete)
                            )
                        }
                    }
                },
                onClick = {
                    datePickerState.selectedDateMillis = uiState.startDate
                        ?.toEpochMillis(offset = ZoneOffset.UTC)
                    event?.openStartDatePicker()
                }
            )
            ClickableOutlinedTextField(
                value = uiState.finishDate.toLocalized(),
                onValueChange = { },
                modifier = Modifier.padding(vertical = 8.dp),
                label = { Text(text = stringResource(R.string.end_date)) },
                trailingIcon = {
                    if (uiState.finishDate != null) {
                        IconButton(onClick = { event?.onChangeFinishDate(null) }) {
                            Icon(
                                painter = painterResource(R.drawable.outline_cancel_24),
                                contentDescription = stringResource(R.string.delete)
                            )
                        }
                    }
                },
                onClick = {
                    datePickerState.selectedDateMillis = uiState.finishDate
                        ?.toEpochMillis(offset = ZoneOffset.UTC)
                    event?.openFinishDatePicker()
                }
            )

            OutlinedTextField(
                value = uiState.tags.orEmpty(),
                onValueChange = {
                    event?.onChangeTags(it)
                },
                modifier = Modifier.padding(16.dp),
                label = { Text(text = stringResource(R.string.tags)) }
            )

            Text(
                text = stringResource(R.string.priority_value).format(uiState.priority.priorityLocalized()),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp),
                fontWeight = FontWeight.Bold
            )
            Slider(
                value = uiState.priority.toFloat(),
                onValueChange = { event?.onChangePriority(it.roundToInt()) },
                modifier = Modifier.padding(horizontal = 16.dp),
                valueRange = 0f..2f,
                steps = 1
            )

            TextCheckBox(
                text = stringResource(
                    if (uiState.mediaType == MediaType.ANIME) R.string.rewatching
                    else R.string.rereading
                ),
                checked = uiState.isRepeating,
                onCheckedChange = {
                    event?.onChangeIsRepeating(it)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            EditMediaProgressRow(
                label = stringResource(
                    if (uiState.mediaType == MediaType.ANIME) R.string.total_rewatches
                    else R.string.total_rereads
                ),
                progress = uiState.repeatCount,
                modifier = Modifier.padding(16.dp),
                totalProgress = null,
                onValueChange = { event?.onChangeRepeatCount(it.toIntOrNull()) },
                onMinusClick = { event?.onChangeRepeatCount(uiState.repeatCount?.minus(1)) },
                onPlusClick = { event?.onChangeRepeatCount(uiState.repeatCount?.plus(1)) }
            )

            Text(
                text = stringResource(
                    id = if (uiState.mediaType == MediaType.ANIME) R.string.rewatch_value
                    else R.string.reread_value,
                    uiState.repeatValue.repeatValueLocalized()
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp),
                fontWeight = FontWeight.Bold
            )
            Slider(
                value = uiState.repeatValue.toFloat(),
                onValueChange = { event?.onChangeRepeatValue(it.roundToInt()) },
                modifier = Modifier.padding(horizontal = 16.dp),
                valueRange = 0f..5f,
                steps = 4
            )

            OutlinedTextField(
                value = uiState.comments.orEmpty(),
                onValueChange = {
                    event?.onChangeComments(it)
                },
                modifier = Modifier.padding(16.dp),
                label = { Text(text = stringResource(R.string.notes)) },
                minLines = 2
            )

            Button(
                onClick = { event?.toggleDeleteDialog(true) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 16.dp),
                enabled = !uiState.isNewEntry,
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
@Preview
@Composable
fun EditMediaSheetPreview() {
    MoeListTheme {
        Surface {
            EditMediaSheetContent(
                uiState = EditMediaUiState(mediaType = MediaType.ANIME),
                event = null,
                sheetState = rememberModalBottomSheetState(),
                onEdited = { _, _ -> },
                onDismissed = {},
            )
        }
    }
}