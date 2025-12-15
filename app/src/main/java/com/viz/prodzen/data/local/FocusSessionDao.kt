package com.viz.prodzen.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.viz.prodzen.data.local.entities.FocusSession
import kotlinx.coroutines.flow.Flow

@Dao
interface FocusSessionDao {
    @Insert
    suspend fun insertSession(session: FocusSession): Long

    @Update
    suspend fun updateSession(session: FocusSession)

    @Query("SELECT * FROM focus_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<FocusSession>>

    @Query("SELECT * FROM focus_sessions WHERE startTime >= :startDate")
    suspend fun getSessionsSince(startDate: Long): List<FocusSession>

    @Query("SELECT * FROM focus_sessions WHERE completed = 1 AND startTime >= :startDate")
    suspend fun getCompletedSessionsSince(startDate: Long): List<FocusSession>

    @Query("SELECT SUM(actualDurationMinutes) FROM focus_sessions WHERE completed = 1 AND startTime >= :startDate")
    suspend fun getTotalFocusMinutesSince(startDate: Long): Int?
}

