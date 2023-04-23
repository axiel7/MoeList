package com.axiel7.moelist.uicompose.details

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.axiel7.moelist.data.model.anime.AnimeDetails
import com.axiel7.moelist.data.model.anime.Recommendations
import com.axiel7.moelist.data.model.anime.RelatedAnime
import com.axiel7.moelist.data.model.manga.MangaDetails
import com.axiel7.moelist.data.model.manga.RelatedManga
import com.axiel7.moelist.data.model.media.BaseMediaDetails
import com.axiel7.moelist.data.model.media.BaseMediaNode
import com.axiel7.moelist.data.model.media.MediaType
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

    var mediaDetails by mutableStateOf<BaseMediaDetails?>(null)
    var studioSerializationJoined by mutableStateOf<String?>(null)
    var relatedAnime = mutableStateListOf<RelatedAnime>()
    var relatedManga = mutableStateListOf<RelatedManga>()
    var recommendations = mutableStateListOf<Recommendations<BaseMediaNode>>()

    @Suppress("UNCHECKED_CAST")
    fun getDetails(mediaId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            isLoading = true
            if (mediaType == MediaType.ANIME) {
                mediaDetails = AnimeRepository.getAnimeDetails(mediaId)
                studioSerializationJoined = (mediaDetails as? AnimeDetails?)?.studios?.joinToString { it.name }
            } else {
                mediaDetails = MangaRepository.getMangaDetails(mediaId)
                studioSerializationJoined = (mediaDetails as? MangaDetails?)?.serialization?.joinToString { it.node.name }
            }
            relatedAnime.clear()
            mediaDetails?.relatedAnime?.let { relatedAnime.addAll(it) }
            relatedManga.clear()
            mediaDetails?.relatedManga?.let { relatedManga.addAll(it) }
            recommendations.clear()
            (mediaDetails?.recommendations as? List<Recommendations<BaseMediaNode>>)
                ?.let { recommendations.addAll(it) }
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