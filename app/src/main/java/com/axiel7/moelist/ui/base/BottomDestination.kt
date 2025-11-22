package com.axiel7.moelist.ui.base

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation3.runtime.NavKey
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.ui.base.navigation.Route

sealed class BottomDestination(
    val value: String,
    val index: Int,
    val route: NavKey,
    @StringRes val title: Int,
    @DrawableRes val icon: Int,
    @DrawableRes val iconSelected: Int,
) {
    data object Home : BottomDestination(
        value = "home",
        index = 0,
        route = Route.Tab.Home,
        title = R.string.title_home,
        icon = R.drawable.ic_outline_home_24,
        iconSelected = R.drawable.ic_round_home_24
    )

    data object AnimeList : BottomDestination(
        value = "anime",
        index = 1,
        route = Route.Tab.Anime(mediaType = MediaType.ANIME),
        title = R.string.title_anime_list,
        icon = R.drawable.ic_outline_local_movies_24,
        iconSelected = R.drawable.ic_round_local_movies_24
    )

    data object MangaList : BottomDestination(
        value = "manga",
        index = 2,
        route = Route.Tab.Manga(MediaType.MANGA),
        title = R.string.title_manga_list,
        icon = R.drawable.ic_outline_book_24,
        iconSelected = R.drawable.ic_round_book_24
    )

    data object Profile : BottomDestination(
        value = "profile",
        index = 4,
        route = Route.Profile,
        title = R.string.title_profile,
        icon = R.drawable.ic_outline_person_24,
        iconSelected = R.drawable.ic_round_person_24
    )

    data object More : BottomDestination(
        value = "more",
        index = 3,
        route = Route.Tab.More,
        title = R.string.more,
        icon = R.drawable.ic_more_horizontal,
        iconSelected = R.drawable.ic_more_horizontal
    )

    companion object {
        val values = listOf(Home, AnimeList, MangaList, More)

        val railValues = listOf(Home, AnimeList, MangaList, Profile, More)

        fun String.toBottomDestinationIndex() = values.find { it.value == this }?.index

        fun Int.toBottomDestinationRoute(): NavKey? = values.find { it.index == this }?.route

        fun NavKey.isBottomDestination() = values.any { it.route == this }

        @Composable
        fun BottomDestination.Icon(selected: Boolean) {
            androidx.compose.material3.Icon(
                painter = painterResource(if (selected) iconSelected else icon),
                contentDescription = stringResource(title)
            )
        }
    }
}
