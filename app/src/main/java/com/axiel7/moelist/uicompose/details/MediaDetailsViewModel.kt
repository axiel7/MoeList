package com.axiel7.moelist.uicompose.details

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.axiel7.moelist.data.model.anime.AnimeDetails
import com.axiel7.moelist.data.model.anime.MyAnimeListStatus
import com.axiel7.moelist.data.model.anime.Recommendations
import com.axiel7.moelist.data.model.anime.RelatedAnime
import com.axiel7.moelist.data.model.manga.MangaDetails
import com.axiel7.moelist.data.model.manga.MyMangaListStatus
import com.axiel7.moelist.data.model.manga.RelatedManga
import com.axiel7.moelist.data.model.media.BaseMediaDetails
import com.axiel7.moelist.data.model.media.BaseMediaNode
import com.axiel7.moelist.data.model.media.BaseMyListStatus
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.data.repository.AnimeRepository
import com.axiel7.moelist.data.repository.MangaRepository
import com.axiel7.moelist.uicompose.base.BaseMediaViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MediaDetailsViewModel(
    override val mediaType: MediaType
) : BaseMediaViewModel() {

    init {
        isLoading = true
    }

    override var myListStatus: BaseMyListStatus? = null
        get() = mediaDetails?.myListStatus
        set(value) {
            when (mediaDetails) {
                is AnimeDetails -> mediaDetails = (mediaDetails as AnimeDetails)
                    .copy(myListStatus = value as? MyAnimeListStatus)

                is MangaDetails -> mediaDetails = (mediaDetails as MangaDetails)
                    .copy(myListStatus = value as? MyMangaListStatus)

                else -> field = value
            }
        }
    var mediaDetails by mutableStateOf<BaseMediaDetails?>(null)
    var studioSerializationJoined by mutableStateOf<String?>(null)
    var relatedAnime = mutableStateListOf<RelatedAnime>()
    var relatedManga = mutableStateListOf<RelatedManga>()
    var recommendations = mutableStateListOf<Recommendations<BaseMediaNode>>()
    var picturesUrls = emptyArray<String>()

    @Suppress("UNCHECKED_CAST")
    fun getDetails(mediaId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            isLoading = true
            if (mediaType == MediaType.ANIME) {
                AnimeRepository.getAnimeDetails(mediaId)?.let { details ->
                    mediaDetails = details
                    mediaInfo = details.toAnimeNode()
                    studioSerializationJoined = details.studios?.joinToString { it.name }
                }
            } else {
                MangaRepository.getMangaDetails(mediaId)?.let { details ->
                    mediaDetails = details
                    mediaInfo = details.toMangaNode()
                    studioSerializationJoined = details.serialization?.joinToString { it.node.name }
                }
            }
            relatedAnime.clear()
            mediaDetails?.relatedAnime?.let { relatedAnime.addAll(it) }
            relatedManga.clear()
            mediaDetails?.relatedManga?.let { relatedManga.addAll(it) }
            recommendations.clear()
            (mediaDetails?.recommendations as? List<Recommendations<BaseMediaNode>>)
                ?.let { recommendations.addAll(it) }

            picturesUrls = arrayOf(mediaDetails?.mainPicture?.large ?: "")
                .plus(mediaDetails?.pictures?.map { it.large }?.toTypedArray() ?: emptyArray())

            if (mediaDetails == null) setErrorMessage("Unable to reach server")
            else if (mediaDetails!!.error != null) setErrorMessage(
                mediaDetails!!.message ?: "Generic error"
            )
            else isLoading = false
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