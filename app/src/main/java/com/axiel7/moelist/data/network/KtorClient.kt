package com.axiel7.moelist.data.network

import com.axiel7.moelist.App
import com.axiel7.moelist.BuildConfig
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.cache.HttpCache
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.ANDROID
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

val ktorHttpClient = HttpClient(OkHttp) {

    expectSuccess = false

    install(ContentNegotiation) {
        json(
            Json {
                coerceInputValues = true
                isLenient = true
                ignoreUnknownKeys = true
            }
        )
    }

    install(HttpCache)

    if (BuildConfig.IS_DEBUG) {
        install(Logging) {
            logger = Logger.ANDROID
            level = LogLevel.ALL
        }
    }

    install(DefaultRequest) {
        header("X-MAL-CLIENT-ID", BuildConfig.CLIENT_ID)
        App.accessToken?.let { header(HttpHeaders.Authorization, "Bearer $it") }
    }
}