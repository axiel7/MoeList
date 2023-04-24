package com.axiel7.moelist.uicompose.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewModelScope
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.User
import com.axiel7.moelist.data.model.media.Stat
import com.axiel7.moelist.data.repository.UserRepository
import com.axiel7.moelist.uicompose.base.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ProfileViewModel : BaseViewModel() {

    var user by mutableStateOf<User?>(null)
    var animeStats = mutableStateOf(listOf(
        Stat(title = R.string.watching, value = 0.0, color = Color(red = 0, green = 200, blue = 83)),
        Stat(title = R.string.completed, value = 0.0, color = Color(red = 92, green = 107, blue = 192)),
        Stat(title = R.string.on_hold, value = 0.0, color = Color(red = 255, green = 213, blue = 0)),
        Stat(title = R.string.dropped, value = 0.0, color = Color(red = 213, green = 0, blue = 0)),
        Stat(title = R.string.ptw, value = 0.0, color = Color(red = 158, green = 158, blue = 158)),
    ))

    fun getMyUser() {
        isLoading = true
        viewModelScope.launch(Dispatchers.IO) {
            user = UserRepository.getMyUser()

            user?.animeStatistics?.let { stats ->
                val tempStatList = mutableListOf<Stat>()
                tempStatList.add(animeStats.value[0].copy(value = stats.numItemsWatching?.toDouble() ?: 0.0))
                tempStatList.add(animeStats.value[1].copy(value = stats.numItemsCompleted?.toDouble() ?: 0.0))
                tempStatList.add(animeStats.value[2].copy(value = stats.numItemsOnHold?.toDouble() ?: 0.0))
                tempStatList.add(animeStats.value[3].copy(value = stats.numItemsDropped?.toDouble() ?: 0.0))
                tempStatList.add(animeStats.value[4].copy(value = stats.numItemsPlanToWatch?.toDouble() ?: 0.0))
                animeStats.value = tempStatList
            }

            isLoading = false
        }
    }
}