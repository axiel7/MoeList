package com.axiel7.moelist.ui.editmedia

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.media.BaseMediaNode
import com.axiel7.moelist.data.model.media.BaseMyListStatus
import com.axiel7.moelist.data.model.media.ListStatus.Companion.listStatusValues
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.data.model.media.priorityLocalized
import com.axiel7.moelist.data.model.media.repeatValueLocalized
import com.axiel7.moelist.data.model.media.scoreText
import com.axiel7.moelist.ui.composables.SelectableIconToggleButton
import com.axiel7.moelist.ui.composables.preferences.PlainPreferenceView
import com.axiel7.moelist.ui.composables.preferences.SwitchPreferenceView
import com.axiel7.moelist.ui.editmedia.composables.DeleteMediaEntryDialog
import com.axiel7.moelist.ui.editmedia.composables.EditMediaDateField
import com.axiel7.moelist.ui.editmedia.composables.EditMediaDatePicker
import com.axiel7.moelist.ui.editmedia.composables.EditMediaProgressRow
import com.axiel7.moelist.ui.editmedia.composables.EditMediaValueRow
import com.axiel7.moelist.ui.theme.MoeListTheme
import com.axiel7.moelist.utils.ContextExtensions.showToast
import com.axiel7.moelist.utils.DateUtils
import com.axiel7.moelist.utils.DateUtils.toEpochMillis
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

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
    val viewModel: EditMediaViewModel = koinViewModel { parametersOf(mediaInfo.mediaType) }
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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
    val haptic = LocalHapticFeedback.current
    val statusValues = remember(uiState.mediaType) {
        listStatusValues(uiState.mediaType)
    }
    val datePickerState = rememberDatePickerState()
    val isKeyboardVisible = WindowInsets.isImeVisible
    val keyboardController = LocalSoftwareKeyboardController.current

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
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
        properties = ModalBottomSheetProperties(shouldDismissOnBackPress = false),
    ) {
        BackHandler(enabled = true) {
            if (isKeyboardVisible) keyboardController?.hide()
            else onDismissed()
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp + bottomPadding)
                .imePadding(),
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

            // Status
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
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            event?.onChangeStatus(status)
                        }
                    )
                }
            }

            // Progress
            EditMediaProgressRow(
                label = if (uiState.mediaType == MediaType.ANIME) stringResource(R.string.episodes)
                else stringResource(R.string.chapters),
                icon = if (uiState.mediaType == MediaType.ANIME) R.drawable.play_circle_outline_24
                else R.drawable.ic_outline_book_24,
                progress = uiState.progress,
                modifier = Modifier.padding(start = 0.dp, end = 16.dp),
                totalProgress = uiState.mediaInfo?.totalDuration(),
                onValueChange = { event?.onChangeProgress(it.toIntOrNull()) },
                minValue = 0,
                maxValue = uiState.mediaInfo?.totalDuration(),
                onMinusClick = { event?.onChangeProgress((uiState.progress ?: 0) - 1) },
                onPlusClick = { event?.onChangeProgress((uiState.progress ?: 0) + 1) }
            )

            if (uiState.mediaType == MediaType.MANGA) {
                EditMediaProgressRow(
                    label = stringResource(R.string.volumes),
                    icon = R.drawable.round_bookmark_24,
                    progress = uiState.volumeProgress,
                    modifier = Modifier.padding(end = 16.dp, top = 8.dp),
                    totalProgress = uiState.mediaInfo?.totalVolumes(),
                    onValueChange = { event?.onChangeVolumeProgress(it.toIntOrNull()) },
                    minValue = 0,
                    maxValue = uiState.mediaInfo?.totalVolumes(),
                    onMinusClick = {
                        event?.onChangeVolumeProgress((uiState.volumeProgress ?: 0) - 1)
                    },
                    onPlusClick = {
                        event?.onChangeVolumeProgress((uiState.volumeProgress ?: 0) + 1)
                    }
                )
            }

            // Score
            EditMediaProgressRow(
                label = uiState.score.scoreText(),
                icon = R.drawable.ic_round_details_star_24,
                progress = uiState.score,
                modifier = Modifier.padding(start = 0.dp, top = 8.dp, end = 16.dp),
                totalProgress = 10,
                onValueChange = { value ->
                    value.toIntOrNull()?.let { event?.onChangeScore(it) }
                },
                minValue = 0,
                maxValue = 10,
                onMinusClick = {
                    event?.onChangeScore(uiState.score.minus(1))
                },
                onPlusClick = {
                    event?.onChangeScore(uiState.score.plus(1))
                }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            // Dates
            EditMediaDateField(
                date = uiState.startDate,
                label = stringResource(R.string.start_date),
                icon = R.drawable.round_calendar_today_24,
                removeDate = { event?.onChangeStartDate(null) },
                onClick = {
                    datePickerState.selectedDateMillis = uiState.startDate?.toEpochMillis()
                    event?.openStartDatePicker()
                }
            )
            EditMediaDateField(
                date = uiState.finishDate,
                label = stringResource(R.string.end_date),
                icon = R.drawable.round_event_available_24,
                removeDate = { event?.onChangeFinishDate(null) },
                onClick = {
                    datePickerState.selectedDateMillis = uiState.finishDate?.toEpochMillis()
                    event?.openFinishDatePicker()
                }
            )

            // Tags
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.round_label_24),
                    contentDescription = stringResource(R.string.tags),
                    modifier = Modifier.padding(start = 16.dp)
                )
                OutlinedTextField(
                    value = uiState.tags.orEmpty(),
                    onValueChange = { event?.onChangeTags(it) },
                    placeholder = {
                        Text(text = stringResource(R.string.tags))
                    },
                    singleLine = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent
                    )
                )
            }

            EditMediaValueRow(
                label = stringResource(R.string.priority_value, uiState.priority.priorityLocalized()),
                icon = R.drawable.round_priority_high_24,
                modifier = Modifier.padding(start = 0.dp, end = 16.dp, bottom = 8.dp),
                minusEnabled = uiState.priority > 0,
                onMinusClick = {
                    event?.onChangePriority(uiState.priority.minus(1))
                },
                plusEnabled = uiState.priority < 2,
                onPlusClick = {
                    event?.onChangePriority(uiState.priority.plus(1))
                }
            )

            SwitchPreferenceView(
                title = stringResource(
                    if (uiState.mediaType == MediaType.ANIME) R.string.rewatching
                    else R.string.rereading
                ),
                value = uiState.isRepeating,
                icon = R.drawable.round_repeat_24,
                iconTint = LocalContentColor.current,
                iconPadding = PaddingValues(
                    start = 16.dp,
                    top = 16.dp,
                    bottom = 16.dp
                ),
                onValueChange = { event?.onChangeIsRepeating(it) },
            )

            EditMediaProgressRow(
                label = stringResource(
                    if (uiState.mediaType == MediaType.ANIME) R.string.total_rewatches
                    else R.string.total_rereads
                ),
                icon = R.drawable.round_repeat_one_24,
                progress = uiState.repeatCount,
                modifier = Modifier.padding(start = 0.dp, top = 8.dp, end = 16.dp),
                totalProgress = null,
                onValueChange = { event?.onChangeRepeatCount(it.toIntOrNull()) },
                minValue = 0,
                onMinusClick = { event?.onChangeRepeatCount(uiState.repeatCount?.minus(1)) },
                onPlusClick = { event?.onChangeRepeatCount(uiState.repeatCount?.plus(1)) }
            )

            EditMediaValueRow(
                label = stringResource(
                    id = if (uiState.mediaType == MediaType.ANIME) R.string.rewatch_value
                    else R.string.reread_value,
                    uiState.repeatValue.repeatValueLocalized()
                ),
                icon = R.drawable.round_event_repeat_24,
                modifier = Modifier.padding(start = 0.dp, top = 8.dp, end = 16.dp),
                minusEnabled = uiState.repeatValue > 0,
                onMinusClick = {
                    event?.onChangeRepeatValue(uiState.repeatValue.minus(1))
                },
                plusEnabled = uiState.repeatValue < 5,
                onPlusClick = {
                    event?.onChangeRepeatValue(uiState.repeatValue.plus(1))
                }
            )

            // Notes
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.round_notes_24),
                    contentDescription = stringResource(R.string.tags),
                    modifier = Modifier.padding(start = 16.dp)
                )
                OutlinedTextField(
                    value = uiState.comments.orEmpty(),
                    onValueChange = { event?.onChangeComments(it) },
                    placeholder = {
                        Text(text = stringResource(R.string.notes))
                    },
                    singleLine = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent
                    )
                )
            }

            // Delete
            PlainPreferenceView(
                title = stringResource(R.string.delete),
                titleTint = MaterialTheme.colorScheme.error,
                icon = R.drawable.delete_outline_24,
                iconTint = MaterialTheme.colorScheme.error,
                iconPadding = PaddingValues(
                    start = 16.dp,
                    top = 8.dp,
                    bottom = 8.dp
                ),
                enabled = !uiState.isNewEntry,
                onClick = { event?.toggleDeleteDialog(true) }
            )
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
                sheetState = SheetState(
                    skipPartiallyExpanded = true,
                    density = LocalDensity.current,
                    initialValue = SheetValue.Expanded
                ),
                onEdited = { _, _ -> },
                onDismissed = {},
            )
        }
    }
}