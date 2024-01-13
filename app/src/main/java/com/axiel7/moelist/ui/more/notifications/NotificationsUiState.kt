package com.axiel7.moelist.ui.more.notifications

import androidx.compose.runtime.Immutable
import androidx.datastore.preferences.core.Preferences
import com.axiel7.moelist.ui.base.state.UiState

@Immutable
data class NotificationsUiState(
    val notifications: Preferences? = null, //TODO: migrate to room
    override val isLoading: Boolean = false,
    override val message: String? = null
) : UiState() {
    override fun setLoading(value: Boolean) = copy(isLoading = value)
    override fun setMessage(value: String?) = copy(message = value)
}
