package com.axiel7.moelist.ui.base.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.ui.base.BottomDestination

@Immutable
class NavActionManager(
    private val backStack: TopLevelBackStack<NavKey>
) {
    fun goBack() {
        backStack.removeLast()
    }

    fun toMediaRanking(mediaType: MediaType) {
        backStack.add(Route.MediaRanking(mediaType))
    }

    fun toMediaDetails(mediaType: MediaType, id: Int) {
        backStack.add(
            Route.MediaDetails(
                mediaType = mediaType,
                mediaId = id,
            )
        )
    }

    fun toCalendar() {
        backStack.add(Route.Calendar)
    }

    fun toSeasonChart() {
        backStack.add(Route.SeasonChart)
    }

    fun toRecommendations() {
        backStack.add(Route.Recommendations)
    }

    fun toFullPoster(pictures: List<String>) {
        backStack.add(Route.FullPoster(pictures))
    }

    fun toSettings() {
        backStack.add(Route.Settings)
    }

    fun toListStyleSettings() {
        backStack.add(Route.ListStyleSettings)
    }

    fun toNotifications() {
        backStack.add(Route.Notifications)
    }

    fun toAbout() {
        backStack.add(Route.About)
    }

    fun toCredits() {
        backStack.add(Route.Credits)
    }

    fun toSearch(mediaType: MediaType) {
        backStack.add(Route.Search(mediaType))
    }

    fun toProfile() {
        backStack.add(Route.Profile)
    }

    companion object {
        @Composable
        fun rememberNavActionManager(
            backStack: TopLevelBackStack<NavKey> = TopLevelBackStack(
                startKey = BottomDestination.Home.route,
                backStack = rememberNavBackStack()
            )
        ) = remember {
            NavActionManager(backStack)
        }
    }
}
