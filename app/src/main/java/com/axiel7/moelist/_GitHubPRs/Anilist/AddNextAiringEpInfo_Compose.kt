package com.axiel7.moelist._GitHubPRs.Anilist

import androidx.compose.runtime.Composable
import com.axiel7.moelist.data.model.anime.AnimeNode
import com.axiel7.moelist.data.model.media.ListStatus
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.ui.userlist.UserMediaListEvent
import com.axiel7.moelist.ui.userlist.UserMediaListUiState
import kotlinx.coroutines.runBlocking

/**
 * add Airing Next Ep No from AnilistApi
 * --  ie:Ep 8 in 5 days
 */
@Composable
fun AddNextAiringEpInfo_Compose(uiState: UserMediaListUiState, event: UserMediaListEvent?  ) {

    if (uiState.mediaType != MediaType.ANIME)
        return
    if ( !(uiState.listStatus == ListStatus.WATCHING
        || uiState.listStatus == ListStatus.PLAN_TO_WATCH) )
        return

    val airingAnimes_idlist = uiState.mediaList.filter { it.isAiring }.map{ it.node.id }
    if (airingAnimes_idlist.isNullOrEmpty())
        return

    uiState.mediaList
        .filter {  it.isAiring }
        .forEach { (it.node as? AnimeNode)?.al_nextAiringEpisode = "AL loading...";  }


    Thread {
        println("alquery.getAiringInfo run. if this is run too much. cache it. ")

        // Perform network operation here
        runBlocking {
            var al_mediaList = AnilistQuery.GetAiringInfo_ToPoco_FromCache(airingAnimes_idlist)
            if (al_mediaList?.isEmpty() == true)
                return@runBlocking

            uiState.mediaList.filter { it.isAiring }.forEach { it ->
                if (!(it.node is AnimeNode))
                    return@runBlocking

                var _id = (it.node as? AnimeNode)?.id?.toLong()
                // (it.node as? AnimeNode)?.al_nextAiringEpisode = "test success"
                var it_AirInfo =
                    al_mediaList?.firstOrNull { it2 -> it2.idMal == _id }?.nextAiringEpisode

                (it.node as? AnimeNode)?.al_nextAiringEpisode = it_AirInfo?.EpN_in_Mdays_ToString()
                runBlocking {
//                    event?.RedrawSingleItem(it)
                    event?.onItemSelected(it)
                }
            }

        }

    }.start()


}