package com.axiel7.moelist.ui.main

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MainViewModel : ViewModel() {

    private val _selectedId = MutableStateFlow<Int?>(null)
    val selectedId: StateFlow<Int?> = _selectedId

    fun selectId(id: Int) {
        _selectedId.value = id
    }
}