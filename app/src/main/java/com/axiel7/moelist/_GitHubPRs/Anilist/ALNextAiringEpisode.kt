package com.axiel7.moelist._GitHubPRs.Anilist

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.anime.AnimeNode
import com.axiel7.moelist.data.model.anime.Broadcast
import com.axiel7.moelist.data.model.media.BaseMediaNode
import com.axiel7.moelist.data.model.media.BaseUserMediaList
import com.axiel7.moelist.utils.StringExtensions.toStringOrEmpty
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
{
    fun EpN_in_Mdays_ToString():String
    {
        var days = secondsToDays_AsString(timeUntilAiring)
        var str = """Ep ${episode} in ${days}"""
        return str
    }


}
@Serializable
data class Title(
    val english: String?,
)
{

}

/**
 * ALl Funcs Below are Helper. they Makes date Human Readable.
 */

/**
 * Supports Days , Hours , Minutes. less than a minute will be 0
 */
fun secondsToDays_AsString(seconds: Long): String {
    val _1month :Long = 30 *24 * 60 * 60
    val _1day :Long = 24 * 60 * 60
    val _1hour :Long = 60 * 60
    val _1min :Long =  60

    var HumanReadbleTime =""

    HumanReadbleTime =
        GetNLDatesString_OrNull(seconds, _1month , "months" ,"month" )
        ?: GetNLDatesString_OrNull(seconds, _1day , "days" ,"day" )
        ?: GetNLDatesString_OrNull(seconds, _1hour , "hours" ,"hour" )
        ?: GetNLDatesString_OrNull(seconds, _1min , "mins" ,"min" )
        ?: "? sec" ;

    return HumanReadbleTime;
}

private fun GetNLDatesString_OrNull(
    seconds: Long,
    _1Period: Long,
    PluaralText:String,
    SingularText:String,
): String? {
    var HumanReadbleTime: String? = null

    if (seconds > _1Period) {
        val days = seconds / _1Period; HumanReadbleTime = "${days} ${PluaralText}"
    } else if (seconds == _1Period) {
        val days = seconds / _1Period; HumanReadbleTime = "${days} ${SingularText}"
    }
    return HumanReadbleTime;
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
 * For Grid - ie: 8d
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

