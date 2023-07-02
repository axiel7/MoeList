package com.axiel7.moelist.uicompose.userlist

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
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
    modifier: Modifier = Modifier,
    navigateToMediaDetails: (MediaType, Int) -> Unit,
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

    Column(
        modifier = modifier
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
                listType = ListType(
                    status = tabRowItems[it].value,
                    mediaType = mediaType
                ),
                navigateToMediaDetails = navigateToMediaDetails,
            )
        }//:Pager
    }//:Column
}