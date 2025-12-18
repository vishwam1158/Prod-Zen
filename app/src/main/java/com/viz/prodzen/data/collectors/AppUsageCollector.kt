package com.viz.prodzen.data.collectors

import android.content.Context
import com.viz.prodzen.data.repository.AppUsageRepository
import com.viz.prodzen.utils.PermissionManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppUsageCollector @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: AppUsageRepository
) {

    fun collectNow() {
        if (PermissionManager.hasUsageStatsPermission(context)) {
            CoroutineScope(Dispatchers.IO).launch {
                repository.refreshUsageData(System.currentTimeMillis())
            }
        }
    }
}

