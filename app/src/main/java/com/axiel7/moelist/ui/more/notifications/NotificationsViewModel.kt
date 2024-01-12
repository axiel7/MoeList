package com.axiel7.moelist.ui.more.notifications

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axiel7.moelist.worker.NotificationWorkerManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NotificationsViewModel(
    dataStore: DataStore<Preferences>,
    private val notificationWorkerManager: NotificationWorkerManager,
) : ViewModel() {

    val notifications = dataStore.data
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun removeNotification(animeId: Int) = viewModelScope.launch {
        notificationWorkerManager.removeAiringAnimeNotification(animeId)
    }

    fun removeAllNotifications() = viewModelScope.launch {
        notificationWorkerManager.removeAllNotifications()
    }
}