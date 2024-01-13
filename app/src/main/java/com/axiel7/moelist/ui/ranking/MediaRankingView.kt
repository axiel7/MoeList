package com.axiel7.moelist.ui.ranking

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.data.model.media.RankingType.Companion.rankingAnimeValues
import com.axiel7.moelist.data.model.media.RankingType.Companion.rankingMangaValues
import com.axiel7.moelist.ui.base.TabRowItem
import com.axiel7.moelist.ui.base.navigation.NavActionManager
import com.axiel7.moelist.ui.composables.DefaultScaffoldWithTopAppBar
import com.axiel7.moelist.ui.composables.TabRowWithPager
import com.axiel7.moelist.ui.ranking.list.MediaRankingListView

@Composable
fun MediaRankingView(
    mediaType: MediaType,
    isCompactScreen: Boolean,
    navActionManager: NavActionManager,
) {
    val tabRowItems = remember {
        (if (mediaType == MediaType.ANIME) rankingAnimeValues else rankingMangaValues)
            .map {
                TabRowItem(value = it, title = it.stringRes)
            }.toTypedArray()
    }

    DefaultScaffoldWithTopAppBar(
        title = stringResource(
            if (mediaType == MediaType.ANIME) R.string.anime_ranking
            else R.string.manga_ranking
        ),
        navigateBack = navActionManager::goBack,
        contentWindowInsets = WindowInsets.systemBars
            .only(WindowInsetsSides.Horizontal)
    ) { padding ->
        TabRowWithPager(
            tabs = tabRowItems,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            isTabScrollable = true
        ) {
            MediaRankingListView(
                mediaType = mediaType,
                rankingType = tabRowItems[it].value,
                isCompactScreen = isCompactScreen,
                navActionManager = navActionManager,
            )
        }
    }
}