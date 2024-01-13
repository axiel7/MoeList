package com.axiel7.moelist.ui.base.viewmodel

import androidx.lifecycle.ViewModel
import com.axiel7.moelist.ui.base.event.UiEvent
import com.axiel7.moelist.ui.base.state.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

abstract class BaseViewModel<S : UiState> : ViewModel(), UiEvent {

    protected abstract val mutableUiState: MutableStateFlow<S>
    val uiState: StateFlow<S> by lazy { mutableUiState.asStateFlow() }

    @Suppress("UNCHECKED_CAST")
    fun setLoading(value: Boolean) {
        mutableUiState.update { it.setLoading(value) as S }
    }

    @Suppress("UNCHECKED_CAST")
    override fun showMessage(message: String?) {
        mutableUiState.update { it.setMessage(message ?: GENERIC_ERROR) as S }
    }

    @Suppress("UNCHECKED_CAST")
    override fun onMessageDisplayed() {
        mutableUiState.update { it.setMessage(null) as S }
    }

    companion object {
        private const val GENERIC_ERROR = "Generic Error"
        const val FLOW_TIMEOUT = 5_000L
    }
}