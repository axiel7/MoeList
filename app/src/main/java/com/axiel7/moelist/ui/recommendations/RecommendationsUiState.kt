package com.axiel7.moelist.ui.recommendations

import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.axiel7.moelist.data.model.anime.AnimeList
import com.axiel7.moelist.ui.base.state.PagedUiState
import com.axiel7.moelist.ui.base.state.UiState

@Stable
data class RecommendationsUiState(
    val animes: SnapshotStateList<AnimeList> = mutableStateListOf(),
    override val nextPage: String? = null,
    override val loadMore: Boolean = true,
    override val isLoading: Boolean = true,
    override val message: String? = null
) : PagedUiState() {
    override fun setLoading(value: Boolean) = copy(isLoading = value)
    override fun setMessage(value: String?) = copy(message = value)
}
