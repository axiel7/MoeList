package com.axiel7.moelist.data.repository

import com.axiel7.moelist.App
import com.axiel7.moelist.data.model.manga.MangaDetails

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

}