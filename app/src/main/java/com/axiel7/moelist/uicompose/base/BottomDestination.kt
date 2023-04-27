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
) {
    object Home: BottomDestination(HOME_DESTINATION, R.string.title_home, R.drawable.ic_outline_home_24)
    object AnimeList: BottomDestination(ANIME_LIST_DESTINATION, R.string.title_anime_list, R.drawable.ic_outline_local_movies_24)
    object MangaList: BottomDestination(MANGA_LIST_DESTINATION, R.string.title_manga_list, R.drawable.ic_outline_book_24)
    object More: BottomDestination(MORE_DESTINATION, R.string.more, R.drawable.ic_more_horizontal)
}
