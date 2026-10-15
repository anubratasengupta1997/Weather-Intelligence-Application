package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recent_cities")
data class RecentCityEntity(
    @PrimaryKey val id: Long,
    val name: String,
    val admin1: String?,
    val country: String?,
    val countryCode: String?,
    val latitude: Double,
    val longitude: Double,
    val searchedAt: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false
)
