package com.axiel7.moelist.data.network

import com.axiel7.moelist.data.model.Response
import com.axiel7.moelist.data.model.UserStats
import com.axiel7.moelist.utils.Constants.JIKAN_API_URL
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders

class JikanApi(private val client: HttpClient) {

    suspend fun getUserStats(
        username: String
    ): Response<UserStats> = client.get("${JIKAN_API_URL}users/$username/statistics") {
        header(HttpHeaders.Authorization, "")
    }.body()
}