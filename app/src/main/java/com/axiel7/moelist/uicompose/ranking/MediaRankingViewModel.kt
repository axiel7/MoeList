package com.axiel7.moelist.uicompose.ranking

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.viewModelScope
import com.axiel7.moelist.App
import com.axiel7.moelist.data.model.ApiParams
import com.axiel7.moelist.data.model.media.BaseRanking
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.data.model.media.RankingType
import com.axiel7.moelist.data.repository.AnimeRepository
import com.axiel7.moelist.data.repository.MangaRepository
import com.axiel7.moelist.uicompose.base.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MediaRankingViewModel(
    private val mediaType: MediaType,
    private val rankingType: RankingType
): BaseViewModel() {

    private val params = ApiParams(
        nsfw = App.nsfw,
        fields = AnimeRepository.RANKING_FIELDS
    )

    var mediaList = mutableStateListOf<BaseRanking>()
    var nextPage: String? = null
    var hasNextPage = false
    var loadedAllPages = false

    fun getRanking(page: String? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            isLoading = page == null //show indicator on 1st load
            val result = if (mediaType == MediaType.ANIME)
                AnimeRepository.getAnimeRanking(rankingType, params, page)
            else
                MangaRepository.getMangaRanking(rankingType, params, page)

            if (result?.data != null) {
                if (page == null) mediaList.clear()
                mediaList.addAll(result.data)

                nextPage = result.paging?.next
                hasNextPage = nextPage != null
                loadedAllPages = page != null && nextPage == null
            } else {
                setErrorMessage(result?.message ?: "Generic error")
                hasNextPage = false
            }
            isLoading = false
        }
    }
}