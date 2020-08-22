package com.axiel7.moelist.room

import androidx.room.*
import com.axiel7.moelist.model.SeasonalList

@Dao
interface SeasonalListDao {

    @Query("SELECT * FROM seasonal_list")
    fun getSeasonalAnimes(): MutableList<SeasonalList>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllSeasonalAnimes(seasonal_list: MutableList<SeasonalList>)

    @Delete
    fun deleteSeasonalAnime(seasonal_list: SeasonalList)

    @Delete
    fun deleteAllSeasonalAnimes(seasonal_list: MutableList<SeasonalList>)
}