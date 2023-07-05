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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.axiel7.moelist.data.model.media.ListType
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.data.model.media.listStatusAnimeValues
import com.axiel7.moelist.data.model.media.listStatusMangaValues
import com.axiel7.moelist.data.model.media.localized
import com.axiel7.moelist.uicompose.base.TabRowItem
import com.axiel7.moelist.uicompose.composables.RoundedTabRowIndicator
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UserMediaListWithTabsView(
    mediaType: MediaType,
    navigateToMediaDetails: (MediaType, Int) -> Unit,
    topBarHeightPx: Float,
    topBarOffsetY: Animatable<Float, AnimationVector1D>,
    padding: PaddingValues,
) {
    val scope = rememberCoroutineScope()
    val tabRowItems = remember {
        (if (mediaType == MediaType.ANIME) listStatusAnimeValues else listStatusMangaValues)
            .map { TabRowItem(
                value = it,
                title = it.value
            ) }
    }
    val pagerState = rememberPagerState { tabRowItems.size }
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
            key = { tabRowItems[it].value }
        ) {
            UserMediaListView(
                listType = ListType(status = tabRowItems[it].value, mediaType = mediaType),
                modifier = Modifier.padding(
                    bottom = systemBarsPadding.calculateBottomPadding()
                ),
                navigateToMediaDetails = navigateToMediaDetails,
                topBarHeightPx = topBarHeightPx,
                topBarOffsetY = topBarOffsetY,
                contentPadding = PaddingValues(
                    bottom = padding.calculateBottomPadding() +
                            systemBarsPadding.calculateBottomPadding()
                )
            )
        }//:Pager
    }//:Column
}