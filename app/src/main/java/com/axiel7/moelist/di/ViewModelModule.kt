package com.axiel7.moelist.di

import com.axiel7.moelist.uicompose.calendar.CalendarViewModel
import com.axiel7.moelist.uicompose.details.MediaDetailsViewModel
import com.axiel7.moelist.uicompose.editmedia.EditMediaViewModel
import com.axiel7.moelist.uicompose.home.HomeViewModel
import com.axiel7.moelist.uicompose.main.MainViewModel
import com.axiel7.moelist.uicompose.more.MoreViewModel
import com.axiel7.moelist.uicompose.more.notifications.NotificationsViewModel
import com.axiel7.moelist.uicompose.more.settings.ListStyleSettingsViewModel
import com.axiel7.moelist.uicompose.more.settings.SettingsViewModel
import com.axiel7.moelist.uicompose.profile.ProfileViewModel
import com.axiel7.moelist.uicompose.ranking.MediaRankingViewModel
import com.axiel7.moelist.uicompose.recommendations.RecommendationsViewModel
import com.axiel7.moelist.uicompose.search.SearchViewModel
import com.axiel7.moelist.uicompose.season.SeasonChartViewModel
import com.axiel7.moelist.uicompose.userlist.UserMediaListViewModel
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
    viewModelOf(::MediaRankingViewModel)
    viewModelOf(::RecommendationsViewModel)
    viewModelOf(::SearchViewModel)
    viewModelOf(::SeasonChartViewModel)
    viewModel { params ->
        UserMediaListViewModel(
            initialListStatus = params.getOrNull(),
            savedStateHandle = get(),
            animeRepository = get(),
            mangaRepository = get(),
            defaultPreferencesRepository = get()
        )
    }
    viewModelOf(::MoreViewModel)
}