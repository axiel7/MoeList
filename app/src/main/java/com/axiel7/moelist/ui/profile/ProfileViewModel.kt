package com.axiel7.moelist.ui.profile

import androidx.lifecycle.viewModelScope
import com.axiel7.moelist.data.model.media.ListStatus
import com.axiel7.moelist.data.model.media.Stat
import com.axiel7.moelist.data.repository.DefaultPreferencesRepository
import com.axiel7.moelist.data.repository.UserRepository
import com.axiel7.moelist.ui.base.viewmodel.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val userRepository: UserRepository,
    private val defaultPreferencesRepository: DefaultPreferencesRepository
) : BaseViewModel<ProfileUiState>(), ProfileEvent {

    override val mutableUiState = MutableStateFlow(ProfileUiState())

    init {
        viewModelScope.launch(Dispatchers.IO) {
            setLoading(true)
            val user = userRepository.getMyUser()

            if (user == null || user.message != null) {
                showMessage(user?.message)
            } else {
                val tempAnimeStatList = mutableListOf<Stat<ListStatus>>()
                user.animeStatistics?.let { stats ->
                    tempAnimeStatList.add(
                        Stat(
                            type = ListStatus.WATCHING,
                            value = stats.numItemsWatching?.toFloat() ?: 0f
                        )
                    )
                    tempAnimeStatList.add(
                        Stat(
                            type = ListStatus.COMPLETED,
                            value = stats.numItemsCompleted?.toFloat() ?: 0f
                        )
                    )
                    tempAnimeStatList.add(
                        Stat(
                            type = ListStatus.ON_HOLD,
                            value = stats.numItemsOnHold?.toFloat() ?: 0f
                        )
                    )
                    tempAnimeStatList.add(
                        Stat(
                            type = ListStatus.DROPPED,
                            value = stats.numItemsDropped?.toFloat() ?: 0f
                        )
                    )
                    tempAnimeStatList.add(
                        Stat(
                            type = ListStatus.PLAN_TO_WATCH,
                            value = stats.numItemsPlanToWatch?.toFloat() ?: 0f
                        )
                    )
                }

                if (user.picture != null && user.picture != mutableUiState.value.profilePictureUrl) {
                    defaultPreferencesRepository.setProfilePicture(user.picture)
                }

                mutableUiState.update {
                    it.copy(
                        user = user,
                        profilePictureUrl = user.picture,
                        animeStats = tempAnimeStatList,
                        isLoading = false,
                        isLoadingManga = user.name != null,
                    )
                }

                // get manga stats from jikan api because the official api has not implemented it
                user.name?.let { username ->
                    // convert the username to lowercase because a bug in the jikan api
                    val jikanUserStats = userRepository.getUserStats(username.lowercase())

                    jikanUserStats.data?.manga?.let { stats ->
                        val tempMangaStatList = mutableListOf<Stat<ListStatus>>()
                        tempMangaStatList.add(
                            Stat(
                                type = ListStatus.READING,
                                value = stats.current.toFloat()
                            )
                        )
                        tempMangaStatList.add(
                            Stat(
                                type = ListStatus.COMPLETED,
                                value = stats.completed.toFloat()
                            )
                        )
                        tempMangaStatList.add(
                            Stat(
                                type = ListStatus.ON_HOLD,
                                value = stats.onHold.toFloat()
                            )
                        )
                        tempMangaStatList.add(
                            Stat(
                                type = ListStatus.DROPPED,
                                value = stats.dropped.toFloat()
                            )
                        )
                        tempMangaStatList.add(
                            Stat(
                                type = ListStatus.PLAN_TO_READ,
                                value = stats.planned.toFloat()
                            )
                        )
                        mutableUiState.update {
                            it.copy(
                                mangaStats = tempMangaStatList,
                                userMangaStats = stats,
                                isLoadingManga = false
                            )
                        }
                    }
                }
            }
        }
    }
}