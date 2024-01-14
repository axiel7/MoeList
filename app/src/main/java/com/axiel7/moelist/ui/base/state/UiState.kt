package com.axiel7.moelist.ui.base.state

abstract class UiState {
    abstract val isLoading: Boolean
    abstract val message: String?

    // These methods are required because we can't have an abstract data class
    // so we need to manually implement the copy() method

    /**
     * copy(isLoading = value)
     */
    abstract fun setLoading(value: Boolean): UiState

    /**
     * copy(message = value)
     */
    abstract fun setMessage(value: String?): UiState
}