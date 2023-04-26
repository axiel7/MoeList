package com.axiel7.moelist.data.network

import android.util.Log
import com.axiel7.moelist.BuildConfig
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import java.util.concurrent.TimeUnit

class KtorClient(private val authToken: String?) {

    val ktorHttpClient = HttpClient(OkHttp) {

        expectSuccess = false

        install(ContentNegotiation) {
            json(Json {
                coerceInputValues = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }

        engine {
            config {
                connectTimeout(Int.MAX_VALUE.toLong(), TimeUnit.MILLISECONDS)
                callTimeout(Int.MAX_VALUE.toLong(), TimeUnit.MILLISECONDS)
                readTimeout(Int.MAX_VALUE.toLong(), TimeUnit.MILLISECONDS)
                writeTimeout(Int.MAX_VALUE.toLong(), TimeUnit.MILLISECONDS)
                retryOnConnectionFailure(true)
            }
        }

        install(HttpTimeout) {
            socketTimeoutMillis = Int.MAX_VALUE.toLong()
            connectTimeoutMillis = Int.MAX_VALUE.toLong()
            requestTimeoutMillis = Int.MAX_VALUE.toLong()
        }

        if (BuildConfig.IS_DEBUG) {
            install(Logging) {
                logger = object: Logger {
                    override fun log(message: String) {
                        Log.v("Logger Ktor =>", message)
                    }
                }
                level = LogLevel.ALL
            }
        }

        install(DefaultRequest) {
            authToken?.let { header(HttpHeaders.Authorization, "Bearer $it") }
        }
    }

    companion object {
        private const val TIME_OUT = 60_000L
    }
}