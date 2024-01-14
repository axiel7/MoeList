package com.axiel7.moelist.ui.base.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.axiel7.moelist.data.model.media.MediaType
import com.uragiristereo.serializednavigationextension.runtime.navigate
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
        navController.navigate(Route.MediaRanking(mediaType))
    }

    fun toMediaDetails(mediaType: MediaType, id: Int) {
        navController.navigate(
            Route.MediaDetails(
                mediaType = mediaType,
                mediaId = id,
            )
        )
    }

    fun toCalendar() {
        navController.navigate(Route.Calendar)
    }

    fun toSeasonChart() {
        navController.navigate(Route.SeasonChart)
    }

    fun toRecommendations() {
        navController.navigate(Route.Recommendations)
    }

    fun toProfile() {
        navController.navigate(Route.Profile)
    }

    fun toSearch() {
        navController.navigate(Route.Search)
    }

    fun toFullPoster(pictures: List<String>) {
        navController.navigate(Route.FullPoster(pictures))
    }

    fun toSettings() {
        navController.navigate(Route.Settings)
    }

    fun toListStyleSettings() {
        navController.navigate(Route.ListStyleSettings)
    }

    fun toNotifications() {
        navController.navigate(Route.Notifications)
    }

    fun toAbout() {
        navController.navigate(Route.About)
    }

    fun toCredits() {
        navController.navigate(Route.Credits)
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
