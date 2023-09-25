package com.axiel7.moelist.uicompose.main

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.axiel7.moelist.App
import com.axiel7.moelist.R
import com.axiel7.moelist.data.datastore.PreferencesDataStore
import com.axiel7.moelist.data.datastore.PreferencesDataStore.ACCESS_TOKEN_PREFERENCE_KEY
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.uicompose.base.BottomDestination
import com.axiel7.moelist.uicompose.base.StringArrayNavType
import com.axiel7.moelist.uicompose.calendar.CALENDAR_DESTINATION
import com.axiel7.moelist.uicompose.calendar.CalendarView
import com.axiel7.moelist.uicompose.composables.DefaultScaffoldWithTopAppBar
import com.axiel7.moelist.uicompose.details.FULL_POSTER_DESTINATION
import com.axiel7.moelist.uicompose.details.FullPosterView
import com.axiel7.moelist.uicompose.details.MEDIA_DETAILS_DESTINATION
import com.axiel7.moelist.uicompose.details.MEDIA_ID_ARGUMENT
import com.axiel7.moelist.uicompose.details.MEDIA_TYPE_ARGUMENT
import com.axiel7.moelist.uicompose.details.MediaDetailsView
import com.axiel7.moelist.uicompose.details.PICTURES_ARGUMENT
import com.axiel7.moelist.uicompose.home.HomeView
import com.axiel7.moelist.uicompose.login.LoginView
import com.axiel7.moelist.uicompose.more.ABOUT_DESTINATION
import com.axiel7.moelist.uicompose.more.AboutView
import com.axiel7.moelist.uicompose.more.CREDITS_DESTINATION
import com.axiel7.moelist.uicompose.more.CreditsView
import com.axiel7.moelist.uicompose.more.LIST_STYLE_SETTINGS_DESTINATION
import com.axiel7.moelist.uicompose.more.ListStyleSettingsView
import com.axiel7.moelist.uicompose.more.MORE_DESTINATION
import com.axiel7.moelist.uicompose.more.MoreView
import com.axiel7.moelist.uicompose.more.NOTIFICATIONS_DESTINATION
import com.axiel7.moelist.uicompose.more.NotificationsView
import com.axiel7.moelist.uicompose.more.SETTINGS_DESTINATION
import com.axiel7.moelist.uicompose.more.SettingsView
import com.axiel7.moelist.uicompose.profile.PROFILE_DESTINATION
import com.axiel7.moelist.uicompose.profile.ProfileView
import com.axiel7.moelist.uicompose.ranking.MEDIA_RANKING_DESTINATION
import com.axiel7.moelist.uicompose.ranking.MediaRankingView
import com.axiel7.moelist.uicompose.search.SEARCH_DESTINATION
import com.axiel7.moelist.uicompose.search.SearchHostView
import com.axiel7.moelist.uicompose.season.SEASON_CHART_DESTINATION
import com.axiel7.moelist.uicompose.season.SeasonChartView
import com.axiel7.moelist.uicompose.userlist.UserMediaListWithFabView
import com.axiel7.moelist.uicompose.userlist.UserMediaListWithTabsView
import com.axiel7.moelist.utils.StringExtensions.removeFirstAndLast

@Composable
fun MainNavigation(
    navController: NavHostController,
    lastTabOpened: Int,
    isCompactScreen: Boolean,
    modifier: Modifier,
    padding: PaddingValues,
    topBarHeightPx: Float,
    topBarOffsetY: Animatable<Float, AnimationVector1D>,
) {
    val accessTokenPreference by PreferencesDataStore.rememberPreference(
        ACCESS_TOKEN_PREFERENCE_KEY,
        App.accessToken ?: ""
    )
    val stringArrayType = remember { StringArrayNavType() }

    NavHost(
        navController = navController,
        startDestination = BottomDestination.values
            .getOrElse(lastTabOpened) { BottomDestination.Home }.route,
        modifier = modifier,
        enterTransition = { fadeIn(tween(400)) },
        exitTransition = { fadeOut(tween(400)) },
        popEnterTransition = { fadeIn(tween(400)) },
        popExitTransition = { fadeOut(tween(400)) }
    ) {
        composable(BottomDestination.Home.route) {
            HomeView(
                isLoggedIn = accessTokenPreference.isNotEmpty(),
                navigateToMediaDetails = { mediaType, mediaId ->
                    navController.navigate(
                        MEDIA_DETAILS_DESTINATION
                            .replace(MEDIA_TYPE_ARGUMENT, mediaType.name)
                            .replace(MEDIA_ID_ARGUMENT, mediaId.toString())
                    )
                },
                navigateToRanking = { mediaType ->
                    navController.navigate(
                        MEDIA_RANKING_DESTINATION
                            .replace(MEDIA_TYPE_ARGUMENT, mediaType.name)
                    )
                },
                navigateToSeasonChart = {
                    navController.navigate(SEASON_CHART_DESTINATION)
                },
                navigateToCalendar = {
                    navController.navigate(CALENDAR_DESTINATION)
                },
                padding = padding,
                topBarHeightPx = topBarHeightPx,
                topBarOffsetY = topBarOffsetY,
            )
        }

        composable(
            MEDIA_RANKING_DESTINATION,
            arguments = listOf(navArgument(MEDIA_TYPE_ARGUMENT.removeFirstAndLast()) {
                type = NavType.StringType
            })
        ) { navEntry ->
            MediaRankingView(
                mediaType = MediaType.valueOf(
                    navEntry.arguments?.getString(MEDIA_TYPE_ARGUMENT.removeFirstAndLast())
                        ?: MediaType.ANIME.name
                ),
                isCompactScreen = isCompactScreen,
                navigateBack = {
                    navController.popBackStack()
                },
                navigateToMediaDetails = { mediaType, mediaId ->
                    navController.navigate(
                        MEDIA_DETAILS_DESTINATION
                            .replace(MEDIA_TYPE_ARGUMENT, mediaType.name)
                            .replace(MEDIA_ID_ARGUMENT, mediaId.toString())
                    )
                }
            )
        }

        composable(CALENDAR_DESTINATION) {
            CalendarView(
                navigateBack = {
                    navController.popBackStack()
                },
                navigateToMediaDetails = { mediaType, mediaId ->
                    navController.navigate(
                        MEDIA_DETAILS_DESTINATION
                            .replace(MEDIA_TYPE_ARGUMENT, mediaType.name)
                            .replace(MEDIA_ID_ARGUMENT, mediaId.toString())
                    )
                }
            )
        }

        composable(SEASON_CHART_DESTINATION) {
            SeasonChartView(
                navigateBack = {
                    navController.popBackStack()
                },
                navigateToMediaDetails = { mediaType, mediaId ->
                    navController.navigate(
                        MEDIA_DETAILS_DESTINATION
                            .replace(MEDIA_TYPE_ARGUMENT, mediaType.name)
                            .replace(MEDIA_ID_ARGUMENT, mediaId.toString())
                    )
                }
            )
        }

        composable(BottomDestination.AnimeList.route) {
            if (accessTokenPreference.isEmpty()) {
                LoginView()
            } else {
                if (App.useListTabs)
                    UserMediaListWithTabsView(
                        mediaType = MediaType.ANIME,
                        isCompactScreen = isCompactScreen,
                        navigateToMediaDetails = { mediaType, mediaId ->
                            navController.navigate(
                                MEDIA_DETAILS_DESTINATION
                                    .replace(MEDIA_TYPE_ARGUMENT, mediaType.name)
                                    .replace(MEDIA_ID_ARGUMENT, mediaId.toString())
                            )
                        },
                        topBarHeightPx = topBarHeightPx,
                        topBarOffsetY = topBarOffsetY,
                        padding = padding
                    )
                else UserMediaListWithFabView(
                    mediaType = MediaType.ANIME,
                    isCompactScreen = isCompactScreen,
                    navigateToMediaDetails = { mediaType, mediaId ->
                        navController.navigate(
                            MEDIA_DETAILS_DESTINATION
                                .replace(MEDIA_TYPE_ARGUMENT, mediaType.name)
                                .replace(MEDIA_ID_ARGUMENT, mediaId.toString())
                        )
                    },
                    topBarHeightPx = topBarHeightPx,
                    topBarOffsetY = topBarOffsetY,
                    padding = padding
                )
            }
        }

        composable(BottomDestination.MangaList.route) {
            if (accessTokenPreference.isEmpty()) {
                LoginView()
            } else {
                if (App.useListTabs)
                    UserMediaListWithTabsView(
                        mediaType = MediaType.MANGA,
                        isCompactScreen = isCompactScreen,
                        navigateToMediaDetails = { mediaType, mediaId ->
                            navController.navigate(
                                MEDIA_DETAILS_DESTINATION
                                    .replace(MEDIA_TYPE_ARGUMENT, mediaType.name)
                                    .replace(MEDIA_ID_ARGUMENT, mediaId.toString())
                            )
                        },
                        topBarHeightPx = topBarHeightPx,
                        topBarOffsetY = topBarOffsetY,
                        padding = padding
                    )
                else UserMediaListWithFabView(
                    mediaType = MediaType.MANGA,
                    isCompactScreen = isCompactScreen,
                    navigateToMediaDetails = { mediaType, mediaId ->
                        navController.navigate(
                            MEDIA_DETAILS_DESTINATION
                                .replace(MEDIA_TYPE_ARGUMENT, mediaType.name)
                                .replace(MEDIA_ID_ARGUMENT, mediaId.toString())
                        )
                    },
                    topBarHeightPx = topBarHeightPx,
                    topBarOffsetY = topBarOffsetY,
                    padding = padding
                )
            }
        }

        navigation(startDestination = MORE_DESTINATION, route = BottomDestination.More.route) {
            composable(MORE_DESTINATION) {
                MoreView(
                    navigateToSettings = {
                        navController.navigate(SETTINGS_DESTINATION)
                    },
                    navigateToNotifications = {
                        navController.navigate(NOTIFICATIONS_DESTINATION)
                    },
                    navigateToAbout = {
                        navController.navigate(ABOUT_DESTINATION)
                    },
                    padding = padding,
                    topBarHeightPx = topBarHeightPx,
                    topBarOffsetY = topBarOffsetY,
                )
            }
            composable(SETTINGS_DESTINATION) {
                SettingsView(
                    navigateToListStyleSettings = {
                        navController.navigate(LIST_STYLE_SETTINGS_DESTINATION)
                    },
                    navigateBack = {
                        navController.popBackStack()
                    }
                )
            }
            composable(LIST_STYLE_SETTINGS_DESTINATION) {
                ListStyleSettingsView(
                    navigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(NOTIFICATIONS_DESTINATION) {
                NotificationsView(
                    navigateBack = {
                        navController.popBackStack()
                    },
                    navigateToMediaDetails = { mediaType, mediaId ->
                        navController.navigate(
                            MEDIA_DETAILS_DESTINATION
                                .replace(MEDIA_TYPE_ARGUMENT, mediaType.name)
                                .replace(MEDIA_ID_ARGUMENT, mediaId.toString())
                        )
                    }
                )
            }
            composable(ABOUT_DESTINATION) {
                AboutView(
                    navigateBack = {
                        navController.popBackStack()
                    },
                    navigateToCredits = {
                        navController.navigate(CREDITS_DESTINATION)
                    }
                )
            }
            composable(CREDITS_DESTINATION) {
                CreditsView(
                    navigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }

        composable(
            MEDIA_DETAILS_DESTINATION,
            arguments = listOf(
                navArgument(MEDIA_TYPE_ARGUMENT.removeFirstAndLast()) { type = NavType.StringType },
                navArgument(MEDIA_ID_ARGUMENT.removeFirstAndLast()) { type = NavType.IntType }
            )
        ) { navEntry ->
            MediaDetailsView(
                mediaType = navEntry.arguments?.getString(MEDIA_TYPE_ARGUMENT.removeFirstAndLast())
                    ?.let { mediaType -> MediaType.valueOf(mediaType) } ?: MediaType.ANIME,
                mediaId = navEntry.arguments?.getInt(MEDIA_ID_ARGUMENT.removeFirstAndLast()) ?: 0,
                isLoggedIn = accessTokenPreference.isNotEmpty(),
                navigateBack = {
                    navController.popBackStack()
                },
                navigateToMediaDetails = { mediaType, mediaId ->
                    navController.navigate(
                        MEDIA_DETAILS_DESTINATION
                            .replace(MEDIA_TYPE_ARGUMENT, mediaType.name)
                            .replace(MEDIA_ID_ARGUMENT, mediaId.toString())
                    )
                },
                navigateToFullPoster = { pictures ->
                    navController.navigate(
                        FULL_POSTER_DESTINATION
                            .replace(PICTURES_ARGUMENT, pictures)
                    )
                }
            )
        }

        composable(
            FULL_POSTER_DESTINATION,
            arguments = listOf(
                navArgument(PICTURES_ARGUMENT.removeFirstAndLast()) { type = stringArrayType }
            )
        ) { navEntry ->
            FullPosterView(
                pictures = navEntry.arguments?.getStringArray(PICTURES_ARGUMENT.removeFirstAndLast())
                    ?: emptyArray(),
                navigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(PROFILE_DESTINATION) {
            if (accessTokenPreference.isEmpty()) {
                DefaultScaffoldWithTopAppBar(
                    title = stringResource(R.string.title_profile),
                    navigateBack = { navController.popBackStack() }
                ) { padding ->
                    LoginView(modifier = Modifier.padding(padding))
                }
            } else {
                ProfileView(
                    navigateBack = {
                        navController.popBackStack()
                    },
                    navigateToFullPoster = { pictures ->
                        navController.navigate(
                            FULL_POSTER_DESTINATION
                                .replace(PICTURES_ARGUMENT, pictures)
                        )
                    }
                )
            }
        }

        composable(SEARCH_DESTINATION) {
            SearchHostView(
                isCompactScreen = isCompactScreen,
                padding = if (isCompactScreen) PaddingValues() else padding,
                navigateBack = {
                    navController.popBackStack()
                },
                navigateToMediaDetails = { mediaType, mediaId ->
                    navController.navigate(
                        MEDIA_DETAILS_DESTINATION
                            .replace(MEDIA_TYPE_ARGUMENT, mediaType.name)
                            .replace(MEDIA_ID_ARGUMENT, mediaId.toString())
                    )
                },
            )
        }
    }//:NavHost
}
