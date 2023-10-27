package com.axiel7.moelist.di

import com.axiel7.moelist.data.repository.AnimeRepository
import com.axiel7.moelist.data.repository.DefaultPreferencesRepository
import com.axiel7.moelist.data.repository.LoginRepository
import com.axiel7.moelist.data.repository.MangaRepository
import com.axiel7.moelist.data.repository.UserRepository
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

val repositoryModule = module {
    single { DefaultPreferencesRepository(get(named(DEFAULT_DATA_STORE))) }
    singleOf(::AnimeRepository)
    singleOf(::MangaRepository)
    singleOf(::LoginRepository)
    singleOf(::UserRepository)
}