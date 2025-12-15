package com.viz.prodzen.data.repository

import com.viz.prodzen.data.local.AppCategoryDao
import com.viz.prodzen.data.local.AppCategoryMappingDao
import com.viz.prodzen.data.local.entities.AppCategory
import com.viz.prodzen.data.local.entities.AppCategoryMapping
import com.viz.prodzen.data.managers.CategoryManager
import com.viz.prodzen.data.model.AppInfo
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepository @Inject constructor(
    private val categoryDao: AppCategoryDao,
    private val mappingDao: AppCategoryMappingDao
) {

    /**
     * Initialize default categories if not already present
     */
    suspend fun initializeDefaultCategories() {
        categoryDao.insertCategories(CategoryManager.DEFAULT_CATEGORIES)
    }

    /**
     * Get all categories
     */
    fun getAllCategories(): Flow<List<AppCategory>> {
        return categoryDao.getAllCategories()
    }

    /**
     * Get category by ID
     */
    suspend fun getCategoryById(id: Int): AppCategory? {
        return categoryDao.getCategoryById(id)
    }

    /**
     * Get category for a specific app
     */
    suspend fun getCategoryForApp(packageName: String): Int? {
        return mappingDao.getMappingForPackage(packageName)?.categoryId
    }

    /**
     * Auto-categorize an app
     */
    suspend fun autoCategorizeApp(packageName: String, appName: String) {
        val categoryId = CategoryManager.categorizeApp(packageName, appName)
        val mapping = AppCategoryMapping(packageName, categoryId)
        mappingDao.insertMapping(mapping)
    }

    /**
     * Auto-categorize multiple apps
     */
    suspend fun autoCategorizeApps(apps: List<AppInfo>) {
        val mappings = apps.map { app ->
            val categoryId = CategoryManager.categorizeApp(app.packageName, app.appName)
            AppCategoryMapping(app.packageName, categoryId)
        }
        mappingDao.insertMappings(mappings)
    }

    /**
     * Manually set app category
     */
    suspend fun setAppCategory(packageName: String, categoryId: Int) {
        val mapping = AppCategoryMapping(packageName, categoryId)
        mappingDao.insertMapping(mapping)
    }

    /**
     * Get all apps in a category
     */
    fun getAppsInCategory(categoryId: Int): Flow<List<AppCategoryMapping>> {
        return mappingDao.getPackagesInCategory(categoryId)
    }

    /**
     * Update category limit
     */
    suspend fun updateCategoryLimit(categoryId: Int, limitMinutes: Int) {
        val category = categoryDao.getCategoryById(categoryId)
        category?.let {
            categoryDao.updateCategory(it.copy(dailyLimitMinutes = limitMinutes))
        }
    }

    /**
     * Remove app from category
     */
    suspend fun removeAppFromCategory(packageName: String) {
        mappingDao.deleteMapping(packageName)
    }
}

