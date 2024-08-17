package com.axiel7.moelist.ui.search

import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.axiel7.moelist.data.model.SearchHistory
import com.axiel7.moelist.data.model.media.BaseMediaList
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.ui.base.state.PagedUiState

@Stable
data class SearchUiState(
    val query: String = "",
    val mediaType: MediaType = MediaType.ANIME,
    val mediaList: SnapshotStateList<BaseMediaList> = mutableStateListOf(),
    val hideScore: Boolean = false,
    val searchHistoryList: List<SearchHistory> = emptyList(),
    val performSearch: Boolean = false,
    val noResults: Boolean = false,
    override val nextPage: String? = null,
    override val loadMore: Boolean = false,
    override val isLoading: Boolean = false,
    override val message: String? = null
) : PagedUiState() {
    override fun setLoading(value: Boolean) = copy(isLoading = value)
    override fun setMessage(value: String?) = copy(message = value)
}
