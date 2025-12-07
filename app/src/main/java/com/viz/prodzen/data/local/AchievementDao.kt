package com.viz.prodzen.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.viz.prodzen.data.local.entities.Achievement
import kotlinx.coroutines.flow.Flow

@Dao
interface AchievementDao {
    @Query("SELECT * FROM achievements")
    fun getAllAchievements(): Flow<List<Achievement>>

    @Query("SELECT * FROM achievements WHERE unlockedAt IS NOT NULL ORDER BY unlockedAt DESC")
    fun getUnlockedAchievements(): Flow<List<Achievement>>

    @Query("SELECT * FROM achievements WHERE unlockedAt IS NULL")
    fun getLockedAchievements(): Flow<List<Achievement>>

    @Query("SELECT * FROM achievements WHERE id = :id")
    suspend fun getAchievementById(id: String): Achievement?

    @Update
    suspend fun updateAchievement(achievement: Achievement)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievements(achievements: List<Achievement>)

    @Query("SELECT COUNT(*) FROM achievements WHERE unlockedAt IS NOT NULL")
    suspend fun getUnlockedCount(): Int
}

