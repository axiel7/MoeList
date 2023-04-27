package com.axiel7.moelist.uicompose.base

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.axiel7.moelist.R
import com.axiel7.moelist.uicompose.home.HOME_DESTINATION
import com.axiel7.moelist.uicompose.more.MORE_DESTINATION
import com.axiel7.moelist.uicompose.userlist.ANIME_LIST_DESTINATION
import com.axiel7.moelist.uicompose.userlist.MANGA_LIST_DESTINATION

sealed class BottomDestination(
    val route: String,
    @StringRes val title: Int,
    @DrawableRes val icon: Int,
    @DrawableRes val iconSelected: Int,
) {
    object Home: BottomDestination(
        route = HOME_DESTINATION,
        title = R.string.title_home,
        icon = R.drawable.ic_outline_home_24,
        iconSelected = R.drawable.ic_round_home_24
    )
    object AnimeList: BottomDestination(
        route = ANIME_LIST_DESTINATION,
        title = R.string.title_anime_list,
        icon = R.drawable.ic_outline_local_movies_24,
        iconSelected = R.drawable.ic_round_local_movies_24
    )
    object MangaList: BottomDestination(
        route = MANGA_LIST_DESTINATION,
        title = R.string.title_manga_list,
        icon = R.drawable.ic_outline_book_24,
        iconSelected = R.drawable.ic_round_book_24
    )
    object More: BottomDestination(
        route = MORE_DESTINATION,
        title = R.string.more,
        icon = R.drawable.ic_more_horizontal,
        iconSelected = R.drawable.ic_more_horizontal
    )
}
