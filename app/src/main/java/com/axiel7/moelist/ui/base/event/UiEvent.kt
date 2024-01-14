package com.axiel7.moelist.ui.base.event

interface UiEvent {
    fun showMessage(message: String?)
    fun onMessageDisplayed()
}