package com.axiel7.moelist.ui.ranking

import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.axiel7.moelist.data.model.media.BaseRanking
import com.axiel7.moelist.data.model.media.RankingType
import com.axiel7.moelist.ui.base.state.PagedUiState

@Stable
data class MediaRankingUiState(
    val rankingType: RankingType,
    val mediaList: SnapshotStateList<BaseRanking> = mutableStateListOf(),
    override val nextPage: String? = null,
    override val loadMore: Boolean = true,
    override val isLoading: Boolean = true,
    override val message: String? = null
) : PagedUiState() {
    override fun setLoading(value: Boolean) = copy(isLoading = value)
    override fun setMessage(value: String?) = copy(message = value)
}
