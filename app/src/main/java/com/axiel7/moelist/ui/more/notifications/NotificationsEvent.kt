package com.axiel7.moelist.ui.more.notifications

import androidx.compose.runtime.Stable
import com.axiel7.moelist.ui.base.event.UiEvent

@Stable
interface NotificationsEvent : UiEvent {
    fun removeNotification(animeId: Int)
    fun removeAllNotifications()
}