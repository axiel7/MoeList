package com.axiel7.moelist.ui.more.notifications

import com.axiel7.moelist.ui.base.event.UiEvent

interface NotificationsEvent : UiEvent {
    fun removeNotification(animeId: Int)
    fun removeAllNotifications()
}