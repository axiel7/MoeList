package com.axiel7.moelist.Anilist

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.anime.AnimeNode
import com.axiel7.moelist.data.model.anime.Broadcast
import com.axiel7.moelist.data.model.anime.UserAnimeList
import com.axiel7.moelist.data.model.media.BaseMediaNode
import com.axiel7.moelist.data.model.media.BaseUserMediaList
import com.axiel7.moelist.data.model.media.ListStatus
import com.axiel7.moelist.utils.StringExtensions.toStringOrEmpty
import com.mayakapps.kache.InMemoryKache
import com.mayakapps.kache.KacheStrategy
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import kotlin.system.measureTimeMillis
import kotlin.time.Duration.Companion.hours

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


//this behaves likes Static Func of c#. interesting?
fun secondsToDays(seconds: Long): String {
    val days = seconds.toDouble() / (24 * 60 * 60)
    return String.format("%.1f", days)
}

@Composable
fun AiringEpN_in_Ndays_ToString(
    broadcast: Broadcast?,
    item: BaseUserMediaList<out BaseMediaNode>
): String {
    val isAiring = remember { item.isAiring }

//  var textCompact = broadcast?.airingInString() ?: stringResource(R.string.airing)
    var text =
         if (isAiring ) broadcast?.airingInString() ?: stringResource(R.string.airing)
       else item.node.mediaFormat?.localized().orEmpty()

    if (item.node is AnimeNode)
        text = (item.node as AnimeNode)?.al_nextAiringEpisode.toStringOrEmpty()
    return text
}


/**
 * For Grid  ie: 8d
  */
@Composable
fun AiringEpN_in_Ndays_ToShortString(
    broadcast: Broadcast?,
    item: BaseUserMediaList<out BaseMediaNode>
): String {
    val isAiring = remember { item.isAiring }

    var text = broadcast?.airingInShortString() ?: stringResource(R.string.airing)

    if (item.node is AnimeNode)
        text = (item.node as AnimeNode)?.al_nextAiringEpisode.toStringOrEmpty()
    return text
}

