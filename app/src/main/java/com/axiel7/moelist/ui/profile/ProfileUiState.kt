package com.axiel7.moelist.ui.profile

import androidx.compose.runtime.Immutable
import com.axiel7.moelist.data.model.User
import com.axiel7.moelist.data.model.UserStats
import com.axiel7.moelist.data.model.media.ListStatus
import com.axiel7.moelist.data.model.media.Stat
import com.axiel7.moelist.ui.base.state.UiState

@Immutable
data class ProfileUiState(
    val user: User? = null,
    val profilePictureUrl: String? = null,
    val animeStats: List<Stat<ListStatus>> = emptyList(),
    val mangaStats: List<Stat<ListStatus>> = emptyList(),
    val userMangaStats: UserStats.MangaStats? = null,
    val isLoadingManga: Boolean = true,
    override val isLoading: Boolean = true,
    override val message: String? = null
) : UiState() {
    override fun setLoading(value: Boolean) = copy(isLoading = value)
    override fun setMessage(value: String?) = copy(message = value)
}
