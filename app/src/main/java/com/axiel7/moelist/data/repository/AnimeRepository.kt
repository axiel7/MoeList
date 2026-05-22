package com.axiel7.moelist.data.repository

import androidx.annotation.IntRange
import com.axiel7.moelist.data.model.Response
import com.axiel7.moelist.data.model.anime.AnimeDetails
import com.axiel7.moelist.data.model.anime.AnimeList
import com.axiel7.moelist.data.model.anime.AnimeNode
import com.axiel7.moelist.data.model.anime.AnimeRanking
import com.axiel7.moelist.data.model.anime.AnimeSeasonal
import com.axiel7.moelist.data.model.anime.MyAnimeListStatus
import com.axiel7.moelist.data.model.anime.StartSeason
import com.axiel7.moelist.data.model.anime.UserAnimeList
import com.axiel7.moelist.data.model.media.Character
import com.axiel7.moelist.data.model.media.ListStatus
import com.axiel7.moelist.data.model.media.MediaSort
import com.axiel7.moelist.data.model.media.MediaStatus
import com.axiel7.moelist.data.model.media.RankingType
import com.axiel7.moelist.data.network.Api
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.time.Instant

class AnimeRepository(
    private val api: Api,
    private val defaultPreferencesRepository: DefaultPreferencesRepository
) {

    private val _userAnimeList = MutableStateFlow<List<UserAnimeList>>(emptyList())
    val userAnimeList = _userAnimeList.asStateFlow()

    companion object {
        const val TODAY_FIELDS =
            "alternative_titles{en,ja},broadcast,mean,start_season,status,my_list_status{status}"
        const val CALENDAR_FIELDS =
            "alternative_titles{en,ja},broadcast,mean,start_season,status,media_type,num_episodes," +
                    "my_list_status{status}"
        const val SEASONAL_FIELDS =
            "alternative_titles{en,ja},start_season,broadcast,num_episodes,media_type,mean,num_list_users" +
                    ",my_list_status{status}"
        private const val RECOMMENDED_FIELDS = "alternative_titles{en,ja},mean"
        private const val LIST_STATUS_FIELDS =
            "start_date,finish_date,num_times_rewatched,is_rewatching,rewatch_value,priority,tags,comments"
        private const val ANIME_DETAILS_FIELDS =
            "id,title,main_picture,pictures,alternative_titles,start_date,end_date," +
                    "synopsis,mean,rank,popularity,num_list_users,num_scoring_users,media_type,status,genres," +
                    "my_list_status{$LIST_STATUS_FIELDS},num_episodes,start_season,broadcast,source," +
                    "average_episode_duration,studios,opening_themes,ending_themes," +
                    "related_anime{media_type,alternative_titles{en,ja}}," +
                    "related_manga{media_type,alternative_titles{en,ja}}," +
                    "recommendations{alternative_titles{en,ja}},background,statistics"
        private const val USER_ANIME_LIST_FIELDS =
            "alternative_titles{en,ja},list_status{$LIST_STATUS_FIELDS},num_episodes,media_type,status,broadcast,mean"
        private const val SEARCH_FIELDS =
            "id,title,alternative_titles{en,ja},main_picture,mean,media_type,num_episodes,start_season," +
                    "my_list_status{status,score}"
        const val RANKING_FIELDS =
            "alternative_titles{en,ja},mean,media_type,num_episodes,num_list_users,my_list_status{status}"

        // https://myanimelist.net/forum/?topicid=2111811
        private const val CHARACTERS_FIELDS =
            "id,first_name,last_name,alternative_name,main_picture,role,voice_actors{first_name,last_name,main_picture}"
    }

    fun getStatusForAnime(id: Int): Flow<MyAnimeListStatus?> {
        return userAnimeList.map { list ->
            list.find { it.node.id == id }?.listStatus
        }.distinctUntilChanged()
    }

    suspend fun getSeasonalAnimes(
        sort: MediaSort,
        startSeason: StartSeason,
        isNew: Boolean? = null,
        limit: Int,
        fields: String?,
        page: String? = null,
    ): Response<List<AnimeSeasonal>> {
        return try {
            val result = if (page == null) api.getSeasonalAnime(
                sort = sort,
                year = startSeason.year,
                season = startSeason.season.value,
                limit = limit,
                nsfw = defaultPreferencesRepository.nsfwInt(),
                fields = fields,
            )
            else api.getSeasonalAnime(page)
            if (isNew != null) {
                result.copy(
                    // filter for new or continuing anime
                    data = result.data?.filter {
                        if (isNew) it.node.startSeason == startSeason
                        else it.node.startSeason != startSeason
                    }
                )
            } else {
                result
            }
        } catch (e: Exception) {
            Response(message = e.message)
        }
    }

    suspend fun getRecommendedAnimes(
        limit: Int,
        page: String? = null
    ): Response<List<AnimeList>> {
        return try {
            if (page == null) api.getAnimeRecommendations(
                limit = limit,
                nsfw = defaultPreferencesRepository.nsfwInt(),
                fields = RECOMMENDED_FIELDS
            )
            else api.getAnimeRecommendations(page)
        } catch (e: Exception) {
            Response(message = e.message)
        }
    }

    suspend fun getAnimeDetails(
        animeId: Int
    ): AnimeDetails? {
        return try {
            api.getAnimeDetails(animeId, ANIME_DETAILS_FIELDS)
        } catch (_: Exception) {
            null
        }
    }

    suspend fun getUserAnimeList(
        status: ListStatus,
        sort: MediaSort,
        page: String? = null,
    ): Response<List<UserAnimeList>> {
        if (page == null) {
            // Clear existing items for this status immediately to force UI into loading state
            _userAnimeList.update { current -> current.filter { it.listStatus?.status != status } }
        }
        return try {
            val result = if (page == null) api.getUserAnimeList(
                status = status,
                sort = sort,
                limit = 1000,
                nsfw = defaultPreferencesRepository.nsfwInt(),
                fields = USER_ANIME_LIST_FIELDS
            )
            else api.getUserAnimeList(page)
            
            if (result.data != null) {
                _userAnimeList.update { currentList ->
                    val newList = currentList.toMutableList()
                    result.data.forEach { newItem ->
                        val existingIndex = newList.indexOfFirst { it.node.id == newItem.node.id }
                        if (existingIndex != -1) {
                            newList[existingIndex] = newItem
                        } else {
                            newList.add(newItem)
                        }
                    }
                    newList
                }
            }
            result
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
        // Optimistic update
        val previousList = _userAnimeList.value
        val now = Instant.now().toString()
        _userAnimeList.update { currentList ->
            currentList.map { 
                if (it.node.id == animeId) {
                    it.copy(
                        listStatus = it.listStatus?.copy(
                            status = status ?: it.listStatus.status,
                            score = score ?: it.listStatus.score,
                            progress = watchedEpisodes ?: it.listStatus.progress,
                            startDate = startDate ?: it.listStatus.startDate,
                            finishDate = endDate ?: it.listStatus.finishDate,
                            isRepeating = isRewatching ?: it.listStatus.isRepeating,
                            repeatCount = numRewatches ?: it.listStatus.repeatCount,
                            repeatValue = rewatchValue ?: it.listStatus.repeatValue,
                            priority = priority ?: it.listStatus.priority,
                            tags = tags?.split(",") ?: it.listStatus.tags,
                            comments = comments ?: it.listStatus.comments,
                            updatedAt = now
                        )
                    )
                } else it
            }
        }

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
            
            // Fetch updated entry info to ensure we have full node data
            val fullDetails = try { api.getAnimeDetails(animeId, USER_ANIME_LIST_FIELDS) } catch (_: Exception) { null }
            
            _userAnimeList.update { currentList ->
                val newList = currentList.toMutableList()
                val existingIndex = newList.indexOfFirst { it.node.id == animeId }
                if (fullDetails != null) {
                    val updatedItem = UserAnimeList(
                        node = AnimeNode(
                            id = fullDetails.id,
                            title = fullDetails.title.orEmpty(),
                            mainPicture = fullDetails.mainPicture,
                            alternativeTitles = fullDetails.alternativeTitles,
                            numEpisodes = fullDetails.numEpisodes ?: 0,
                            mediaFormat = fullDetails.mediaFormat,
                            status = fullDetails.status,
                            broadcast = fullDetails.broadcast,
                            mean = fullDetails.mean ?: 0f
                        ),
                        listStatus = result ?: fullDetails.myListStatus
                    )
                    if (existingIndex != -1) {
                        newList[existingIndex] = updatedItem
                    } else {
                        newList.add(updatedItem)
                    }
                }
                newList
            }
            result
        } catch (_: Exception) {
            _userAnimeList.value = previousList
            null
        }
    }

    suspend fun deleteAnimeEntry(
        animeId: Int
    ): Boolean {
        return try {
            val result = api.deleteAnimeEntry(animeId)
            if (result.status == HttpStatusCode.OK) {
                _userAnimeList.update { currentList ->
                    currentList.filter { it.node.id != animeId }
                }
                return true
            }
            false
        } catch (_: Exception) {
            false
        }
    }

    suspend fun searchAnime(
        query: String,
        limit: Int,
        offset: Int? = null,
        page: String? = null,
    ): Response<List<AnimeList>> {
        return try {
            if (page == null) api.getAnimeList(
                query = query,
                limit = limit,
                offset = offset,
                nsfw = defaultPreferencesRepository.nsfwInt(),
                fields = SEARCH_FIELDS,
            )
            else api.getAnimeList(page)
        } catch (e: Exception) {
            Response(message = e.message)
        }
    }


    suspend fun getAnimeRanking(
        rankingType: RankingType,
        limit: Int,
        fields: String?,
        page: String? = null
    ): Response<List<AnimeRanking>> {
        return try {
            if (page == null) api.getAnimeRanking(
                rankingType = rankingType.serialName,
                limit = limit,
                nsfw = defaultPreferencesRepository.nsfwInt(),
                fields = fields,
            )
            else api.getAnimeRanking(page)
        } catch (e: Exception) {
            Response(message = e.message)
        }
    }

    suspend fun getWeeklyAnime(
        fields: String? = CALENDAR_FIELDS
    ): Response<Array<MutableList<AnimeRanking>>> {
        val rankResponse = getAnimeRanking(
            rankingType = RankingType.AIRING,
            limit = 300,
            fields = fields
        )
        return if (rankResponse.isSuccess) {
            val tempWeekArray = arrayOf<MutableList<AnimeRanking>>(
                mutableListOf(),//0: MONDAY
                mutableListOf(),//1: TUESDAY
                mutableListOf(),//2: WEDNESDAY
                mutableListOf(),//3: THURSDAY
                mutableListOf(),//4: FRIDAY
                mutableListOf(),//5: SATURDAY
                mutableListOf(),//6: SUNDAY
            )
            rankResponse.data
                ?.sortedBy { it.node.broadcast?.secondsUntilNextBroadcast() }
                ?.forEach { anime ->
                anime.node.broadcast?.localDayOfTheWeek()?.let { day ->
                    tempWeekArray[day.ordinal].add(anime)
                }
            }

            Response(data = tempWeekArray)
        } else {
            Response(
                message = rankResponse.message,
                error = rankResponse.error,
            )
        }
    }

    suspend fun getAnimeAiringStatus(
        animeId: Int
    ): AnimeDetails? {
        return try {
            api.getAnimeDetails(animeId, fields = "id,status")
        } catch (_: Exception) {
            null
        }
    }

    suspend fun getAnimeCharacters(
        animeId: Int,
        limit: Int?,
        offset: Int?,
        page: String? = null
    ): Response<List<Character>> {
        return try {
            if (page == null) api.getAnimeCharacters(
                animeId = animeId,
                limit = limit,
                offset = offset,
                fields = CHARACTERS_FIELDS,
            )
            else api.getAnimeCharacters(page)
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
                fields = "status,broadcast,alternative_titles{en,ja}"
            )

            result.data?.map { it.node }
                ?.filter { it.broadcast != null && it.status == MediaStatus.AIRING }
                ?.sortedBy { it.broadcast!!.secondsUntilNextBroadcast() }
        } catch (_: Exception) {
            null
        }
    }

    suspend fun getAnimeIdsOfUserList(
        status: ListStatus,
        prefetchedList: List<UserAnimeList> = emptyList(),
        page: String? = null
    ): Response<List<Int>> {
        return try {
            val result = if (page == null) api.getUserAnimeList(
                status = status,
                sort = MediaSort.UPDATED,
                limit = 1000,
                nsfw = defaultPreferencesRepository.nsfwInt(),
                fields = "id",
            ) else api.getUserAnimeList(page)
            if (result.error != null) {
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
