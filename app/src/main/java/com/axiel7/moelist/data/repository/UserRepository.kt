package com.axiel7.moelist.data.repository

import com.axiel7.moelist.App
import com.axiel7.moelist.data.model.User

object UserRepository {

    private const val USER_FIELDS = "id,name,gender,location,joined_at,anime_statistics"

    suspend fun getMyUser(): User? {
        return try {
            val result = App.api.getUser(USER_FIELDS)
            result.error?.let { BaseRepository.handleResponseError(it) }
            return result
        } catch (e: Exception) {
            null
        }
    }
}