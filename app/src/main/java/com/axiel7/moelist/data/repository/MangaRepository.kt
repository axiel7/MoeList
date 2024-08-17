package com.axiel7.moelist.data.repository

import androidx.annotation.IntRange
import com.axiel7.moelist.data.model.Response
import com.axiel7.moelist.data.model.manga.MangaDetails
import com.axiel7.moelist.data.model.manga.MangaList
import com.axiel7.moelist.data.model.manga.MangaRanking
import com.axiel7.moelist.data.model.manga.MyMangaListStatus
import com.axiel7.moelist.data.model.manga.UserMangaList
import com.axiel7.moelist.data.model.media.ListStatus
import com.axiel7.moelist.data.model.media.MediaSort
import com.axiel7.moelist.data.model.media.RankingType
import com.axiel7.moelist.data.network.Api
import io.ktor.http.HttpStatusCode

class MangaRepository(
    private val api: Api,
    private val defaultPreferencesRepository: DefaultPreferencesRepository
) : BaseRepository(api, defaultPreferencesRepository) {

    companion object {
        private const val LIST_STATUS_FIELDS =
            "start_date,finish_date,num_times_reread,is_rereading,reread_value,priority,tags,comments"
        private const val MANGA_DETAILS_FIELDS =
            "id,title,main_picture,pictures,alternative_titles,start_date,end_date," +
                    "synopsis,mean,rank,popularity,num_list_users,num_scoring_users,media_type,status,genres," +
                    "my_list_status{$LIST_STATUS_FIELDS},num_chapters,num_volumes,source,authors{first_name,last_name}," +
                    "serialization,related_anime{media_type,alternative_titles{en,ja}}," +
                    "related_manga{media_type,alternative_titles{en,ja}}," +
                    "recommendations{alternative_titles{en,ja}},background"
        private const val USER_MANGA_LIST_FIELDS =
            "alternative_titles{en,ja},list_status{$LIST_STATUS_FIELDS},num_chapters,num_volumes,media_type,status"
        private const val SEARCH_FIELDS =
            "id,title,alternative_titles{en,ja},main_picture,mean,media_type,num_chapters,start_date," +
                    "my_list_status{status}"
        private const val RANKING_FIELDS =
            "alternative_titles{en,ja},mean,media_type,num_chapters,num_list_users,my_list_status{status}"
    }

    suspend fun getMangaDetails(
        mangaId: Int
    ): MangaDetails? {
        return try {
            api.getMangaDetails(mangaId, MANGA_DETAILS_FIELDS)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getUserMangaList(
        status: ListStatus,
        sort: MediaSort,
        page: String? = null
    ): Response<List<UserMangaList>> {
        return try {
            val result = if (page == null) api.getUserMangaList(
                status = status,
                sort = sort,
                limit = null,
                nsfw = defaultPreferencesRepository.nsfwInt(),
                fields = USER_MANGA_LIST_FIELDS,
            )
            else api.getUserMangaList(page)
            result.error?.let { handleResponseError(it) }
            return result
        } catch (e: Exception) {
            Response(message = e.message)
        }
    }

    suspend fun updateMangaEntry(
        mangaId: Int,
        status: ListStatus? = null,
        @IntRange(0, 10) score: Int? = null,
        chaptersRead: Int? = null,
        volumesRead: Int? = null,
        startDate: String? = null,
        endDate: String? = null,
        isRereading: Boolean? = null,
        numRereads: Int? = null,
        @IntRange(0, 5) rereadValue: Int? = null,
        @IntRange(0, 2) priority: Int? = null,
        tags: String? = null,
        comments: String? = null,
    ): MyMangaListStatus? {
        return try {
            val result = api.updateUserMangaList(
                mangaId,
                status,
                score,
                chaptersRead,
                volumesRead,
                startDate,
                endDate,
                isRereading,
                numRereads,
                rereadValue,
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

    suspend fun deleteMangaEntry(
        mangaId: Int
    ): Boolean {
        return try {
            val result = api.deleteMangaEntry(mangaId)
            return result.status == HttpStatusCode.OK
        } catch (e: Exception) {
            false
        }
    }

    suspend fun searchManga(
        query: String,
        limit: Int,
        offset: Int? = null,
        page: String? = null
    ): Response<List<MangaList>> {
        return try {
            val result = if (page == null) api.getMangaList(
                query = query,
                limit = limit,
                offset = offset,
                nsfw = defaultPreferencesRepository.nsfwInt(),
                fields = SEARCH_FIELDS,
            )
            else api.getMangaList(page)
            result.error?.let { handleResponseError(it) }
            return result
        } catch (e: Exception) {
            Response(message = e.message)
        }
    }

    suspend fun getMangaRanking(
        rankingType: RankingType,
        limit: Int,
        page: String? = null
    ): Response<List<MangaRanking>> {
        return try {
            val result =
                if (page == null) api.getMangaRanking(
                    rankingType = rankingType.serialName,
                    limit = limit,
                    nsfw = defaultPreferencesRepository.nsfwInt(),
                    fields = RANKING_FIELDS,
                )
                else api.getMangaRanking(page)
            result.error?.let { handleResponseError(it) }
            return result
        } catch (e: Exception) {
            Response(message = e.message)
        }
    }

    suspend fun getMangaIdsOfUserList(
        status: ListStatus,
        prefetchedList: List<UserMangaList> = emptyList(),
        page: String? = null
    ): Response<List<Int>> {
        return try {
            val result = if (page == null) api.getUserMangaList(
                status = status,
                sort = MediaSort.UPDATED,
                limit = 1000,
                nsfw = defaultPreferencesRepository.nsfwInt(),
                fields = "id",
            ) else api.getUserMangaList(page)
            result.error?.let {
                handleResponseError(it)
                return Response(error = result.error, message = result.message)
            }
            if (result.paging?.next != null) {
                getMangaIdsOfUserList(
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