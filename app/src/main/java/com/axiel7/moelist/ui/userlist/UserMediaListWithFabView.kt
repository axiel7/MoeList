package com.axiel7.moelist.ui.userlist

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
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.media.ListStatus
import com.axiel7.moelist.data.model.media.ListStatus.Companion.listStatusValues
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.ui.base.navigation.NavActionManager
import com.axiel7.moelist.ui.composables.LoadingDialog
import com.axiel7.moelist.ui.editmedia.EditMediaSheet
import com.axiel7.moelist.ui.theme.MoeListTheme
import com.axiel7.moelist.ui.userlist.composables.MediaListSortDialog
import com.axiel7.moelist.ui.userlist.composables.SetScoreDialog
import com.axiel7.moelist.utils.ContextExtensions.showToast
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun UserMediaListWithFabView(
    mediaType: MediaType,
    isCompactScreen: Boolean,
    navActionManager: NavActionManager,
    topBarHeightPx: Float,
    topBarOffsetY: Animatable<Float, AnimationVector1D>,
    padding: PaddingValues,
) {
    val viewModel: UserMediaListViewModel = koinViewModel { parametersOf(mediaType) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    UserMediaListWithFabViewContent(
        uiState = uiState,
        event = viewModel,
        navActionManager = navActionManager,
        isCompactScreen = isCompactScreen,
        topBarHeightPx = topBarHeightPx,
        topBarOffsetY = topBarOffsetY,
        padding = padding,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UserMediaListWithFabViewContent(
    uiState: UserMediaListUiState,
    event: UserMediaListEvent?,
    navActionManager: NavActionManager,
    isCompactScreen: Boolean,
    topBarHeightPx: Float = 0f,
    topBarOffsetY: Animatable<Float, AnimationVector1D> = Animatable(0f),
    padding: PaddingValues = PaddingValues(),
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    val statusSheetState = rememberModalBottomSheetState()
    var showStatusSheet by remember { mutableStateOf(false) }
    fun hideStatusSheet() {
        scope.launch { statusSheetState.hide() }.invokeOnCompletion { showStatusSheet = false }
    }

    val editSheetState = rememberModalBottomSheetState()
    var showEditSheet by remember { mutableStateOf(false) }
    fun hideEditSheet() {
        scope.launch { editSheetState.hide() }.invokeOnCompletion { showEditSheet = false }
    }

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
    val bottomBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    if (showStatusSheet && uiState.listStatus != null) {
        ListStatusSheet(
            mediaType = uiState.mediaType,
            selectedStatus = uiState.listStatus,
            sheetState = statusSheetState,
            bottomPadding = bottomBarPadding,
            onStatusChanged = { event?.onChangeStatus(it) },
            onDismiss = {
                hideStatusSheet()
            }
        )
    }

    if (uiState.openSortDialog && uiState.listSort != null) {
        MediaListSortDialog(uiState, event)
    }

    if (uiState.openSetScoreDialog) {
        SetScoreDialog(
            onDismiss = { event?.toggleSetScoreDialog(false) },
            onConfirm = { event?.setScore(it) }
        )
    }

    if (uiState.isLoadingRandom) {
        LoadingDialog()
    }

    if (showEditSheet && uiState.mediaInfo != null) {
        EditMediaSheet(
            sheetState = editSheetState,
            mediaInfo = uiState.mediaInfo!!,
            myListStatus = uiState.myListStatus,
            bottomPadding = bottomBarPadding,
            onEdited = { status, removed ->
                hideEditSheet()
                event?.onChangeItemMyListStatus(status, removed)
            },
            onDismissed = {
                hideEditSheet()
            }
        )
    }

    LaunchedEffect(uiState.randomId) {
        uiState.randomId?.let { id ->
            event?.onRandomIdOpen()
            navActionManager.toMediaDetails(uiState.mediaType, id)
        }
    }

    LaunchedEffect(uiState.message) {
        if (uiState.message != null) {
            context.showToast(uiState.message)
            event?.onMessageDisplayed()
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
                    onClick = { showStatusSheet = true }
                ) {
                    Icon(
                        painter = painterResource(
                            id = uiState.listStatus?.icon ?: R.drawable.round_format_list_bulleted_24
                        ),
                        contentDescription = "status",
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(text = uiState.listStatus?.localized() ?: stringResource(R.string.loading))
                }
            }
        },
        contentWindowInsets = WindowInsets.systemBars
            .only(WindowInsetsSides.Horizontal)
    ) { childPadding ->
        if (uiState.listSort != null) {
            UserMediaListView(
                uiState = uiState,
                event = event,
                navActionManager = navActionManager,
                isCompactScreen = isCompactScreen,
                modifier = Modifier.padding(childPadding),
                nestedScrollConnection = nestedScrollConnection,
                topBarHeightPx = topBarHeightPx,
                topBarOffsetY = topBarOffsetY,
                contentPadding = PaddingValues(
                    start = padding.calculateStartPadding(LocalLayoutDirection.current),
                    top = padding.calculateTopPadding(),
                    end = padding.calculateEndPadding(LocalLayoutDirection.current),
                ),
                onShowEditSheet = { item ->
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    event?.onItemSelected(item)
                    showEditSheet = true
                }
            )
        }
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
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) }
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

@Preview
@Composable
fun UserMediaListHostPreview() {
    MoeListTheme {
        Surface {
            UserMediaListWithFabViewContent(
                uiState = UserMediaListUiState(
                    mediaType = MediaType.ANIME,
                    listStatus = ListStatus.WATCHING
                ),
                event = null,
                navActionManager = NavActionManager.rememberNavActionManager(),
                isCompactScreen = true
            )
        }
    }
}