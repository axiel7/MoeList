package com.axiel7.moelist.ui.base

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.ui.base.navigation.NavArgument
import com.axiel7.moelist.ui.base.navigation.NavDestination

sealed class BottomDestination(
    val value: String,
    val route: String,
    @StringRes val title: Int,
    @DrawableRes val icon: Int,
    @DrawableRes val iconSelected: Int,
) {
    data object Home : BottomDestination(
        value = "home",
        route = NavDestination.HomeTab.route(),
        title = R.string.title_home,
        icon = R.drawable.ic_outline_home_24,
        iconSelected = R.drawable.ic_round_home_24
    )

    data object AnimeList : BottomDestination(
        value = "anime",
        route = NavDestination.AnimeTab.putArguments(
            mapOf(NavArgument.MediaType to MediaType.ANIME.name)
        ),
        title = R.string.title_anime_list,
        icon = R.drawable.ic_outline_local_movies_24,
        iconSelected = R.drawable.ic_round_local_movies_24
    )

    data object MangaList : BottomDestination(
        value = "manga",
        route = NavDestination.MangaTab.putArguments(
            mapOf(NavArgument.MediaType to MediaType.MANGA.name)
        ),
        title = R.string.title_manga_list,
        icon = R.drawable.ic_outline_book_24,
        iconSelected = R.drawable.ic_round_book_24
    )

    data object Profile : BottomDestination(
        value = "profile",
        route = NavDestination.Profile.route(),
        title = R.string.title_profile,
        icon = R.drawable.ic_outline_person_24,
        iconSelected = R.drawable.ic_round_person_24
    )

    data object More : BottomDestination(
        value = "more",
        route = NavDestination.MoreTab.route(),
        title = R.string.more,
        icon = R.drawable.ic_more_horizontal,
        iconSelected = R.drawable.ic_more_horizontal
    )

    companion object {
        val values = listOf(Home, AnimeList, MangaList, More)

        val railValues = listOf(Home, AnimeList, MangaList, Profile, More)

        val routes = values.map { it.route }

        fun String.toBottomDestinationIndex() = when (this) {
            Home.value -> 0
            AnimeList.value -> 1
            MangaList.value -> 2
            More.value -> 3
            Profile.value -> 4
            else -> null
        }

        @Composable
        fun BottomDestination.Icon(selected: Boolean) {
            androidx.compose.material3.Icon(
                painter = painterResource(if (selected) iconSelected else icon),
                contentDescription = stringResource(title)
            )
        }
    }
}
