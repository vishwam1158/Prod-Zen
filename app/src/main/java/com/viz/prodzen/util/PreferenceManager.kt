package com.viz.prodzen.util

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferenceManager @Inject constructor(@ApplicationContext context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("prodzen_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_DATA_BACKFILLED = "data_backfilled"
        private const val KEY_CATEGORIES_INITIALIZED = "categories_initialized"
    }

    fun isDataBackfilled(): Boolean {
        return prefs.getBoolean(KEY_DATA_BACKFILLED, false)
    }

    fun setDataBackfilled(wasBackfilled: Boolean) {
        prefs.edit().putBoolean(KEY_DATA_BACKFILLED, wasBackfilled).apply()
    }

    fun areCategoriesInitialized(): Boolean {
        return prefs.getBoolean(KEY_CATEGORIES_INITIALIZED, false)
    }

    fun setCategoriesInitialized(initialized: Boolean) {
        prefs.edit().putBoolean(KEY_CATEGORIES_INITIALIZED, initialized).apply()
    }
}
