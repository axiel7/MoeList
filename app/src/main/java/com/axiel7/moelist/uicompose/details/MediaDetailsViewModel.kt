package com.axiel7.moelist.uicompose.details

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.axiel7.moelist.data.model.anime.AnimeDetails
import com.axiel7.moelist.data.model.manga.MangaDetails
import com.axiel7.moelist.data.model.media.BaseMediaDetails
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.data.model.media.Related
import com.axiel7.moelist.data.repository.AnimeRepository
import com.axiel7.moelist.data.repository.MangaRepository
import com.axiel7.moelist.uicompose.base.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MediaDetailsViewModel(
    val mediaType: MediaType
): BaseViewModel() {

    init {
        isLoading = true
    }

    var basicDetails by mutableStateOf<BaseMediaDetails?>(null)
    var animeDetails by mutableStateOf<AnimeDetails?>(null)
    var mangaDetails by mutableStateOf<MangaDetails?>(null)
    var related by mutableStateOf(emptyList<Related>())

    fun getDetails(mediaId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            isLoading = true
            if (mediaType == MediaType.ANIME) {
                animeDetails = AnimeRepository.getAnimeDetails(mediaId)
                basicDetails = animeDetails
            } else {
                mangaDetails = MangaRepository.getMangaDetails(mediaId)
                basicDetails = mangaDetails
            }
            val tempRelated = mutableListOf<Related>()
            basicDetails?.relatedAnime?.let { tempRelated.addAll(it) }
            basicDetails?.relatedManga?.let { tempRelated.addAll(it) }
            related = tempRelated
            isLoading = false
        }
    }

    fun buildQueryFromThemeText(themeText: String): String {
        var query = themeText.replace(" ", "+")
        val size = query.length

        if (query.startsWith("#")) {
            query = query.substring(4, size)
        }
        val index = query.indexOf("(ep")

        return if (index == -1) query
        else query.substring(0, index - 1)
    }
}