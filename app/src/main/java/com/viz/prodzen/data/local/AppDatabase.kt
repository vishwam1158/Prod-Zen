package com.viz.prodzen.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.viz.prodzen.data.model.AppInfo

@Database(
    entities = [AppInfo::class],
    version = 1, // Simplified back to version 1
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao
}