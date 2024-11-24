package com.axiel7.moelist._GitHubPRs.Anilist

import android.app.Application
import com.axiel7.moelist.data.model.anime.UserAnimeList
import com.axiel7.moelist.data.model.media.ListStatus
import com.axiel7.moelist.utils.StringExtensions.toStringOrEmpty
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response

import com.mayakapps.kache.InMemoryKache
import com.mayakapps.kache.KacheStrategy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import kotlin.concurrent.thread
import kotlin.system.measureTimeMillis

import kotlin.time.Duration.Companion.hours

class AnilistQuery {

    //static Holder
    companion object {
        lateinit var appThis: CoroutineScope
        lateinit var appContext: Application
        lateinit var cache : InMemoryKache<List<Int>,List<Media>>

        @JvmStatic
        suspend fun New_ObjectKache() : InMemoryKache<List<Int>,List<Media>>  {
            //GlobalScope.launch (Dispatchers.Main)  {}
            val cache = InMemoryKache<List<Int>,List<Media>>(500) {
                strategy = KacheStrategy.LRU
                expireAfterAccessDuration =  1.hours
            }
            return cache
        }



        //---------------Normal
        /**
         * Uses withCache
         */
        suspend fun AddNextAiringEpInfo_withMeasureTime(
            result: com.axiel7.moelist.data.model.Response<List<UserAnimeList>>) {
            result.data?.let {
                val timeInMillis: Long = measureTimeMillis {
                    AddNextAiringEpInfo(it)
                }
                println("AddNextAiringEpInfo : elapsedTime(ms):" + timeInMillis)
            };
        }

        //AnimeRepository
        /**
         * Uses withCache
         */
        suspend fun AddNextAiringEpInfo( userAnimeList :List<UserAnimeList>
        ):List<UserAnimeList>?
        {
            fun _isAiring(it: UserAnimeList) =
                (
                        (it.listStatus?.status == ListStatus.WATCHING
                                || it.listStatus?.status == ListStatus.PLAN_TO_WATCH)
                                && it.isAiring
                        )

            var airingAnimes = userAnimeList.filter{ _isAiring(it) }

            val airingAnimes_idlist = airingAnimes.map{ it.node.id }
            if (airingAnimes_idlist.isEmpty())
                return null

            var al_mediaList = GetAiringInfo_ToPoco_FromCache(airingAnimes_idlist)
            if (al_mediaList?.isEmpty() == true)
                return null

            userAnimeList.filter  { _isAiring(it) }.forEach { it ->
                var _id = it.node.id.toLong();
                // it.node.al_nextAiringEpisode = "test success";
                var it_AirInfo = al_mediaList?.firstOrNull {  it.idMal == _id }?.nextAiringEpisode

                it.node.al_nextAiringEpisode = it_AirInfo?.EpN_in_Mdays_ToString()
            }
            return userAnimeList;
        }



        suspend fun GetAiringInfo_ToPoco_FromCache( airingAnimes_id_list: List<Int>): List<Media>?
        {
            val key = airingAnimes_id_list;

            val data = cache.getOrPut(key) {
                try {
                    GetAiringInfo_ToPoco(key)
                } catch (ex: Throwable) {
                    println(ex.message )
                    println(ex.cause )
                    println("GetAiringInfo_ToPoco_FromCache" )
                    null // returning null, The value (null) will not be cached
                }
            }
            return data;
        }

        fun GetAiringInfo_ToPoco( airingAnimes_id_list: List<Int> ): List<Media>?
        {
            var resp1 = getAiringInfo(airingAnimes_id_list)
            var resp1_bodSTR = resp1.body?.string().toStringOrEmpty()
            val al_AirDataList = Json.decodeFromString<ALNextAiringEpisode>(resp1_bodSTR)
            var al_mediaList = al_AirDataList.data?.Page?.media;
            return al_mediaList
        }
        
        fun getAiringInfo (mal_id_list: List<Int>): Response
        {
            var url =  "https://com.example/graphql";
            var query = Build_query_AiringInfo(mal_id_list);
//        // HAVE TO MAKE graphql query one liner.  - otherwise its not valid json, will fail.
//        query = query.replace("\n", "  " ).replace("  ", "  ")

            var resp1 = makeRequest_POST_JSON(ANILIST_GRAPHQL_URL,query);

            if(resp1.code != 200) {
                println("response NOT200:" + resp1.code + " - " + resp1.body)
                var errorMessage = resp1.body?.string()
                println("response_body?_string:" + errorMessage)
            }

            return  resp1;
        }

        private fun Build_query_AiringInfo( mal_id_list: List<Int> ): String
        {
            val mal_ids_asSTR = mal_id_list.joinToString(separator = ", ")

            //validJSON
            val query_AiringInfo_oneliner = """   query Al_AiringInfo { Page (page: 0, perPage: 50) {  pageInfo {  total  currentPage  lastPage  hasNextPage  perPage  }  media(idMal_in: [$mal_ids_asSTR], type: ANIME)  {  id  idMal  nextAiringEpisode {  episode  timeUntilAiring  }  title {  english  }  } } }  """;
            return  query_AiringInfo_oneliner;

//        val query_AiringInfo =
//            """
//query  {
//    Page (page: 0, perPage: 50) {
//        pageInfo {
//            total
//            currentPage
//            lastPage
//            hasNextPage
//            perPage
//        }
//
//        media(idMal_in: [$mal_ids_asSTR], type: ANIME)
//        {
//            id
//            idMal
//            nextAiringEpisode {
//                episode
//                timeUntilAiring
//            }
//            title {
//                english
//            }
//        }
//    }
//}
//
//"""

        }

        private fun makeRequest_POST_JSON(url: String, query: String ): Response
        {
            var jsonQuery =  """ {"query":"  $query  ", "variables":null, "operationName":null} """
            val client = OkHttpClient()

//        val json = JSONObject()
//        json.put("query",query)
//        val requestBody = json.toString().toRequestBody(null)
            val requestBody = jsonQuery.toRequestBody()
            val request =
                Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .addHeader("Content-type", "application/json")
                    .addHeader("Accept", "application/json")
                    .build()

            return client.newCall(request).execute()
        }


    }


}

