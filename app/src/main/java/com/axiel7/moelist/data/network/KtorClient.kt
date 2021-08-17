package com.axiel7.moelist.data.network

import android.util.Log
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.features.*
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.*
import io.ktor.client.features.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.json.Json
import java.util.concurrent.TimeUnit

class KtorClient(private val authToken: String?) {

    val ktorHttpClient = HttpClient(OkHttp) {

        expectSuccess = false

        install(JsonFeature) {
            serializer = KotlinxSerializer(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })

            engine {
                config {
                    connectTimeout(Int.MAX_VALUE.toLong(), TimeUnit.MILLISECONDS)
                    callTimeout(Int.MAX_VALUE.toLong(), TimeUnit.MILLISECONDS)
                    readTimeout(Int.MAX_VALUE.toLong(), TimeUnit.MILLISECONDS)
                    writeTimeout(Int.MAX_VALUE.toLong(), TimeUnit.MILLISECONDS)
                    retryOnConnectionFailure(true)
                }
            }
        }

        install(HttpTimeout) {
            socketTimeoutMillis = Int.MAX_VALUE.toLong()
            connectTimeoutMillis = Int.MAX_VALUE.toLong()
            requestTimeoutMillis = Int.MAX_VALUE.toLong()
        }

        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    Log.v("Logger Ktor =>", message)
                }
            }
            level = LogLevel.ALL
        }

        install(DefaultRequest) {
            authToken?.let { header(HttpHeaders.Authorization, "Bearer $it") }
        }
    }

    companion object {
        private const val TIME_OUT = 60_000L
    }
}