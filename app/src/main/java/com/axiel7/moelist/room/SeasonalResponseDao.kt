package com.axiel7.moelist.room

import androidx.room.*
import com.axiel7.moelist.model.SeasonalAnimeResponse

@Dao
interface SeasonalResponseDao {

    @Query("SELECT * FROM seasonal_response WHERE season LIKE :season AND year LIKE :year")
    fun getSeasonalResponse(season: String, year: Int): SeasonalAnimeResponse

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSeasonalResponse(seasonal_response: SeasonalAnimeResponse)

    @Delete
    fun deleteSeasonalResponse(seasonal_response: SeasonalAnimeResponse)
}