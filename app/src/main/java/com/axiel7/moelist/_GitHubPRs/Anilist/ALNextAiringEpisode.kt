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

//    @Composable //compoable error all the way up
//    fun EpN_in_Mdays_ToString():String
//    {
//        var daysStr = timeUntilAiring.secondsToLegibleText();
//        return """Ep ${episode} in ${daysStr}"""
//    }

//    fun EpN_in_Mdays_ToShortString() =
//        """Ep${episode}, ${secondsToDays(timeUntilAiring)}d"""

}
@Serializable
data class Title(
    val english: String?,
)
{

}


//fun EpN_in_Mdays_ToString(it_AirInfo: NextAiringEpisode?) =
//    """Ep ${it_AirInfo?.episode} in ${secondsToDays(it_AirInfo?.timeUntilAiring ?: Long.MAX_VALUE)} day(s) """

////this behaves likes Static Func of c#. interesting?
//fun secondsToDays_AsStr(seconds: Long): String {
//    val days = seconds.toDouble() / (24 * 60 * 60)
//    return String.format("%.1f", days)
//}
//fun secondsToDays(seconds: Long): Double {
//    val days = seconds.toDouble() / (24 * 60 * 60)
//    return Math_Round(days,1)
//}
//fun Math_Round(value: Double , fractionalDigits:Int): Double {
//    return round(value * 10.0.pow(fractionalDigits)) / 10.0.pow(fractionalDigits)
//}

/**
 * Supports Days , Hours , Minutes. less than a minute will be 0
 */
fun secondsToDays_AsString(seconds: Long): String {
    val _1day :Long = 24 * 60 * 60
    val _1hour :Long = 60 * 60
    val _1min :Long =  60

    var HumanReadbleTime =""
    var pluralSuffix =""
    if(seconds> _1day)
    { val days = seconds / _1day; HumanReadbleTime="${days} days" }
    else if(seconds== _1day)
    { val days = seconds / _1day; HumanReadbleTime="${days} day" }
    else if(seconds>_1hour)
    { val days = seconds / _1hour; HumanReadbleTime="${days} hours" }
    else if(seconds==_1hour)
    { val days = seconds / _1hour; HumanReadbleTime="${days} hour"}
    else if(seconds>_1min)
    { val days = seconds / _1min; HumanReadbleTime="${days} mins"}
    else if(seconds==_1min)
    { val days = seconds / _1min; HumanReadbleTime="${days} min"}
    else
    { val days = seconds / _1min; HumanReadbleTime="? sec"}

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

