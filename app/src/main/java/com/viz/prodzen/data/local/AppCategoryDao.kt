package com.viz.prodzen.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.viz.prodzen.data.local.entities.AppCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface AppCategoryDao {
    @Query("SELECT * FROM app_categories ORDER BY name ASC")
    fun getAllCategories(): Flow<List<AppCategory>>

    @Query("SELECT * FROM app_categories WHERE id = :id")
    suspend fun getCategoryById(id: Int): AppCategory?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: AppCategory): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<AppCategory>)

    @Update
    suspend fun updateCategory(category: AppCategory)

    @Query("DELETE FROM app_categories WHERE id = :id")
    suspend fun deleteCategory(id: Int)
}

