package com.axiel7.moelist.ui.userlist

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axiel7.moelist.data.model.media.ListStatus.Companion.listStatusValues
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.ui.base.TabRowItem
import com.axiel7.moelist.ui.base.navigation.NavActionManager
import com.axiel7.moelist.ui.composables.LoadingDialog
import com.axiel7.moelist.ui.composables.TabRowWithPager
import com.axiel7.moelist.ui.editmedia.EditMediaSheet
import com.axiel7.moelist.ui.userlist.composables.MediaListSortDialog
import com.axiel7.moelist.ui.userlist.composables.SetAsCompletedDialog
import com.axiel7.moelist.utils.ContextExtensions.showToast
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserMediaListWithTabsView(
    mediaType: MediaType,
    isCompactScreen: Boolean,
    navActionManager: NavActionManager,
    topBarHeightPx: Float,
    topBarOffsetY: Animatable<Float, AnimationVector1D>,
    padding: PaddingValues,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val tabRowItems = remember {
        listStatusValues(mediaType)
            .map {
                TabRowItem(value = it, title = it.stringRes)
            }.toTypedArray()
    }
    val editSheetState = rememberModalBottomSheetState()
    var showEditSheet by remember { mutableStateOf(false) }
    fun hideEditSheet() {
        scope.launch { editSheetState.hide() }.invokeOnCompletion { showEditSheet = false }
    }

    val systemBarsPadding = WindowInsets.systemBars.asPaddingValues()

    TabRowWithPager(
        tabs = tabRowItems,
        modifier = Modifier
            .padding(
                top = padding.calculateTopPadding(),
            )
            .graphicsLayer {
                val topPadding = padding.calculateTopPadding().value +
                        systemBarsPadding.calculateTopPadding().value +
                        systemBarsPadding.calculateBottomPadding().value

                translationY = if (topBarOffsetY.value > -topPadding) topBarOffsetY.value
                else -topPadding
            },
        beyondBoundsPageCount = -1,
        isTabScrollable = true
    ) {
        val listStatus = tabRowItems[it].value
        val viewModel: UserMediaListViewModel = koinViewModel(
            key = listStatus.name,
            parameters = { parametersOf(mediaType, listStatus) }
        )
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

        if (uiState.openSortDialog && uiState.listSort != null) {
            MediaListSortDialog(
                uiState = uiState,
                event = viewModel
            )
        }

        if (uiState.openSetAtCompletedDialog) {
            SetAsCompletedDialog(
                uiState = uiState,
                event = viewModel
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
                bottomPadding = systemBarsPadding.calculateBottomPadding(),
                onEdited = { status, removed ->
                    hideEditSheet()
                    viewModel.onChangeItemMyListStatus(status, removed)
                },
                onDismissed = { hideEditSheet() }
            )
        }

        LaunchedEffect(uiState.randomId) {
            uiState.randomId?.let { id ->
                navActionManager.toMediaDetails(uiState.mediaType, id)
                viewModel.onRandomIdOpen()
            }
        }

        LaunchedEffect(uiState.message) {
            if (uiState.message != null) {
                context.showToast(uiState.message.orEmpty())
                viewModel.onMessageDisplayed()
            }
        }

        if (uiState.listSort != null) {
            UserMediaListView(
                uiState = uiState,
                event = viewModel,
                navActionManager = navActionManager,
                isCompactScreen = isCompactScreen,
                modifier = Modifier.padding(
                    bottom = systemBarsPadding.calculateBottomPadding() + 8.dp
                ),
                topBarHeightPx = topBarHeightPx,
                topBarOffsetY = topBarOffsetY,
                contentPadding = PaddingValues(
                    bottom = padding.calculateBottomPadding() +
                            systemBarsPadding.calculateBottomPadding()
                ),
                onShowEditSheet = { item ->
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.onItemSelected(item)
                    showEditSheet = true
                },
            )
        }
    }//:Pager
}