package com.axiel7.moelist.uicompose.ranking

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.axiel7.moelist.data.model.media.BaseRanking
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.data.model.media.RankingType
import com.axiel7.moelist.data.repository.AnimeRepository
import com.axiel7.moelist.data.repository.MangaRepository
import com.axiel7.moelist.uicompose.base.BaseViewModel
import com.axiel7.moelist.uicompose.details.MEDIA_TYPE_ARGUMENT
import com.axiel7.moelist.utils.StringExtensions.removeFirstAndLast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MediaRankingViewModel(
    savedStateHandle: SavedStateHandle,
    private val animeRepository: AnimeRepository,
    private val mangaRepository: MangaRepository,
) : BaseViewModel() {

    private val mediaType = MediaType.valueOf(
        savedStateHandle.get<String>(MEDIA_TYPE_ARGUMENT.removeFirstAndLast())!!
    )

    val mediaList = mutableStateListOf<BaseRanking>()
    var nextPage: String? = null
    var hasNextPage = false
    var loadedAllPages = false

    fun getRanking(
        rankingType: RankingType,
        page: String? = null
    ) = viewModelScope.launch(Dispatchers.IO) {
        isLoading = page == null //show indicator on 1st load
        val result = if (mediaType == MediaType.ANIME)
            animeRepository.getAnimeRanking(
                rankingType = rankingType,
                limit = 25,
                fields = AnimeRepository.RANKING_FIELDS,
                page = page,
            )
        else
            mangaRepository.getMangaRanking(
                rankingType = rankingType,
                limit = 25,
                page = page,
            )

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