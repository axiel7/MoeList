package com.axiel7.moelist.ui.editmedia

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.media.BaseMediaNode
import com.axiel7.moelist.data.model.media.BaseMyListStatus
import com.axiel7.moelist.data.model.media.ListStatus
import com.axiel7.moelist.data.model.media.ListStatus.Companion.listStatusValues
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.ui.composables.media.MediaPoster
import com.axiel7.moelist.ui.composables.score.ScoreSlider
import com.axiel7.moelist.ui.editmedia.composables.DeleteMediaEntryDialog
import com.axiel7.moelist.ui.editmedia.composables.EditMediaDatePicker
import com.axiel7.moelist.ui.theme.MoeListTheme
import com.axiel7.moelist.utils.ContextExtensions.showToast
import com.axiel7.moelist.utils.DateUtils
import com.axiel7.moelist.utils.DateUtils.toEpochMillis
import com.axiel7.moelist.utils.NumExtensions.toStringPositiveValueOrUnknown
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
    var selectedTabIndex by remember { mutableIntStateOf(0) }

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
        properties = ModalBottomSheetProperties(shouldDismissOnBackPress = false),
        shape = BottomSheetDefaults.ExpandedShape,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentWindowInsets = { WindowInsets(0) },
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        BackHandler(enabled = true) {
            if (isKeyboardVisible) keyboardController?.hide()
            else onDismissed()
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .imePadding()
                .animateContentSize(),
        ) {
            // Top Actions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!uiState.isNewEntry) {
                    FilledTonalIconButton(
                        onClick = { event?.toggleDeleteDialog(true) },
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    ) {
                        Icon(
                            Icons.Rounded.Delete,
                            contentDescription = stringResource(R.string.delete)
                        )
                    }
                } else {
                    Spacer(Modifier.size(48.dp))
                }

                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Button(
                        onClick = { event?.updateListItem() },
                        shape = CircleShape,
                        contentPadding = ButtonDefaults.ButtonWithIconContentPadding
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = null,
                            modifier = Modifier.size(ButtonDefaults.IconSize)
                        )
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text(stringResource(if (uiState.isNewEntry) R.string.add else R.string.apply))
                    }
                }
            }

            // Refined Header (Sticky)
            ListItem(
                modifier = Modifier.padding(horizontal = 8.dp),
                headlineContent = {
                    Text(
                        text = uiState.mediaInfo?.userPreferredTitle() ?: stringResource(R.string.edit_entry),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                supportingContent = {
                    Row(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .animateContentSize(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        uiState.mediaInfo?.mediaFormat?.let { format ->
                            Surface(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = RoundedCornerShape(50)
                            ) {
                                Text(
                                    text = format.localized(),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Surface(
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            shape = RoundedCornerShape(50)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = uiState.status.icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp),
                                    tint = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                                Text(
                                    text = uiState.status.localized(),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                },
                leadingContent = {
                    MediaPoster(
                        url = uiState.mediaInfo?.mainPicture?.medium,
                        modifier = Modifier
                            .size(width = 48.dp, height = 72.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 24.dp + bottomPadding)
            ) {
                // Status Selection
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    statusValues.forEach { status ->
                        val isSelected = uiState.status == status
                        val containerColor by animateColorAsState(
                            targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f),
                            label = "statusContainer"
                        )
                        val contentColor by animateColorAsState(
                            targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                            label = "statusContent"
                        )

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .clip(CircleShape)
                                .background(containerColor)
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    event?.onChangeStatus(status)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = status.icon,
                                contentDescription = status.localized(),
                                tint = contentColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                // Progress Card
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column {
                        if (uiState.mediaType == MediaType.MANGA) {
                            PrimaryTabRow(
                                selectedTabIndex = selectedTabIndex,
                                containerColor = Color.Transparent,
                                divider = {}
                            ) {
                                Tab(
                                    selected = selectedTabIndex == 0,
                                    onClick = { selectedTabIndex = 0 },
                                    text = {
                                        Text(
                                            text = stringResource(R.string.chapters),
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                )
                                Tab(
                                    selected = selectedTabIndex == 1,
                                    onClick = { selectedTabIndex = 1 },
                                    text = {
                                        Text(
                                            text = stringResource(R.string.volumes),
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                )
                            }
                        } else {
                            ListItem(
                                headlineContent = {
                                    Text(
                                        text = stringResource(R.string.episodes),
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                },
                                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                            )
                        }

                        val isVolumeTab = uiState.mediaType == MediaType.MANGA && selectedTabIndex == 1
                        val current = if (isVolumeTab) uiState.volumeProgress ?: 0 else uiState.progress ?: 0
                        val total = if (isVolumeTab) uiState.mediaInfo?.totalVolumes() else uiState.mediaInfo?.totalDuration()

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FilledTonalIconButton(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    if (isVolumeTab) event?.onChangeVolumeProgress(current - 1)
                                    else event?.onChangeProgress(current - 1)
                                },
                                enabled = current > 0,
                                modifier = Modifier.size(44.dp),
                                shape = CircleShape
                            ) {
                                Icon(painterResource(R.drawable.round_remove_24), contentDescription = null, modifier = Modifier.size(20.dp))
                            }

                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = current.toString(),
                                    style = MaterialTheme.typography.displaySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = " / ${total.toStringPositiveValueOrUnknown()}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
                                )
                            }

                            FilledTonalIconButton(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    if (isVolumeTab) event?.onChangeVolumeProgress(current + 1)
                                    else event?.onChangeProgress(current + 1)
                                },
                                enabled = total == null || current < total,
                                modifier = Modifier.size(44.dp),
                                shape = CircleShape,
                                colors = IconButtonDefaults.filledTonalIconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            ) {
                                Icon(painterResource(R.drawable.ic_round_add_24), contentDescription = null, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }

                // Rating Card
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = stringResource(R.string.edit_rating),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        ScoreSlider(
                            score = uiState.score,
                            onValueChange = { event?.onChangeScore(it) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Date Selection
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    listOf(
                        stringResource(R.string.start_date) to uiState.startDate,
                        stringResource(R.string.end_date) to uiState.finishDate
                    ).forEachIndexed { index, pair ->
                        Surface(
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .clickable {
                                        if (index == 0) {
                                            datePickerState.selectedDateMillis = uiState.startDate?.toEpochMillis()
                                            event?.openStartDatePicker()
                                        } else {
                                            datePickerState.selectedDateMillis = uiState.finishDate?.toEpochMillis()
                                            event?.openFinishDatePicker()
                                        }
                                    }
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = pair.first,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = pair.second?.toString() ?: stringResource(R.string.unknown),
                                        style = MaterialTheme.typography.bodyLarge,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (pair.second != null) {
                                        IconButton(
                                            onClick = { if (index == 0) event?.onChangeStartDate(null) else event?.onChangeFinishDate(null) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Rounded.Clear, null, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun EditMediaSheetPreview() {
    MoeListTheme {
        Surface {
            EditMediaSheetContent(
                uiState = EditMediaUiState(
                    mediaType = MediaType.ANIME,
                    status = ListStatus.WATCHING,
                    progress = 12
                ),
                event = null,
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false),
                onEdited = { _, _ -> },
                onDismissed = {},
            )
        }
    }
}
