package com.axiel7.moelist.data.model.media

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.axiel7.moelist.R
import com.axiel7.moelist.utils.StringExtensions.formatGenre
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Genre(
    @SerialName("id")
    val id: Int,
    @SerialName("name")
    val name: String
)

@Composable
fun String.genreLocalized() = when (this) {
    "Action" -> stringResource(R.string.genre_action)
    "Adventure" -> stringResource(R.string.genre_adventure)
    "Cars" -> stringResource(R.string.genre_cars)
    "Comedy" -> stringResource(R.string.genre_comedy)
    "Dementia" -> stringResource(R.string.genre_dementia)
    "Demons" -> stringResource(R.string.genre_demons)
    "Drama" -> stringResource(R.string.genre_drama)
    "Ecchi" -> stringResource(R.string.genre_ecchi)
    "Fantasy" -> stringResource(R.string.genre_fantasy)
    "Game" -> stringResource(R.string.genre_game)
    "Harem" -> stringResource(R.string.genre_harem)
    "Hentai" -> stringResource(R.string.genre_hentai)
    "Historical" -> stringResource(R.string.genre_historical)
    "Horror" -> stringResource(R.string.genre_horror)
    "Josei" -> stringResource(R.string.genre_josei)
    "Kids" -> stringResource(R.string.genre_kids)
    "Magic" -> stringResource(R.string.genre_magic)
    "Martial Arts" -> stringResource(R.string.genre_martial_arts)
    "Mecha" -> stringResource(R.string.genre_mecha)
    "Military" -> stringResource(R.string.genre_military)
    "Music" -> stringResource(R.string.genre_music)
    "Mystery" -> stringResource(R.string.genre_mystery)
    "Parody" -> stringResource(R.string.genre_parody)
    "Police" -> stringResource(R.string.genre_police)
    "Psychological" -> stringResource(R.string.genre_psychological)
    "Romance" -> stringResource(R.string.genre_romance)
    "Samurai" -> stringResource(R.string.genre_samurai)
    "School" -> stringResource(R.string.genre_school)
    "Sci-Fi" -> stringResource(R.string.genre_sci_fi)
    "Seinen" -> stringResource(R.string.genre_seinen)
    "Shoujo" -> stringResource(R.string.genre_shoujo)
    "Shoujo Ai" -> stringResource(R.string.genre_shoujo_ai)
    "Shounen" -> stringResource(R.string.genre_shounen)
    "Shounen Ai" -> stringResource(R.string.genre_shounen_ai)
    "Slice of Life" -> stringResource(R.string.genre_slice_of_life)
    "Space" -> stringResource(R.string.genre_space)
    "Sports" -> stringResource(R.string.genre_sports)
    "Super Power" -> stringResource(R.string.genre_superpower)
    "Supernatural" -> stringResource(R.string.genre_supernatural)
    "Thriller" -> stringResource(R.string.genre_thriller)
    "Vampire" -> stringResource(R.string.genre_vampire)
    "Yaoi" -> stringResource(R.string.genre_yaoi)
    "Yuri" -> stringResource(R.string.genre_yuri)
    else -> this
}