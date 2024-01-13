package com.axiel7.moelist.di

import com.axiel7.moelist.ui.calendar.CalendarViewModel
import com.axiel7.moelist.ui.details.MediaDetailsViewModel
import com.axiel7.moelist.ui.editmedia.EditMediaViewModel
import com.axiel7.moelist.ui.home.HomeViewModel
import com.axiel7.moelist.ui.main.MainViewModel
import com.axiel7.moelist.ui.more.MoreViewModel
import com.axiel7.moelist.ui.more.notifications.NotificationsViewModel
import com.axiel7.moelist.ui.more.settings.SettingsViewModel
import com.axiel7.moelist.ui.more.settings.list.ListStyleSettingsViewModel
import com.axiel7.moelist.ui.profile.ProfileViewModel
import com.axiel7.moelist.ui.ranking.MediaRankingViewModel
import com.axiel7.moelist.ui.recommendations.RecommendationsViewModel
import com.axiel7.moelist.ui.search.SearchViewModel
import com.axiel7.moelist.ui.season.SeasonChartViewModel
import com.axiel7.moelist.ui.userlist.UserMediaListViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

val viewModelModule = module {
    viewModelOf(::SettingsViewModel)
    viewModelOf(::CalendarViewModel)
    viewModelOf(::MediaDetailsViewModel)
    viewModelOf(::EditMediaViewModel)
    viewModelOf(::HomeViewModel)
    viewModelOf(::MainViewModel)
    viewModelOf(::ListStyleSettingsViewModel)
    viewModel {
        NotificationsViewModel(
            dataStore = get(named(NOTIFICATIONS_DATA_STORE)),
            notificationWorkerManager = get()
        )
    }
    viewModelOf(::ProfileViewModel)
    viewModel { parameters ->
        MediaRankingViewModel(
            rankingType = parameters.get(),
            savedStateHandle = get(),
            animeRepository = get(),
            mangaRepository = get()
        )
    }
    viewModelOf(::RecommendationsViewModel)
    viewModelOf(::SearchViewModel)
    viewModelOf(::SeasonChartViewModel)
    viewModel { params ->
        UserMediaListViewModel(
            mediaType = params.get(),
            initialListStatus = params.getOrNull(),
            animeRepository = get(),
            mangaRepository = get(),
            defaultPreferencesRepository = get()
        )
    }
    viewModelOf(::MoreViewModel)
}