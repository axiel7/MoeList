package com.axiel7.moelist.di

import com.axiel7.moelist.data.network.Api
import com.axiel7.moelist.data.network.JikanApi
import com.axiel7.moelist.data.network.ktorHttpClient
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val networkModule = module {
    single { ktorHttpClient }
    singleOf(::Api)
    singleOf(::JikanApi)
}