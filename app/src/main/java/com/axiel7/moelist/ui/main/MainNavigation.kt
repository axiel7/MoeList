package com.axiel7.moelist.ui.main

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.ui.base.navigation.NavActionManager
import com.axiel7.moelist.ui.base.navigation.Route
import com.axiel7.moelist.ui.base.navigation.TopLevelBackStack
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

private val topNavigationTransitionSpec = NavDisplay.transitionSpec {
    ContentTransform(
        fadeIn(animationSpec = tween()),
        fadeOut(animationSpec = tween()),
    )
} + NavDisplay.popTransitionSpec {
    ContentTransform(
        fadeIn(animationSpec = tween()),
        fadeOut(animationSpec = tween()),
    )
} + NavDisplay.predictivePopTransitionSpec {
    ContentTransform(
        fadeIn(spring(dampingRatio = 1f, stiffness = 1600f)),
        fadeOut(spring(dampingRatio = 1f, stiffness = 1600f))
    )
}

@Composable
fun MainNavigation(
    topLevelBackStack: TopLevelBackStack<NavKey>,
    navActionManager: NavActionManager,
    isLoggedIn: Boolean,
    isCompactScreen: Boolean,
    useListTabs: Boolean,
    modifier: Modifier,
    padding: PaddingValues,
    topBarHeightPx: Float,
    topBarOffsetY: Float,
    topBarAnimateTo: suspend (Float) -> Unit,
    topBarSnapTo: suspend (Float) -> Unit,
) {
    NavDisplay(
        backStack = topLevelBackStack.backStack,
        onBack = { topLevelBackStack.removeLast() },
        modifier = modifier,
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        transitionSpec = {
            // Slide in from right when navigating forward
            (slideInHorizontally(initialOffsetX = { it })) togetherWith
                    (slideOutHorizontally(targetOffsetX = { -it })
                            + fadeOut(animationSpec = tween()))
        },
        popTransitionSpec = {
            // Slide in from left when navigating back
            (slideInHorizontally(initialOffsetX = { -it }) + fadeIn()) togetherWith
                    slideOutHorizontally(targetOffsetX = { it })
        },
        predictivePopTransitionSpec = {
            // Slide in from left when navigating back
            (slideInHorizontally(initialOffsetX = { -it })
                    + fadeIn(animationSpec = tween())) togetherWith
                    (slideOutHorizontally(targetOffsetX = { it }))
        },
        entryProvider = entryProvider {
            entry<Route.Tab.Home>(
                metadata = topNavigationTransitionSpec
            ) {
                HomeView(
                    isLoggedIn = isLoggedIn,
                    navActionManager = navActionManager,
                    padding = padding,
                    topBarHeightPx = topBarHeightPx,
                    topBarOffsetY = topBarOffsetY,
                    topBarAnimateTo = topBarAnimateTo,
                    topBarSnapTo = topBarSnapTo,
                )
            }

            entry<Route.Tab.Anime>(
                metadata = topNavigationTransitionSpec
            ) {
                if (!isLoggedIn) {
                    LoginView()
                } else {
                    if (useListTabs) {
                        UserMediaListWithTabsView(
                            mediaType = MediaType.ANIME,
                            isCompactScreen = isCompactScreen,
                            navActionManager = navActionManager,
                            padding = padding
                        )
                    } else {
                        UserMediaListWithFabView(
                            mediaType = MediaType.ANIME,
                            isCompactScreen = isCompactScreen,
                            navActionManager = navActionManager,
                            topBarHeightPx = topBarHeightPx,
                            topBarOffsetY = topBarOffsetY,
                            topBarAnimateTo = topBarAnimateTo,
                            topBarSnapTo = topBarSnapTo,
                            padding = padding
                        )
                    }
                }
            }

            entry<Route.Tab.Manga>(
                metadata = topNavigationTransitionSpec
            ) {
                if (!isLoggedIn) {
                    LoginView()
                } else {
                    if (useListTabs) {
                        UserMediaListWithTabsView(
                            mediaType = MediaType.MANGA,
                            isCompactScreen = isCompactScreen,
                            navActionManager = navActionManager,
                            padding = padding
                        )
                    } else {
                        UserMediaListWithFabView(
                            mediaType = MediaType.MANGA,
                            isCompactScreen = isCompactScreen,
                            navActionManager = navActionManager,
                            topBarHeightPx = topBarHeightPx,
                            topBarOffsetY = topBarOffsetY,
                            topBarAnimateTo = topBarAnimateTo,
                            topBarSnapTo = topBarSnapTo,
                            padding = padding
                        )
                    }
                }
            }

            entry<Route.Tab.More>(
                metadata = topNavigationTransitionSpec
            ) {
                MoreView(
                    navActionManager = navActionManager,
                    padding = padding,
                    topBarHeightPx = topBarHeightPx,
                    topBarOffsetY = topBarOffsetY,
                    topBarAnimateTo = topBarAnimateTo,
                    topBarSnapTo = topBarSnapTo,
                    isLoggedIn = isLoggedIn
                )
            }

            entry<Route.MediaRanking> {
                MediaRankingView(
                    arguments = it,
                    isCompactScreen = isCompactScreen,
                    navActionManager = navActionManager,
                )
            }

            entry<Route.Calendar> {
                CalendarView(
                    navActionManager = navActionManager
                )
            }

            entry<Route.SeasonChart> {
                SeasonChartView(
                    navActionManager = navActionManager
                )
            }

            entry<Route.Recommendations> {
                RecommendationsView(
                    navActionManager = navActionManager
                )
            }

            entry<Route.Settings> {
                SettingsView(
                    navActionManager = navActionManager
                )
            }

            entry<Route.ListStyleSettings> {
                ListStyleSettingsView(
                    navActionManager = navActionManager
                )
            }

            entry<Route.Notifications> {
                NotificationsView(
                    navActionManager = navActionManager
                )
            }

            entry<Route.About> {
                AboutView(
                    navActionManager = navActionManager
                )
            }

            entry<Route.Credits> {
                CreditsView(
                    navActionManager = navActionManager
                )
            }

            entry<Route.MediaDetails> {
                MediaDetailsView(
                    arguments = it,
                    isLoggedIn = isLoggedIn,
                    navActionManager = navActionManager
                )
            }

            entry<Route.FullPoster>(
                metadata = NavDisplay.transitionSpec {
                    ContentTransform(fadeIn(), fadeOut())
                } + NavDisplay.popTransitionSpec {
                    ContentTransform(fadeIn(), fadeOut())
                },
            ) {
                FullPosterView(
                    pictures = it.pictures,
                    navActionManager = navActionManager
                )
            }

            entry<Route.Profile> {
                if (!isLoggedIn) {
                    DefaultScaffoldWithTopAppBar(
                        title = stringResource(R.string.title_profile),
                        navigateBack = { navActionManager.goBack() }
                    ) { padding ->
                        LoginView(modifier = Modifier.padding(padding))
                    }
                } else {
                    ProfileView(
                        navActionManager = navActionManager
                    )
                }
            }

            entry<Route.Search>(
                metadata = NavDisplay.transitionSpec {
                    ContentTransform(
                        expandVertically(expandFrom = Alignment.Top),
                        shrinkVertically(shrinkTowards = Alignment.Top)
                    )
                } + NavDisplay.popTransitionSpec {
                    ContentTransform(
                        expandVertically(expandFrom = Alignment.Top),
                        shrinkVertically(shrinkTowards = Alignment.Top)
                    )
                },
            ) {
                SearchHostView(
                    arguments = it,
                    isCompactScreen = isCompactScreen,
                    padding = if (isCompactScreen) PaddingValues() else padding,
                    navActionManager = navActionManager
                )
            }
        }
    )
}
