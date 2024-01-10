package com.axiel7.moelist.data.repository

import androidx.annotation.IntRange
import com.axiel7.moelist.data.model.Response
import com.axiel7.moelist.data.model.anime.AnimeDetails
import com.axiel7.moelist.data.model.anime.AnimeList
import com.axiel7.moelist.data.model.anime.AnimeNode
import com.axiel7.moelist.data.model.anime.AnimeRanking
import com.axiel7.moelist.data.model.anime.AnimeSeasonal
import com.axiel7.moelist.data.model.anime.MyAnimeListStatus
import com.axiel7.moelist.data.model.anime.Season
import com.axiel7.moelist.data.model.anime.UserAnimeList
import com.axiel7.moelist.data.model.media.Character
import com.axiel7.moelist.data.model.media.ListStatus
import com.axiel7.moelist.data.model.media.MediaSort
import com.axiel7.moelist.data.model.media.MediaStatus
import com.axiel7.moelist.data.model.media.RankingType
import com.axiel7.moelist.data.network.Api
import io.ktor.http.HttpStatusCode

class AnimeRepository(
    private val api: Api,
    private val defaultPreferencesRepository: DefaultPreferencesRepository
) : BaseRepository(api, defaultPreferencesRepository) {

    companion object {
        const val TODAY_FIELDS =
            "alternative_titles{en,ja},broadcast,mean,start_season,status"
        const val CALENDAR_FIELDS =
            "alternative_titles{en,ja},broadcast,mean,start_season,status,media_type,num_episodes"
        const val SEASONAL_FIELDS =
            "alternative_titles{en,ja},start_season,broadcast,num_episodes,media_type,mean,num_list_users"
        private const val RECOMMENDED_FIELDS = "alternative_titles{en,ja},mean"
        private const val LIST_STATUS_FIELDS =
            "start_date,finish_date,num_times_rewatched,is_rewatching,rewatch_value,priority,tags,comments"
        private const val ANIME_DETAILS_FIELDS =
            "id,title,main_picture,pictures,alternative_titles,start_date,end_date," +
                    "synopsis,mean,rank,popularity,num_list_users,num_scoring_users,media_type,status,genres," +
                    "my_list_status{$LIST_STATUS_FIELDS},num_episodes,start_season,broadcast,source," +
                    "average_episode_duration,studios,opening_themes,ending_themes,related_anime{media_type}," +
                    "related_manga{media_type},recommendations,background"
        private const val USER_ANIME_LIST_FIELDS =
            "alternative_titles{en,ja},list_status{$LIST_STATUS_FIELDS},num_episodes,media_type,status,broadcast"
        private const val SEARCH_FIELDS =
            "id,title,alternative_titles{en,ja},main_picture,mean,media_type,num_episodes,start_season"
        const val RANKING_FIELDS =
            "alternative_titles{en,ja},mean,media_type,num_episodes,num_list_users"
        private const val CHARACTERS_FIELDS =
            "id,first_name,last_name,alternative_name,main_picture"
    }

    suspend fun getSeasonalAnimes(
        sort: MediaSort,
        year: Int,
        season: Season,
        limit: Int,
        fields: String?,
        page: String? = null,
    ): Response<List<AnimeSeasonal>>? {
        return try {
            val result = if (page == null) api.getSeasonalAnime(
                sort = sort,
                year = year,
                season = season.value,
                limit = limit,
                nsfw = defaultPreferencesRepository.nsfwInt(),
                fields = fields,
            )
            else api.getSeasonalAnime(page)
            result.error?.let { handleResponseError(it) }
            return result
        } catch (e: Exception) {
            Response(message = e.message)
        }
    }

    suspend fun getRecommendedAnimes(
        limit: Int,
        page: String? = null
    ): Response<List<AnimeList>>? {
        return try {
            val result = if (page == null) api.getAnimeRecommendations(
                limit = limit,
                nsfw = defaultPreferencesRepository.nsfwInt(),
                fields = RECOMMENDED_FIELDS
            )
            else api.getAnimeRecommendations(page)
            result.error?.let { handleResponseError(it) }
            return result
        } catch (e: Exception) {
            Response(message = e.message)
        }
    }

    suspend fun getAnimeDetails(
        animeId: Int
    ): AnimeDetails? {
        return try {
            api.getAnimeDetails(animeId, ANIME_DETAILS_FIELDS)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getUserAnimeList(
        status: ListStatus,
        sort: MediaSort,
        page: String? = null,
    ): Response<List<UserAnimeList>>? {
        return try {
            val result = if (page == null) api.getUserAnimeList(
                status = status,
                sort = sort,
                limit = null,
                nsfw = defaultPreferencesRepository.nsfwInt(),
                fields = USER_ANIME_LIST_FIELDS
            )
            else api.getUserAnimeList(page)
            result.error?.let { handleResponseError(it) }
            return result
        } catch (e: Exception) {
            Response(message = e.message)
        }
    }

    suspend fun updateAnimeEntry(
        animeId: Int,
        status: ListStatus? = null,
        @IntRange(0, 10) score: Int? = null,
        watchedEpisodes: Int? = null,
        startDate: String? = null,
        endDate: String? = null,
        isRewatching: Boolean? = null,
        numRewatches: Int? = null,
        @IntRange(0, 5) rewatchValue: Int? = null,
        @IntRange(0, 2) priority: Int? = null,
        tags: String? = null,
        comments: String? = null,
    ): MyAnimeListStatus? {
        return try {
            val result = api.updateUserAnimeList(
                animeId,
                status,
                score,
                watchedEpisodes,
                startDate,
                endDate,
                isRewatching,
                numRewatches,
                rewatchValue,
                priority,
                tags,
                comments
            )
            result.error?.let { handleResponseError(it) }
            return result
        } catch (e: Exception) {
            null
        }
    }

    suspend fun deleteAnimeEntry(
        animeId: Int
    ): Boolean {
        return try {
            val result = api.deleteAnimeEntry(animeId)
            return result.status == HttpStatusCode.OK
        } catch (e: Exception) {
            false
        }
    }

    suspend fun searchAnime(
        query: String,
        limit: Int,
        offset: Int?,
        page: String? = null,
    ): Response<List<AnimeList>>? {
        return try {
            val result = if (page == null) api.getAnimeList(
                query = query,
                limit = limit,
                offset = offset,
                nsfw = defaultPreferencesRepository.nsfwInt(),
                fields = SEARCH_FIELDS,
            )
            else api.getAnimeList(page)
            result.error?.let { handleResponseError(it) }
            return result
        } catch (e: Exception) {
            Response(message = e.message)
        }
    }


    suspend fun getAnimeRanking(
        rankingType: RankingType,
        limit: Int,
        fields: String?,
        page: String? = null
    ): Response<List<AnimeRanking>>? {
        return try {
            val result =
                if (page == null) api.getAnimeRanking(
                    rankingType = rankingType.serialName,
                    limit = limit,
                    nsfw = defaultPreferencesRepository.nsfwInt(),
                    fields = fields,
                )
                else api.getAnimeRanking(page)
            result.error?.let { handleResponseError(it) }
            return result
        } catch (e: Exception) {
            Response(message = e.message)
        }
    }

    suspend fun getAnimeAiringStatus(
        animeId: Int
    ): AnimeDetails? {
        return try {
            api.getAnimeDetails(animeId, fields = "id,status")
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getAnimeCharacters(
        animeId: Int,
        limit: Int?,
        offset: Int?,
        page: String? = null
    ): Response<List<Character>>? {
        return try {
            val result = if (page == null) api.getAnimeCharacters(
                animeId = animeId,
                limit = limit,
                offset = offset,
                fields = CHARACTERS_FIELDS,
            )
            else api.getAnimeCharacters(page)
            result.error?.let { handleResponseError(it) }
            return result
        } catch (e: Exception) {
            Response(message = e.message)
        }
    }

    // widget
    suspend fun getAiringAnimeOnList(): List<AnimeNode>? {
        return try {
            val result: Response<List<UserAnimeList>> = api.getUserAnimeList(
                status = ListStatus.WATCHING,
                sort = MediaSort.ANIME_START_DATE,
                limit = null,
                nsfw = defaultPreferencesRepository.nsfwInt(),
                fields = "status,broadcast"
            )

            return result.data?.map { it.node }
                ?.filter { it.broadcast != null && it.status == MediaStatus.AIRING }
                ?.sortedBy { it.broadcast!!.secondsUntilNextBroadcast() }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getAnimeIdsOfUserList(
        status: ListStatus,
        prefetchedList: List<UserAnimeList> = emptyList(),
        page: String? = null
    ): Response<List<Int>>? {
        return try {
            val result = if (page == null) api.getUserAnimeList(
                status = status,
                sort = MediaSort.UPDATED,
                limit = 1000,
                nsfw = defaultPreferencesRepository.nsfwInt(),
                fields = "id",
            ) else api.getUserAnimeList(page)
            result.error?.let {
                handleResponseError(it)
                return Response(error = result.error, message = result.message)
            }
            if (result.paging?.next != null) {
                getAnimeIdsOfUserList(
                    status = status,
                    prefetchedList = prefetchedList.plus(result.data.orEmpty()),
                    page = result.paging.next
                )
            } else return Response(
                data = prefetchedList.plus(result.data.orEmpty()).map { it.node.id }
            )
        } catch (e: Exception) {
            Response(message = e.message)
        }
    }
}