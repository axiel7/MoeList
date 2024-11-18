package com.axiel7.moelist.Anilist

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class ALNextAiringEpisode(
    val data: Data,
)
@Serializable
data class Data(
    @SerialName("Page" ) val Page: Page,
)
@Serializable
data class Page(
    val pageInfo: PageInfo,
    val media: List<Media>,
)
@Serializable
data class PageInfo(
    val total: Long,
    val currentPage: Long,
    val lastPage: Long,
    val hasNextPage: Boolean,
    val perPage: Long,
)
@Serializable
data class Media(
    val id: Long,
    val idMal: Long,
    val nextAiringEpisode: NextAiringEpisode?,
    val title: Title,
)
@Serializable
data class NextAiringEpisode(
    val episode: Long,
    val timeUntilAiring: Long,
)
@Serializable
data class Title(
    val english: String,
)


fun secondsToDays(seconds: Long): String {
    val days = seconds.toDouble() / (24 * 60 * 60)
    return String.format("%.1f", days)
}