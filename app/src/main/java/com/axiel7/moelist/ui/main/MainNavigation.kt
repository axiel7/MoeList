package com.axiel7.moelist.ui.main

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.ui.base.BottomDestination
import com.axiel7.moelist.ui.base.navigation.NavActionManager
import com.axiel7.moelist.ui.base.navigation.NavActionManager.Companion.rememberNavActionManager
import com.axiel7.moelist.ui.base.navigation.NavArgument
import com.axiel7.moelist.ui.base.navigation.NavDestination
import com.axiel7.moelist.ui.calendar.CalendarView
import com.axiel7.moelist.ui.composables.DefaultScaffoldWithTopAppBar
import com.axiel7.moelist.ui.details.MediaDetailsView
import com.axiel7.moelist.ui.fullposter.FullPosterView
import com.axiel7.moelist.ui.home.HomeView
import com.axiel7.moelist.ui.login.LoginView
import com.axiel7.moelist.ui.more.MoreView
import com.axiel7.moelist.ui.more.about.AboutView
import com.axiel7.moelist.ui.more.credits.CreditsView
import com.axiel7.moelist.ui.more.notifications.NotificationsView
import com.axiel7.moelist.ui.more.settings.SettingsView
import com.axiel7.moelist.ui.more.settings.list.ListStyleSettingsView
import com.axiel7.moelist.ui.profile.ProfileView
import com.axiel7.moelist.ui.ranking.MediaRankingView
import com.axiel7.moelist.ui.recommendations.RecommendationsView
import com.axiel7.moelist.ui.search.SearchHostView
import com.axiel7.moelist.ui.season.SeasonChartView
import com.axiel7.moelist.ui.userlist.UserMediaListWithFabView
import com.axiel7.moelist.ui.userlist.UserMediaListWithTabsView

@Composable
fun MainNavigation(
    navController: NavHostController,
    navActionManager: NavActionManager,
    lastTabOpened: Int,
    isLoggedIn: Boolean,
    isCompactScreen: Boolean,
    useListTabs: Boolean,
    modifier: Modifier,
    padding: PaddingValues,
    topBarHeightPx: Float,
    topBarOffsetY: Animatable<Float, AnimationVector1D>,
) {
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
                isLoggedIn = isLoggedIn,
                navActionManager = navActionManager,
                padding = padding,
                topBarHeightPx = topBarHeightPx,
                topBarOffsetY = topBarOffsetY,
            )
        }

        composable(
            route = BottomDestination.AnimeList.route,
            arguments = NavDestination.AnimeTab.namedNavArguments
        ) {
            if (!isLoggedIn) {
                LoginView()
            } else {
                if (useListTabs) {
                    UserMediaListWithTabsView(
                        mediaType = MediaType.ANIME,
                        isCompactScreen = isCompactScreen,
                        navActionManager = navActionManager,
                        topBarHeightPx = topBarHeightPx,
                        topBarOffsetY = topBarOffsetY,
                        padding = padding
                    )
                } else {
                    UserMediaListWithFabView(
                        mediaType = MediaType.ANIME,
                        isCompactScreen = isCompactScreen,
                        navActionManager = navActionManager,
                        topBarHeightPx = topBarHeightPx,
                        topBarOffsetY = topBarOffsetY,
                        padding = padding
                    )
                }
            }
        }

        composable(
            route = BottomDestination.MangaList.route,
            arguments = NavDestination.MangaTab.namedNavArguments
        ) {
            if (!isLoggedIn) {
                LoginView()
            } else {
                if (useListTabs) {
                    UserMediaListWithTabsView(
                        mediaType = MediaType.MANGA,
                        isCompactScreen = isCompactScreen,
                        navActionManager = navActionManager,
                        topBarHeightPx = topBarHeightPx,
                        topBarOffsetY = topBarOffsetY,
                        padding = padding
                    )
                } else {
                    UserMediaListWithFabView(
                        mediaType = MediaType.MANGA,
                        isCompactScreen = isCompactScreen,
                        navActionManager = navActionManager,
                        topBarHeightPx = topBarHeightPx,
                        topBarOffsetY = topBarOffsetY,
                        padding = padding
                    )
                }
            }
        }

        composable(BottomDestination.More.route) {
            MoreView(
                navActionManager = navActionManager,
                padding = padding,
                topBarHeightPx = topBarHeightPx,
                topBarOffsetY = topBarOffsetY,
            )
        }

        composable(
            route = NavDestination.MediaRanking.route(),
            arguments = NavDestination.MediaRanking.namedNavArguments
        ) { navEntry ->
            MediaRankingView(
                mediaType = MediaType.valueOf(
                    navEntry.arguments?.getString(NavArgument.MediaType.name)
                        ?: MediaType.ANIME.name
                ),
                isCompactScreen = isCompactScreen,
                navActionManager = navActionManager,
            )
        }

        composable(NavDestination.Calendar.route()) {
            CalendarView(
                navActionManager = navActionManager
            )
        }

        composable(NavDestination.SeasonChart.route()) {
            SeasonChartView(
                navActionManager = navActionManager
            )
        }

        composable(NavDestination.Recommendations.route()) {
            RecommendationsView(
                navActionManager = navActionManager
            )
        }

        composable(NavDestination.Settings.route()) {
            SettingsView(
                navActionManager = navActionManager
            )
        }

        composable(NavDestination.ListStyleSettings.route()) {
            ListStyleSettingsView(
                navActionManager = navActionManager
            )
        }

        composable(NavDestination.Notifications.route()) {
            NotificationsView(
                navActionManager = navActionManager
            )
        }

        composable(NavDestination.About.route()) {
            AboutView(
                navActionManager = navActionManager
            )
        }

        composable(NavDestination.Credits.route()) {
            CreditsView(
                navActionManager = navActionManager
            )
        }

        composable(
            route = NavDestination.MediaDetails.route(),
            arguments = NavDestination.MediaDetails.namedNavArguments
        ) {
            MediaDetailsView(
                isLoggedIn = isLoggedIn,
                navActionManager = navActionManager
            )
        }

        composable(
            route = NavDestination.FullPoster.route(),
            arguments = NavDestination.FullPoster.namedNavArguments
        ) { navEntry ->
            FullPosterView(
                pictures = navEntry.arguments?.getStringArray(NavArgument.Pictures.name)
                    ?: emptyArray(),
                navActionManager = navActionManager
            )
        }

        composable(NavDestination.Profile.route()) {
            if (!isLoggedIn) {
                DefaultScaffoldWithTopAppBar(
                    title = stringResource(R.string.title_profile),
                    navigateBack = { navController.popBackStack() }
                ) { padding ->
                    LoginView(modifier = Modifier.padding(padding))
                }
            } else {
                ProfileView(
                    navActionManager = navActionManager
                )
            }
        }

        composable(NavDestination.Search.route()) {
            SearchHostView(
                isCompactScreen = isCompactScreen,
                padding = if (isCompactScreen) PaddingValues() else padding,
                navActionManager = navActionManager
            )
        }
    }//:NavHost
}
