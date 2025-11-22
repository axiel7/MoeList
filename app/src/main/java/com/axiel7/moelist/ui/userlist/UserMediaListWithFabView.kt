package com.axiel7.moelist.ui.userlist

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axiel7.moelist.data.model.media.ListStatus
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.ui.base.navigation.NavActionManager
import com.axiel7.moelist.ui.composables.LoadingDialog
import com.axiel7.moelist.ui.editmedia.EditMediaSheet
import com.axiel7.moelist.ui.theme.MoeListTheme
import com.axiel7.moelist.ui.userlist.composables.ListStatusFab
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
    topBarOffsetY: Float,
    topBarAnimateTo: suspend (Float) -> Unit,
    topBarSnapTo: suspend (Float) -> Unit,
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
        topBarAnimateTo = topBarAnimateTo,
        topBarSnapTo = topBarSnapTo,
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
    topBarOffsetY: Float = 0f,
    topBarAnimateTo: suspend (Float) -> Unit= {},
    topBarSnapTo: suspend (Float) -> Unit = {},
    padding: PaddingValues = PaddingValues(),
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

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
            if (uiState.listStatus != null) {
                ListStatusFab(
                    mediaType = uiState.mediaType,
                    status = uiState.listStatus,
                    onStatusChanged = { event?.onChangeStatus(it) },
                    isVisible = isFabVisible,
                )
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
                topBarAnimateTo = topBarAnimateTo,
                topBarSnapTo = topBarSnapTo,
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