package com.axiel7.moelist.data.repository

import com.axiel7.moelist.utils.SharedPrefsHelpers

object BaseRepository {
    fun handleResponseError(error: String) {
        when (error) {
            "invalid_token" -> {
                SharedPrefsHelpers.instance?.deleteValue("access_token")
            }
        }
    }
}