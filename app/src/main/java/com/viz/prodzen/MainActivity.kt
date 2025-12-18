package com.viz.prodzen


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.isSystemInDarkTheme
import com.viz.prodzen.data.collectors.AppUsageCollector
import com.viz.prodzen.ui.navigation.AppNavigation
import com.viz.prodzen.ui.theme.ProdZenTheme
import com.viz.prodzen.utils.LocalThemePreference
import com.viz.prodzen.utils.ThemePreference
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var appUsageCollector: AppUsageCollector

    private val permissionCheckTrigger = mutableStateOf(0)
    private lateinit var themePreference: ThemePreference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        themePreference = ThemePreference(this)

        setContent {
            val themeMode = themePreference.themeMode
            val systemInDarkTheme = isSystemInDarkTheme()

            val darkTheme = when (themeMode) {
                ThemePreference.MODE_LIGHT -> false
                ThemePreference.MODE_DARK -> true
                else -> systemInDarkTheme
            }

            CompositionLocalProvider(LocalThemePreference provides themePreference) {
                ProdZenTheme(darkTheme = darkTheme) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        // Trigger recomposition when permissions change
                        val trigger by permissionCheckTrigger
                        AppNavigation(permissionCheckTrigger = trigger)
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Check permissions every time app comes to foreground
        // This triggers navigation recomposition
        permissionCheckTrigger.value += 1

        // Trigger data collection if permissions are granted
        appUsageCollector.collectNow()
    }
}
