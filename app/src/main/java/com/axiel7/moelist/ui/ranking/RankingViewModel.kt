package com.axiel7.moelist.ui.ranking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.axiel7.moelist.App
import com.axiel7.moelist.data.model.ApiParams
import com.axiel7.moelist.data.paging.AnimeRankingPaging
import com.axiel7.moelist.data.paging.MangaRankingPaging
import kotlinx.coroutines.flow.MutableStateFlow

class RankingViewModel : ViewModel() {

    private val nsfw = MutableStateFlow(0)
    fun setNsfw(value: Int) {
        nsfw.value = value
        params.value.nsfw = nsfw.value
    }

    private val rankType = MutableStateFlow("all")
    fun setRankType(value: String) {
        rankType.value = value
    }

    private val params = MutableStateFlow(
        ApiParams(
            nsfw = nsfw.value,
            fields = FIELDS
        )
    )

    val animeRankingFlow = Pager(
        PagingConfig(pageSize = 15, prefetchDistance = 10)
    ) {
        AnimeRankingPaging(App.api, params.value, rankType.value)
    }.flow
        .cachedIn(viewModelScope)

    val mangaRankingFlow = Pager(
        PagingConfig(pageSize = 15, prefetchDistance = 10)
    ) {
        MangaRankingPaging(App.api, params.value, rankType.value)
    }.flow
        .cachedIn(viewModelScope)

    companion object {
        private const val FIELDS = "mean,media_type,num_episodes,num_chapters,num_list_users"
    }
}