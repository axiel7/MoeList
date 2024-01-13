package com.axiel7.moelist.ui.base.state

abstract class PagedUiState : UiState() {

    abstract val nextPage: String?

    /**
     * Trigger variable to load more items, be careful to set it to false after loading more
     */
    abstract val loadMore: Boolean

    val canLoadMore get() = nextPage != null && !isLoading
}