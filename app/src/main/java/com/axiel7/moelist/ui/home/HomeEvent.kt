package com.axiel7.moelist.ui.home

import com.axiel7.moelist.ui.base.event.UiEvent

interface HomeEvent : UiEvent {
    fun initRequestChain(isLoggedIn: Boolean)
}