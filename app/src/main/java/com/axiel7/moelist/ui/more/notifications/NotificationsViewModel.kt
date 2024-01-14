package com.axiel7.moelist.ui.more.notifications

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.viewModelScope
import com.axiel7.moelist.ui.base.viewmodel.BaseViewModel
import com.axiel7.moelist.worker.NotificationWorkerManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NotificationsViewModel(
    dataStore: DataStore<Preferences>,
    private val notificationWorkerManager: NotificationWorkerManager,
) : BaseViewModel<NotificationsUiState>(), NotificationsEvent {

    override val mutableUiState = MutableStateFlow(NotificationsUiState())

    override fun removeNotification(animeId: Int) {
        viewModelScope.launch {
            notificationWorkerManager.removeAiringAnimeNotification(animeId)
        }
    }

    override fun removeAllNotifications() {
        viewModelScope.launch {
            notificationWorkerManager.removeAllNotifications()
        }
    }

    init {
        dataStore.data
            .onEach { notifications ->
                mutableUiState.update { it.copy(notifications = notifications) }
            }
            .launchIn(viewModelScope)
    }
}