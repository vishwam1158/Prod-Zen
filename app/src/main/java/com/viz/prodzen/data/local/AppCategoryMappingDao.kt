package com.viz.prodzen.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.viz.prodzen.data.local.entities.AppCategoryMapping
import kotlinx.coroutines.flow.Flow

@Dao
interface AppCategoryMappingDao {
    @Query("SELECT * FROM app_category_mapping WHERE packageName = :packageName")
    suspend fun getMappingForPackage(packageName: String): AppCategoryMapping?

    @Query("SELECT * FROM app_category_mapping WHERE categoryId = :categoryId")
    fun getPackagesInCategory(categoryId: Int): Flow<List<AppCategoryMapping>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMapping(mapping: AppCategoryMapping)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMappings(mappings: List<AppCategoryMapping>)

    @Query("DELETE FROM app_category_mapping WHERE packageName = :packageName")
    suspend fun deleteMapping(packageName: String)

    @Query("DELETE FROM app_category_mapping WHERE categoryId = :categoryId")
    suspend fun deleteMappingsForCategory(categoryId: Int)
}

