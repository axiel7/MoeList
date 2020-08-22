package com.axiel7.moelist.room

import androidx.room.*
import com.axiel7.moelist.model.AnimeRanking

@Dao
interface RankingAnimeDao {

    @Query("SELECT * FROM anime_ranking WHERE ranking_type LIKE :rankingType ORDER BY rank ASC")
    fun getRankingAnimes(rankingType: String): MutableList<AnimeRanking>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllRankingAnimes(anime_ranking: MutableList<AnimeRanking>)

    @Delete
    fun deleteRankingAnime(anime_ranking: AnimeRanking)

    @Delete
    fun deleteAllRankingAnimes(anime_ranking: MutableList<AnimeRanking>)
}