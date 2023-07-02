package com.axiel7.moelist.uicompose.base

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.axiel7.moelist.R
import com.axiel7.moelist.uicompose.home.HOME_DESTINATION
import com.axiel7.moelist.uicompose.more.MORE_TAB_DESTINATION
import com.axiel7.moelist.uicompose.userlist.ANIME_LIST_DESTINATION
import com.axiel7.moelist.uicompose.userlist.MANGA_LIST_DESTINATION

sealed class BottomDestination(
    val value: String,
    val route: String,
    @StringRes val title: Int,
    @DrawableRes val icon: Int,
    @DrawableRes val iconSelected: Int,
) {
    object Home: BottomDestination(
        value = "home",
        route = HOME_DESTINATION,
        title = R.string.title_home,
        icon = R.drawable.ic_outline_home_24,
        iconSelected = R.drawable.ic_round_home_24
    )
    object AnimeList: BottomDestination(
        value = "anime",
        route = ANIME_LIST_DESTINATION,
        title = R.string.title_anime_list,
        icon = R.drawable.ic_outline_local_movies_24,
        iconSelected = R.drawable.ic_round_local_movies_24
    )
    object MangaList: BottomDestination(
        value = "manga",
        route = MANGA_LIST_DESTINATION,
        title = R.string.title_manga_list,
        icon = R.drawable.ic_outline_book_24,
        iconSelected = R.drawable.ic_round_book_24
    )
    object More: BottomDestination(
        value = "more",
        route = MORE_TAB_DESTINATION,
        title = R.string.more,
        icon = R.drawable.ic_more_horizontal,
        iconSelected = R.drawable.ic_more_horizontal
    )

    companion object {
        val values = listOf(Home, AnimeList, MangaList, More)

        fun String.toBottomDestinationIndex() = when (this) {
            Home.value -> 0
            AnimeList.value -> 1
            MangaList.value -> 2
            More.value -> 3
            else -> null
        }
    }
}
