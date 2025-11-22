package com.axiel7.moelist.ui.home

import androidx.compose.runtime.Stable
import com.axiel7.moelist.ui.base.event.UiEvent

@Stable
interface HomeEvent : UiEvent {
    fun initRequestChain(isLoggedIn: Boolean)
}