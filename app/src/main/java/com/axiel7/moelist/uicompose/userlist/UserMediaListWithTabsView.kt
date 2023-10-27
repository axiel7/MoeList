package com.axiel7.moelist.uicompose.userlist

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axiel7.moelist.data.model.media.ListStatus.Companion.listStatusValues
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.uicompose.base.TabRowItem
import com.axiel7.moelist.uicompose.composables.LoadingDialog
import com.axiel7.moelist.uicompose.composables.RoundedTabRowIndicator
import com.axiel7.moelist.uicompose.editmedia.EditMediaSheet
import com.axiel7.moelist.uicompose.userlist.composables.MediaListSortDialog
import com.axiel7.moelist.uicompose.userlist.composables.SetAsCompletedDialog
import com.axiel7.moelist.utils.ContextExtensions.showToast
import kotlinx.coroutines.launch
import org.koin.androidx.compose.navigation.koinNavViewModel

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
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
                TabRowItem(
                    value = it,
                    title = it.value
                )
            }
    }
    val pagerState = rememberPagerState { tabRowItems.size }
    val editSheetState = rememberModalBottomSheetState()
    val systemBarsPadding = WindowInsets.systemBars.asPaddingValues()

    Column(
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
            }
    ) {
        ScrollableTabRow(
            selectedTabIndex = pagerState.currentPage,
            edgePadding = 16.dp,
            indicator = { tabPositions ->
                RoundedTabRowIndicator(tabPositions[pagerState.currentPage])
            }
        ) {
            tabRowItems.forEachIndexed { index, item ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                    text = { Text(text = item.value.localized()) },
                    unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            beyondBoundsPageCount = 0,
            verticalAlignment = Alignment.Top,
            key = { tabRowItems[it].value }
        ) {
            val listStatus = tabRowItems[it].value
            val viewModel: UserMediaListViewModel = koinNavViewModel(key = listStatus.name)
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
    }//:Column
}