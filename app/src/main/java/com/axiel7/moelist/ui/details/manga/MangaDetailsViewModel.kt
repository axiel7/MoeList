package com.axiel7.moelist.ui.details.manga

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axiel7.moelist.App
import com.axiel7.moelist.data.model.Related
import com.axiel7.moelist.data.model.manga.MangaDetails
import com.axiel7.moelist.utils.Constants.ERROR_SERVER
import com.axiel7.moelist.utils.Constants.RESPONSE_ERROR
import com.axiel7.moelist.utils.Constants.RESPONSE_NONE
import com.axiel7.moelist.utils.Constants.RESPONSE_OK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MangaDetailsViewModel : ViewModel() {

    private val _mangaDetails = MutableStateFlow<MangaDetails?>(null)
    val mangaDetails: StateFlow<MangaDetails?> = _mangaDetails

    private val _relateds = MutableStateFlow<MutableList<Related>>(mutableListOf())
    val relateds: StateFlow<MutableList<Related>> = _relateds

    private val _response = MutableStateFlow(RESPONSE_NONE to "")
    val response: StateFlow<Pair<String, String>> = _response

    fun getMangaDetails(mangaId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            App.animeDb.mangaDetailsDao().getMangaDetailsById(mangaId)?.let { postValues(it) }

            val result = try {
                App.api.getMangaDetails(mangaId, FIELDS)
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
                    App.animeDb.mangaDetailsDao().insertMangaDetails(result)
                }
            }
        }
    }

    private fun postValues(data: MangaDetails) {
        _mangaDetails.value = data
        _relateds.value.clear()
        data.relatedAnime?.let { _relateds.value.addAll(it) }
        data.relatedManga?.let { _relateds.value.addAll(it) }
        _relateds.value = _relateds.value
    }

    companion object {
        private const val FIELDS = "id,title,main_picture,alternative_titles,start_date,end_date,synopsis,mean,rank,popularity," +
                "num_list_users,num_scoring_users,media_type,status,genres,my_list_status,num_chapters,num_volumes," +
                "source,authors{first_name,last_name},serialization,related_anime{media_type},related_manga{media_type}"
    }
}