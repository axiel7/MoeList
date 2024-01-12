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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
    navigateToMediaDetails: (MediaType, Int) -> Unit,
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
            parameters = { parametersOf(listStatus) }
        )
        val listSort by viewModel.listSort.collectAsStateWithLifecycle()

        if (viewModel.openSortDialog && listSort != null) {
            MediaListSortDialog(
                listSort = listSort!!,
                viewModel = viewModel
            )
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
                bottomPadding = systemBarsPadding.calculateBottomPadding()
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

        if (listSort != null) {
            UserMediaListView(
                viewModel = viewModel,
                listSort = listSort!!,
                isCompactScreen = isCompactScreen,
                modifier = Modifier.padding(
                    bottom = systemBarsPadding.calculateBottomPadding() + 8.dp
                ),
                navigateToMediaDetails = navigateToMediaDetails,
                topBarHeightPx = topBarHeightPx,
                topBarOffsetY = topBarOffsetY,
                contentPadding = PaddingValues(
                    bottom = padding.calculateBottomPadding() +
                            systemBarsPadding.calculateBottomPadding()
                ),
                onShowEditSheet = { item ->
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    scope.launch {
                        viewModel.onItemSelected(item)
                        editSheetState.show()
                    }
                },
            )
        }
    }//:Pager
}