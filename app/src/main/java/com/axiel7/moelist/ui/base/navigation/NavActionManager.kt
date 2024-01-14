package com.axiel7.moelist.ui.base.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.axiel7.moelist.data.model.media.MediaType
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Immutable
class NavActionManager(
    private val navController: NavHostController
) {
    fun goBack() {
        navController.popBackStack()
    }

    fun toMediaRanking(mediaType: MediaType) {
        navController.navigate(
            NavDestination.MediaRanking
                .putArguments(mapOf(NavArgument.MediaType to mediaType.name))
        )
    }

    fun toMediaDetails(mediaType: MediaType, id: Int) {
        navController.navigate(
            NavDestination.MediaDetails
                .putArguments(mapOf(
                    NavArgument.MediaType to mediaType.name,
                    NavArgument.MediaId to id.toString()
                ))
        )
    }

    fun toCalendar() {
        navController.navigate(NavDestination.Calendar.route())
    }

    fun toSeasonChart() {
        navController.navigate(NavDestination.SeasonChart.route())
    }

    fun toRecommendations() {
        navController.navigate(NavDestination.Recommendations.route())
    }

    fun toProfile() {
        navController.navigate(NavDestination.Profile.route())
    }

    fun toSearch() {
        navController.navigate(NavDestination.Search.route())
    }

    fun toFullPoster(pictures: List<String>) {
        navController.navigate(
            NavDestination.FullPoster
                .putArguments(mapOf(NavArgument.Pictures to pictures.toNavArgument()))
        )
    }

    fun toSettings() {
        navController.navigate(NavDestination.Settings.route())
    }

    fun toListStyleSettings() {
        navController.navigate(NavDestination.ListStyleSettings.route())
    }

    fun toNotifications() {
        navController.navigate(NavDestination.Notifications.route())
    }

    fun toAbout() {
        navController.navigate(NavDestination.About.route())
    }

    fun toCredits() {
        navController.navigate(NavDestination.Credits.route())
    }

    companion object {
        @Composable
        fun rememberNavActionManager(
            navController: NavHostController = rememberNavController()
        ) = remember {
            NavActionManager(navController)
        }

        fun List<String>.toNavArgument(): String = Uri.encode(Json.encodeToString(this))
    }
}