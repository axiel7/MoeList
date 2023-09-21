package com.axiel7.moelist.uicompose.userlist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.axiel7.moelist.data.model.media.ListStatus
import com.axiel7.moelist.data.model.media.ListStatus.Companion.listStatusValues
import com.axiel7.moelist.data.model.media.ListType
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.uicompose.composables.LoadingDialog
import com.axiel7.moelist.uicompose.editmedia.EditMediaSheet
import com.axiel7.moelist.uicompose.theme.MoeListTheme
import com.axiel7.moelist.uicompose.userlist.composables.MediaListSortDialog
import com.axiel7.moelist.uicompose.userlist.composables.SetAsCompletedDialog
import com.axiel7.moelist.utils.ContextExtensions.showToast
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserMediaListWithFabView(
    mediaType: MediaType,
    isCompactScreen: Boolean,
    navigateToMediaDetails: (MediaType, Int) -> Unit,
    topBarHeightPx: Float,
    topBarOffsetY: Animatable<Float, AnimationVector1D>,
    padding: PaddingValues,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val viewModel = viewModel(key = mediaType.toString()) {
        UserMediaListViewModel(mediaType)
    }
    val statusSheetState = rememberModalBottomSheetState()
    val editSheetState = rememberModalBottomSheetState()
    var isFabVisible by remember { mutableStateOf(true) }
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (available.y < -1) isFabVisible = false
                else if (available.y > 1) isFabVisible = true
                return Offset.Zero
            }
        }
    }
    val listType by remember {
        derivedStateOf { ListType(viewModel.listStatus, mediaType) }
    }
    val bottomBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    if (statusSheetState.isVisible) {
        ListStatusSheet(
            mediaType = mediaType,
            selectedStatus = viewModel.listStatus,
            sheetState = statusSheetState,
            bottomPadding = bottomBarPadding,
            onStatusChanged = viewModel::onStatusChanged,
            onDismiss = {
                scope.launch { statusSheetState.hide() }
            }
        )
    }

    if (viewModel.openSortDialog) {
        MediaListSortDialog(viewModel = viewModel)
    }

    if (viewModel.openSetAtCompletedDialog) {
        SetAsCompletedDialog(viewModel = viewModel)
    }

    if (viewModel.isLoadingRandom) {
        LoadingDialog()
    }

    if (editSheetState.isVisible) {
        EditMediaSheet(
            coroutineScope = scope,
            sheetState = editSheetState,
            mediaViewModel = viewModel,
            bottomPadding = bottomBarPadding
        )
    }

    LaunchedEffect(viewModel.randomId) {
        viewModel.randomId?.let { id ->
            navigateToMediaDetails(viewModel.mediaType, id)
            viewModel.randomId = null
        }
    }

    LaunchedEffect(viewModel.message) {
        if (viewModel.showMessage) {
            context.showToast(viewModel.message)
            viewModel.showMessage = false
        }
    }

    Scaffold(
        modifier = Modifier
            .padding(bottom = padding.calculateBottomPadding()),
        floatingActionButton = {
            AnimatedVisibility(
                visible = isFabVisible,
                modifier = Modifier.sizeIn(minWidth = 80.dp, minHeight = 56.dp),
                enter = slideInVertically(initialOffsetY = { it * 2 }),
                exit = slideOutVertically(targetOffsetY = { it * 2 }),
            ) {
                ExtendedFloatingActionButton(
                    onClick = { scope.launch { statusSheetState.show() } }
                ) {
                    Icon(
                        painter = painterResource(viewModel.listStatus.icon),
                        contentDescription = "status",
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(text = viewModel.listStatus.localized())
                }
            }
        },
        contentWindowInsets = WindowInsets.systemBars
            .only(WindowInsetsSides.Horizontal)
    ) { childPadding ->
        UserMediaListView(
            viewModel = viewModel,
            listType = listType,
            isCompactScreen = isCompactScreen,
            modifier = Modifier.padding(childPadding),
            nestedScrollConnection = nestedScrollConnection,
            navigateToMediaDetails = navigateToMediaDetails,
            topBarHeightPx = topBarHeightPx,
            topBarOffsetY = topBarOffsetY,
            contentPadding = padding,
            onShowEditSheet = { item ->
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                scope.launch {
                    viewModel.onItemSelected(item)
                    editSheetState.show()
                }
            },
        )
    }//:Scaffold
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListStatusSheet(
    mediaType: MediaType,
    selectedStatus: ListStatus,
    sheetState: SheetState,
    bottomPadding: Dp,
    onStatusChanged: (ListStatus) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        windowInsets = WindowInsets(0, 0, 0, 0)
    ) {
        Column(
            modifier = Modifier.padding(bottom = 8.dp + bottomPadding)
        ) {
            listStatusValues(mediaType).forEach {
                val isSelected = selectedStatus == it
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onStatusChanged(it)
                            onDismiss()
                        }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(it.icon),
                        contentDescription = "check",
                        tint = if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = it.localized(),
                        modifier = Modifier.padding(start = 8.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun UserMediaListHostPreview() {
    MoeListTheme {
        UserMediaListWithFabView(
            mediaType = MediaType.ANIME,
            isCompactScreen = true,
            navigateToMediaDetails = { _, _ -> },
            topBarHeightPx = 0f,
            topBarOffsetY = remember { Animatable(0f) },
            padding = PaddingValues(),
        )
    }
}