package com.axiel7.moelist.ui.details.anime

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axiel7.moelist.App
import com.axiel7.moelist.data.model.Related
import com.axiel7.moelist.data.model.anime.AnimeDetails
import com.axiel7.moelist.utils.Constants.ERROR_SERVER
import com.axiel7.moelist.utils.Constants.RESPONSE_ERROR
import com.axiel7.moelist.utils.Constants.RESPONSE_NONE
import com.axiel7.moelist.utils.Constants.RESPONSE_OK
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AnimeDetailsViewModel : ViewModel() {

    private val _animeDetails = MutableStateFlow<AnimeDetails?>(null)
    val animeDetails: StateFlow<AnimeDetails?> = _animeDetails

    private val _relateds = MutableStateFlow<List<Related>>(emptyList())
    val relateds: StateFlow<List<Related>> = _relateds

    private val _response = MutableStateFlow(RESPONSE_NONE to "")
    val response: StateFlow<Pair<String, String>> = _response

    fun getAnimeDetails(animeId: Int) {
        viewModelScope.launch {
            App.animeDb.animeDetailsDao().getAnimeDetailsById(animeId)?.let { postValues(it) }

            val call = async { App.api.getAnimeDetails(animeId, FIELDS) }
            val result = try {
                call.await()
            } catch (e: Exception) {
                null
            }

            when {
                result == null -> _response.value = RESPONSE_ERROR to ERROR_SERVER
                !result.error.isNullOrEmpty() -> _response.value = RESPONSE_ERROR to "${result.error}: ${result.message}"
                !result.message.isNullOrEmpty() -> _response.value = RESPONSE_ERROR to "${result.error}: ${result.message}"
                else -> {
                    _response.value = RESPONSE_OK to ""
                    postValues(result)
                    App.animeDb.animeDetailsDao().insertAnimeDetails(result)
                }
            }
        }
    }

    private fun postValues(data: AnimeDetails) {
        _animeDetails.value = data
        mutableListOf<Related>().apply {
            data.relatedAnime?.let { addAll(it) }
            data.relatedManga?.let { addAll(it) }
            _relateds.value = this
        }
    }


    companion object {
        private const val FIELDS = "id,title,main_picture,alternative_titles,start_date,end_date,synopsis,mean,rank,popularity," +
                "num_list_users,num_scoring_users,media_type,status,genres,my_list_status,num_episodes,start_season," +
                "broadcast,source,average_episode_duration,studios,opening_themes,ending_themes,related_anime{media_type},related_manga{media_type}"
    }
}