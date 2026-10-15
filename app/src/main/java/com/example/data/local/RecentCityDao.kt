package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RecentCityDao {
    @Query("SELECT * FROM recent_cities ORDER BY searchedAt DESC LIMIT 10")
    fun getRecentCities(): Flow<List<RecentCityEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(city: RecentCityEntity)

    @Delete
    suspend fun delete(city: RecentCityEntity)

    @Query("DELETE FROM recent_cities")
    suspend fun clearAll()

    @Query("UPDATE recent_cities SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: Long, isFavorite: Boolean)
}
