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
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.ui.base.BottomDestination
import com.axiel7.moelist.ui.base.navigation.NavActionManager
import com.axiel7.moelist.ui.base.navigation.Route
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
import com.uragiristereo.serializednavigationextension.navigation.compose.composable

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
        composable<Route.Tab.Home> {
            HomeView(
                isLoggedIn = isLoggedIn,
                navActionManager = navActionManager,
                padding = padding,
                topBarHeightPx = topBarHeightPx,
                topBarOffsetY = topBarOffsetY,
            )
        }

        composable<Route.Tab.Anime> {
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

        composable<Route.Tab.Manga> {
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

        composable<Route.Tab.More> {
            MoreView(
                navActionManager = navActionManager,
                padding = padding,
                topBarHeightPx = topBarHeightPx,
                topBarOffsetY = topBarOffsetY,
            )
        }

        composable<Route.MediaRanking> {
            val args = rememberNavArgsOf()

            MediaRankingView(
                mediaType = args?.mediaType ?: MediaType.ANIME,
                isCompactScreen = isCompactScreen,
                navActionManager = navActionManager,
            )
        }

        composable<Route.Calendar> {
            CalendarView(
                navActionManager = navActionManager
            )
        }

        composable<Route.SeasonChart> {
            SeasonChartView(
                navActionManager = navActionManager
            )
        }

        composable<Route.Recommendations> {
            RecommendationsView(
                navActionManager = navActionManager
            )
        }

        composable<Route.Settings> {
            SettingsView(
                navActionManager = navActionManager
            )
        }

        composable<Route.ListStyleSettings> {
            ListStyleSettingsView(
                navActionManager = navActionManager
            )
        }

        composable<Route.Notifications> {
            NotificationsView(
                navActionManager = navActionManager
            )
        }

        composable<Route.About> {
            AboutView(
                navActionManager = navActionManager
            )
        }

        composable<Route.Credits> {
            CreditsView(
                navActionManager = navActionManager
            )
        }

        composable<Route.MediaDetails> {
            MediaDetailsView(
                isLoggedIn = isLoggedIn,
                navActionManager = navActionManager
            )
        }

        composable<Route.FullPoster> {
            val args = rememberNavArgsOf()

            FullPosterView(
                pictures = args?.pictures ?: emptyList(),
                navActionManager = navActionManager
            )
        }

        composable<Route.Profile> {
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

        composable<Route.Search> {
            SearchHostView(
                isCompactScreen = isCompactScreen,
                padding = if (isCompactScreen) PaddingValues() else padding,
                navActionManager = navActionManager
            )
        }
    }//:NavHost
}
