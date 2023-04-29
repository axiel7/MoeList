package com.axiel7.moelist.uicompose.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.viewModelScope
import com.axiel7.moelist.App
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.User
import com.axiel7.moelist.data.model.media.Stat
import com.axiel7.moelist.data.repository.UserRepository
import com.axiel7.moelist.uicompose.base.BaseViewModel
import com.axiel7.moelist.utils.PreferencesDataStore.PROFILE_PICTURE_PREFERENCE_KEY
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class ProfileViewModel : BaseViewModel() {

    init {
        App.dataStore?.data?.map {
            profilePictureUrl = it[PROFILE_PICTURE_PREFERENCE_KEY]
        }
    }

    var user by mutableStateOf<User?>(null)
    var animeStats by mutableStateOf(listOf(
        Stat(title = R.string.watching, value = 0f, color = Color(red = 0, green = 200, blue = 83)),
        Stat(title = R.string.completed, value = 0f, color = Color(red = 92, green = 107, blue = 192)),
        Stat(title = R.string.on_hold, value = 0f, color = Color(red = 255, green = 213, blue = 0)),
        Stat(title = R.string.dropped, value = 0f, color = Color(red = 213, green = 0, blue = 0)),
        Stat(title = R.string.ptw, value = 0f, color = Color(red = 158, green = 158, blue = 158)),
    ))
    var profilePictureUrl by mutableStateOf<String?>(null)

    fun getMyUser() {
        isLoading = true
        viewModelScope.launch(Dispatchers.IO) {
            user = UserRepository.getMyUser()

            user?.animeStatistics?.let { stats ->
                val tempStatList = mutableListOf<Stat>()
                tempStatList.add(animeStats[0].copy(value = stats.numItemsWatching?.toFloat() ?: 0f))
                tempStatList.add(animeStats[1].copy(value = stats.numItemsCompleted?.toFloat() ?: 0f))
                tempStatList.add(animeStats[2].copy(value = stats.numItemsOnHold?.toFloat() ?: 0f))
                tempStatList.add(animeStats[3].copy(value = stats.numItemsDropped?.toFloat() ?: 0f))
                tempStatList.add(animeStats[4].copy(value = stats.numItemsPlanToWatch?.toFloat() ?: 0f))
                animeStats = tempStatList
            }
            if (user?.picture != null && user?.picture != profilePictureUrl) {
                App.dataStore?.edit {
                    it[PROFILE_PICTURE_PREFERENCE_KEY] = user!!.picture!!
                }
                profilePictureUrl = user!!.picture!!
            }

            isLoading = false
        }
    }
}