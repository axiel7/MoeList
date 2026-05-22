package com.axiel7.moelist.ui.base

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocalMovies
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.rounded.Book
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LocalMovies
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.Person
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.ui.base.navigation.Route

sealed class BottomDestination(
    val value: String,
    val route: Any,
    @StringRes val title: Int,
    val icon: ImageVector,
    val iconSelected: ImageVector,
) {
    data object Home : BottomDestination(
        value = "home",
        route = Route.Tab.Home,
        title = R.string.title_home,
        icon = Icons.Outlined.Home,
        iconSelected = Icons.Rounded.Home
    )

    data object AnimeList : BottomDestination(
        value = "anime",
        route = Route.Tab.Anime(mediaType = MediaType.ANIME),
        title = R.string.title_anime_list,
        icon = Icons.Outlined.LocalMovies,
        iconSelected = Icons.Rounded.LocalMovies
    )

    data object MangaList : BottomDestination(
        value = "manga",
        route = Route.Tab.Manga(MediaType.MANGA),
        title = R.string.title_manga_list,
        icon = Icons.Outlined.Book,
        iconSelected = Icons.Rounded.Book
    )

    data object Profile : BottomDestination(
        value = "profile",
        route = Route.Profile,
        title = R.string.title_profile,
        icon = Icons.Outlined.Person,
        iconSelected = Icons.Rounded.Person
    )

    data object More : BottomDestination(
        value = "more",
        route = Route.Tab.More,
        title = R.string.more,
        icon = Icons.Rounded.MoreHoriz,
        iconSelected = Icons.Rounded.MoreHoriz
    )

    companion object {
        val values = listOf(Home, AnimeList, MangaList, More)

        val railValues = listOf(Home, AnimeList, MangaList, Profile, More)

        fun String.toBottomDestinationIndex() = when (this) {
            Home.value -> 0
            AnimeList.value -> 1
            MangaList.value -> 2
            More.value -> 3
            Profile.value -> 4
            else -> null
        }

        fun NavBackStackEntry.isBottomDestination() =
            destination.hierarchy.any { dest ->
                values.any { value -> dest.hasRoute(value.route::class) }
            }

        @Composable
        fun BottomDestination.Icon(selected: Boolean) {
            androidx.compose.material3.Icon(
                imageVector = if (selected) iconSelected else icon,
                contentDescription = stringResource(title)
            )
        }
    }
}
