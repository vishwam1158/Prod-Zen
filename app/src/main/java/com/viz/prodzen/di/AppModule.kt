package com.viz.prodzen.di

import android.content.Context
import androidx.room.Room
import com.viz.prodzen.data.local.AppDao
import com.viz.prodzen.data.local.AppDatabase
import com.viz.prodzen.data.repository.AppRepository
import com.viz.prodzen.data.repository.AppRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "prodzen_db"
        )
            // This will delete the database if a schema migration is needed.
            // Useful for development, but should be replaced with a proper migration
            // strategy for a production app.
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideAppDao(database: AppDatabase): AppDao {
        return database.appDao()
    }

    // FIXED: Reverted the repository provider to match the simplified AppRepositoryImpl constructor.
    @Provides
    @Singleton
    fun provideAppRepository(appDao: AppDao, @ApplicationContext context: Context): AppRepository {
        return AppRepositoryImpl(appDao, context)
    }
}
