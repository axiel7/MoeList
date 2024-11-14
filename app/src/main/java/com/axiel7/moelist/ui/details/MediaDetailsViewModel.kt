package com.axiel7.moelist.ui.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.axiel7.moelist.data.model.Response
import com.axiel7.moelist.data.model.anime.AnimeDetails
import com.axiel7.moelist.data.model.anime.MyAnimeListStatus
import com.axiel7.moelist.data.model.anime.Recommendations
import com.axiel7.moelist.data.model.manga.MangaDetails
import com.axiel7.moelist.data.model.manga.MyMangaListStatus
import com.axiel7.moelist.data.model.media.BaseMediaNode
import com.axiel7.moelist.data.model.media.BaseMyListStatus
import com.axiel7.moelist.data.model.media.Character
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.data.model.media.WeekDay
import com.axiel7.moelist.data.repository.AnimeRepository
import com.axiel7.moelist.data.repository.DefaultPreferencesRepository
import com.axiel7.moelist.data.repository.MangaRepository
import com.axiel7.moelist.ui.base.navigation.Route
import com.axiel7.moelist.ui.base.viewmodel.BaseViewModel
import com.axiel7.moelist.worker.NotificationWorkerManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import kotlin.reflect.typeOf

@Suppress("UNCHECKED_CAST")
class MediaDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val animeRepository: AnimeRepository,
    private val mangaRepository: MangaRepository,
    private val notificationWorkerManager: NotificationWorkerManager,
    defaultPreferencesRepository: DefaultPreferencesRepository,
) : BaseViewModel<MediaDetailsUiState>(), MediaDetailsEvent {

    private val args = savedStateHandle.toRoute<Route.MediaDetails>(
        typeMap = mapOf(typeOf<MediaType>() to MediaType.navType)
    )
    private val mediaType = args.mediaType
    private val mediaId = args.mediaId

    override val mutableUiState = MutableStateFlow(MediaDetailsUiState())

    override fun onChangedMyListStatus(value: BaseMyListStatus?, removed: Boolean) {
        mutableUiState.update {
            when (it.mediaDetails) {
                is AnimeDetails -> {
                    it.copy(
                        mediaDetails = it.mediaDetails.copy(
                            myListStatus = (value as? MyAnimeListStatus).takeIf { !removed }
                        )
                    )
                }

                is MangaDetails -> {
                    it.copy(
                        mediaDetails = it.mediaDetails.copy(
                            myListStatus = (value as? MyMangaListStatus).takeIf { !removed }
                        )
                    )
                }

                else -> it
            }
        }
    }

    override fun getCharacters() {
        viewModelScope.launch(Dispatchers.IO) {
            mutableUiState.update { it.copy(isLoadingCharacters = true) }

            val result = animeRepository.getAnimeCharacters(
                animeId = mediaId,
                limit = null,
                offset = null,
                page = null,
            )

            if (result.wasError) {
                mutableUiState.update {
                    it.copy(
                        isLoadingCharacters = false,
                        message = result.message ?: "Error loading characters"
                    )
                }
            } else {
                mutableUiState.update {
                    it.copy(
                        characters = Sort_Characters(result.data), //result.data.orEmpty(),
                        isLoadingCharacters = false
                    )
                }
            }
        }
    }

    private fun Sort_Characters( data: List<Character>? ): List<Character> {
        //sort By Roles ,  1st Main, 2nd Support
        return  data.orEmpty().sortedBy { it.role };
    }

    override fun scheduleAiringAnimeNotification(
        title: String,
        animeId: Int,
        weekDay: WeekDay,
        jpHour: LocalTime
    ) {
        viewModelScope.launch {
            notificationWorkerManager.scheduleAiringAnimeNotification(
                title,
                animeId,
                weekDay,
                jpHour
            )
        }
    }

    override fun scheduleAnimeStartNotification(
        title: String,
        animeId: Int,
        startDate: LocalDate,
    ) {
        viewModelScope.launch {
            notificationWorkerManager.scheduleAnimeStartNotification(title, animeId, startDate)
        }
    }

    override fun removeAiringAnimeNotification(animeId: Int) {
        viewModelScope.launch {
            notificationWorkerManager.removeAiringAnimeNotification(animeId)
        }
    }

    init {
        viewModelScope.launch(Dispatchers.IO) {
            setLoading(true)
            val mediaDetails = if (mediaType == MediaType.ANIME) {
                animeRepository.getAnimeDetails(mediaId)
            } else {
                mangaRepository.getMangaDetails(mediaId)
            }

            if (mediaDetails == null) showMessage("Unable to reach server")
            else if (mediaDetails.error != null) showMessage(mediaDetails.error)
            else {
                val recommendations =
                    (mediaDetails.recommendations as? List<Recommendations<BaseMediaNode>>).orEmpty()

                val picturesUrls = listOf(mediaDetails.mainPicture?.large.orEmpty())
                    .plus(mediaDetails.pictures?.map { it.large ?: it.medium.orEmpty() }
                        .orEmpty())

                mutableUiState.update {
                    it.copy(
                        mediaDetails = mediaDetails,
                        relatedAnime = mediaDetails.relatedAnime.orEmpty(),
                        relatedManga = mediaDetails.relatedManga.orEmpty(),
                        recommendations = recommendations,
                        picturesUrls = picturesUrls,
                        isLoading = false
                    )
                }

                if (mediaType == MediaType.ANIME
                    && defaultPreferencesRepository.loadCharacters.first()
                ) {
                    getCharacters()
                }
            }
        }

        notificationWorkerManager.getNotification(mediaId)
            .onEach { notification ->
                mutableUiState.update { it.copy(notification = notification) }
            }
            .launchIn(viewModelScope)

        notificationWorkerManager.getStartNotification(mediaId)
            .onEach { startNotification ->
                mutableUiState.update { it.copy(startNotification = startNotification) }
            }
            .launchIn(viewModelScope)
    }
}
