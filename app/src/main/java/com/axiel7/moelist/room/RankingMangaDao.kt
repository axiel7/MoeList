package com.axiel7.moelist.room

import androidx.room.*
import com.axiel7.moelist.model.MangaRanking

@Dao
interface RankingMangaDao {

    @Query("SELECT * FROM manga_ranking WHERE ranking_type LIKE :rankingType ORDER BY rank ASC")
    fun getRankingMangas(rankingType: String): MutableList<MangaRanking>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllRankingMangas(manga_ranking: MutableList<MangaRanking>)

    @Delete
    fun deleteRankingManga(manga_ranking: MangaRanking)

    @Delete
    fun deleteAllRankingMangas(manga_ranking: MutableList<MangaRanking>)
}