package com.axiel7.moelist.data.repository

import com.axiel7.moelist.App
import com.axiel7.moelist.data.model.ApiParams
import com.axiel7.moelist.data.model.Response
import com.axiel7.moelist.data.model.manga.MangaDetails
import com.axiel7.moelist.data.model.manga.MyMangaListStatus
import com.axiel7.moelist.data.model.manga.UserMangaList

object MangaRepository {

    const val MANGA_DETAILS_FIELDS = "id,title,main_picture,alternative_titles,start_date,end_date," +
            "synopsis,mean,rank,popularity,num_list_users,num_scoring_users,media_type,status,genres," +
            "my_list_status{num_times_reread},num_chapters,num_volumes,source,authors{first_name,last_name}," +
            "serialization,related_anime{media_type},related_manga{media_type}"

    suspend fun getMangaDetails(
        mangaId: Int
    ): MangaDetails? {
        return try {
            App.api.getMangaDetails(mangaId, MANGA_DETAILS_FIELDS)
        } catch (e: Exception) {
            null
        }
    }

    const val USER_MANGA_LIST_FIELDS = "list_status{num_times_rewatched},num_episodes,media_type,status"

    suspend fun getUserMangaList(
        apiParams: ApiParams,
        page: String? = null
    ): Response<List<UserMangaList>>? {
        return try {
            val result = if (page == null) App.api.getUserMangaList(apiParams)
            else App.api.getUserMangaList(page)
            result.error?.let { BaseRepository.handleResponseError(it) }
            return result
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updateAnimeEntry(
        mangaId: Int,
        status: String?,
        score: Int?,
        chaptersRead: Int?,
        volumesRead: Int?,
        startDate: String?,
        endDate: String?,
        numRereads: Int?,
    ): MyMangaListStatus? {
        return try {
            val result = App.api.updateUserMangaList(mangaId, status, score, chaptersRead, volumesRead, startDate, endDate, numRereads)
            result.error?.let { BaseRepository.handleResponseError(it) }
            return result
        } catch (e: Exception) {
            null
        }
    }
}