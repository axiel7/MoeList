package com.axiel7.moelist.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axiel7.moelist.App
import com.axiel7.moelist.data.model.User
import com.axiel7.moelist.utils.Constants.ERROR_SERVER
import com.axiel7.moelist.utils.Constants.RESPONSE_ERROR
import com.axiel7.moelist.utils.Constants.RESPONSE_NONE
import com.axiel7.moelist.utils.Constants.RESPONSE_OK
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    private val _response = MutableStateFlow(RESPONSE_NONE to "")
    val response: StateFlow<Pair<String, String>> = _response

    fun getUser(userId: Int? = null) {
        viewModelScope.launch {
            userId?.let { _user.value = App.animeDb.userDao().getUserById(it) }

            val call = async { App.api.getUser(FIELDS) }
            val result = try {
                call.await()
            } catch (e: Exception) {
                null
            }

            when {
                result == null -> _response.value = RESPONSE_ERROR to ERROR_SERVER
                !result.error.isNullOrEmpty() -> _response.value = RESPONSE_ERROR to "${result.error}: ${result.message}"
                !result.message.isNullOrEmpty() -> _response.value = RESPONSE_ERROR to "${result.error}: ${result.message}"
                else -> {
                    _response.value = RESPONSE_OK to ""
                    _user.value = result
                    App.animeDb.userDao().insertUser(result)
                }
            }
        }
    }

    companion object {
        private const val FIELDS = "id,name,gender,location,joined_at,anime_statistics"
    }
}