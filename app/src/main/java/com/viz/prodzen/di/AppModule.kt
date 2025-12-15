package com.viz.prodzen.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.viz.prodzen.data.local.*
import com.viz.prodzen.data.repository.AppRepository
import com.viz.prodzen.data.repository.AppRepositoryImpl
import com.viz.prodzen.data.repository.CategoryRepository
import com.viz.prodzen.data.repository.UserStatsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    private val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Create user_stats table
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS user_stats (
                    id INTEGER PRIMARY KEY NOT NULL,
                    currentStreak INTEGER NOT NULL,
                    longestStreak INTEGER NOT NULL,
                    totalPoints INTEGER NOT NULL,
                    dailyGoalMinutes INTEGER NOT NULL,
                    lastGoalCheckDate INTEGER NOT NULL
                )
            """)

            // Create daily_goals table
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS daily_goals (
                    date INTEGER PRIMARY KEY NOT NULL,
                    screenTimeGoalMinutes INTEGER NOT NULL,
                    actualScreenTimeMinutes INTEGER NOT NULL,
                    goalMet INTEGER NOT NULL
                )
            """)

            // Create focus_sessions table
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS focus_sessions (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    startTime INTEGER NOT NULL,
                    endTime INTEGER,
                    plannedDurationMinutes INTEGER NOT NULL,
                    actualDurationMinutes INTEGER NOT NULL,
                    completed INTEGER NOT NULL
                )
            """)

            // Create achievements table
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS achievements (
                    id TEXT PRIMARY KEY NOT NULL,
                    name TEXT NOT NULL,
                    description TEXT NOT NULL,
                    iconRes INTEGER NOT NULL,
                    pointsReward INTEGER NOT NULL,
                    unlockedAt INTEGER
                )
            """)

            // Add openCount column to daily_usage
            database.execSQL("""
                ALTER TABLE daily_usage ADD COLUMN openCount INTEGER NOT NULL DEFAULT 0
            """)
        }
    }

    private val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Create app_categories table
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS app_categories (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    name TEXT NOT NULL,
                    color TEXT NOT NULL,
                    iconName TEXT NOT NULL,
                    dailyLimitMinutes INTEGER NOT NULL
                )
            """)

            // Create app_category_mapping table
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS app_category_mapping (
                    packageName TEXT PRIMARY KEY NOT NULL,
                    categoryId INTEGER NOT NULL,
                    FOREIGN KEY(categoryId) REFERENCES app_categories(id) ON DELETE CASCADE
                )
            """)

            // Create index for faster lookups
            database.execSQL("""
                CREATE INDEX IF NOT EXISTS index_app_category_mapping_categoryId 
                ON app_category_mapping(categoryId)
            """)
        }
    }

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "prodzen_db"
        )
            .addMigrations(MIGRATION_2_3, MIGRATION_3_4)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideAppDao(database: AppDatabase): AppDao {
        return database.appDao()
    }

    @Provides
    @Singleton
    fun provideUsageDao(database: AppDatabase): UsageDao {
        return database.usageDao()
    }

    @Provides
    @Singleton
    fun provideUserStatsDao(database: AppDatabase): UserStatsDao {
        return database.userStatsDao()
    }

    @Provides
    @Singleton
    fun provideFocusSessionDao(database: AppDatabase): FocusSessionDao {
        return database.focusSessionDao()
    }

    @Provides
    @Singleton
    fun provideAchievementDao(database: AppDatabase): AchievementDao {
        return database.achievementDao()
    }

    @Provides
    @Singleton
    fun provideAppCategoryDao(database: AppDatabase): AppCategoryDao {
        return database.appCategoryDao()
    }

    @Provides
    @Singleton
    fun provideAppCategoryMappingDao(database: AppDatabase): AppCategoryMappingDao {
        return database.appCategoryMappingDao()
    }

    @Provides
    @Singleton
    fun provideAppRepository(
        appDao: AppDao,
        usageDao: UsageDao,
        @ApplicationContext context: Context,
        categoryRepository: CategoryRepository
    ): AppRepository {
        return AppRepositoryImpl(appDao, usageDao, context, categoryRepository)
    }

    @Provides
    @Singleton
    fun provideUserStatsRepository(userStatsDao: UserStatsDao): UserStatsRepository {
        return UserStatsRepository(userStatsDao)
    }

    @Provides
    @Singleton
    fun provideCategoryRepository(
        categoryDao: AppCategoryDao,
        mappingDao: AppCategoryMappingDao
    ): CategoryRepository {
        return CategoryRepository(categoryDao, mappingDao)
    }
}
