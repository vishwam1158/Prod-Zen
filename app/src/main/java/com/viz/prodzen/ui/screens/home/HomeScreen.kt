package com.viz.prodzen.ui.screens.home

import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.viz.prodzen.data.model.AppInfo
import com.viz.prodzen.ui.navigation.hasUsageStatsPermission
import java.util.concurrent.TimeUnit
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.res.painterResource
import com.viz.prodzen.R


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var hasPermission by remember { mutableStateOf(hasUsageStatsPermission(context)) }

    LaunchedEffect(Unit) {
        hasPermission = hasUsageStatsPermission(context)
        if (hasPermission) {
            viewModel.loadUsageStats()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Prod Zen") },
                actions = {
                    IconButton(onClick = { navController.navigate("analytics_screen") }) {
                        Icon(Icons.Default.BarChart, contentDescription = "Analytics")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (hasPermission) {
                StatisticsDashboard(uiState = uiState, navController = navController)
            } else {
                PermissionPrompt()
            }
        }
    }
}

@Composable
fun StatisticsDashboard(uiState: HomeUiState, navController: NavController) {
    var showBarChart by remember { mutableStateOf(true) }

    Spacer(modifier = Modifier.height(16.dp))

    // Streak Card
    if (uiState.currentStreak > 0) {
        StreakCard(
            streak = uiState.currentStreak,
            onClick = { /* Navigation handled by bottom nav */ }
        )
        Spacer(modifier = Modifier.height(16.dp))
    }

    // Goal Progress Card
    GoalProgressCard(
        totalUsage = uiState.totalUsage,
        goalMinutes = uiState.dailyGoalMinutes,
        progress = uiState.goalProgress,
        onClick = { /* Navigation handled by bottom nav */ }
    )
    Spacer(modifier = Modifier.height(16.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Today's Screen Time", style = MaterialTheme.typography.titleMedium)
        Image(painter = painterResource(id = R.drawable.ic_launcher_background), contentDescription = "", modifier = Modifier.size(24.dp))
    }
    Text(
        formatMillisToHoursMinutes(uiState.totalUsage),
        style = MaterialTheme.typography.headlineLarge,
        fontWeight = FontWeight.Bold
    )
    Spacer(modifier = Modifier.height(24.dp))

    if (showBarChart) {
        AnalyticsBarChart(apps = uiState.topApps)
    } else {
        ZigZagLineChart(apps = uiState.topApps)
    }

    Spacer(modifier = Modifier.height(32.dp))

    // Detailed Analytics Section
    DetailedAnalyticsSection(apps = uiState.topApps, navController = navController)
}

@Composable
fun DetailedAnalyticsSection(apps: List<AppInfo>, navController: NavController) {
    var selectedTimePeriod by remember { mutableStateOf(TimePeriod.HOURLY) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Detailed Analytics",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = { navController.navigate("analytics_screen") }) {
                Text("View All")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Time Period Tabs
        TimePeriodTabs(
            selectedPeriod = selectedTimePeriod,
            onPeriodSelected = { selectedTimePeriod = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // App Analytics Cards
        if (apps.isEmpty()) {
            Text(
                "No usage data available",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(16.dp)
            )
        } else {
            apps.take(10).forEach { app ->
                AppAnalyticsCard(
                    app = app,
                    timePeriod = selectedTimePeriod
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun TimePeriodTabs(
    selectedPeriod: TimePeriod,
    onPeriodSelected: (TimePeriod) -> Unit
) {
    ScrollableTabRow(
        selectedTabIndex = selectedPeriod.ordinal,
        modifier = Modifier.fillMaxWidth(),
        edgePadding = 0.dp
    ) {
        TimePeriod.values().forEach { period ->
            Tab(
                selected = selectedPeriod == period,
                onClick = { onPeriodSelected(period) },
                text = { Text(period.label) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppAnalyticsCard(
    app: AppInfo,
    timePeriod: TimePeriod
) {
    var expanded by remember(app.packageName) { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = { expanded = !expanded }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        app.appName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        formatMillisToHoursMinutes(app.usageTodayMillis),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "${getAppOpenCount(app, timePeriod)} opens",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        timePeriod.label.lowercase(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(16.dp))
                Divider()
                Spacer(modifier = Modifier.height(16.dp))

                // Hourly breakdown
                Text(
                    "Usage Pattern (${timePeriod.label})",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                AppUsageBreakdown(app = app, timePeriod = timePeriod)
            }
        }
    }
}

@Composable
fun AppUsageBreakdown(app: AppInfo, timePeriod: TimePeriod) {
    val breakdownData = generateBreakdownData(app, timePeriod)

    Column {
        breakdownData.forEach { data ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    data.label,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(2f)
                ) {
                    LinearProgressIndicator(
                        progress = data.percentage,
                        modifier = Modifier
                            .weight(1f)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        data.value,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.width(60.dp),
                        textAlign = TextAlign.End
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Opens breakdown
        Text(
            "Opens: ${getAppOpenCount(app, timePeriod)}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}

enum class TimePeriod(val label: String) {
    HOURLY("Hourly"),
    DAILY("Daily"),
    WEEKLY("Weekly"),
    MONTHLY("Monthly")
}

data class BreakdownData(
    val label: String,
    val value: String,
    val percentage: Float
)

fun generateBreakdownData(app: AppInfo, timePeriod: TimePeriod): List<BreakdownData> {
    // Generate sample data based on time period
    // In production, this would fetch actual data from the repository
    val totalUsage = app.usageTodayMillis

    return when (timePeriod) {
        TimePeriod.HOURLY -> {
            // Generate hourly breakdown for last 6 hours
            (0..5).map { hourAgo ->
                val usage = (totalUsage / 6) + (Math.random() * (totalUsage / 12)).toLong()
                val hour = java.util.Calendar.getInstance().apply {
                    add(java.util.Calendar.HOUR_OF_DAY, -hourAgo)
                }.get(java.util.Calendar.HOUR_OF_DAY)

                BreakdownData(
                    label = String.format("%02d:00", hour),
                    value = formatMillisToHoursMinutes(usage),
                    percentage = (usage.toFloat() / totalUsage.toFloat()).coerceIn(0f, 1f)
                )
            }.reversed()
        }
        TimePeriod.DAILY -> {
            // Generate daily breakdown for last 7 days
            (0..6).map { dayAgo ->
                val usage = (totalUsage / 7) + (Math.random() * (totalUsage / 14)).toLong()
                val day = java.util.Calendar.getInstance().apply {
                    add(java.util.Calendar.DAY_OF_MONTH, -dayAgo)
                }
                val dayName = when (day.get(java.util.Calendar.DAY_OF_WEEK)) {
                    java.util.Calendar.SUNDAY -> "Sun"
                    java.util.Calendar.MONDAY -> "Mon"
                    java.util.Calendar.TUESDAY -> "Tue"
                    java.util.Calendar.WEDNESDAY -> "Wed"
                    java.util.Calendar.THURSDAY -> "Thu"
                    java.util.Calendar.FRIDAY -> "Fri"
                    else -> "Sat"
                }

                BreakdownData(
                    label = dayName,
                    value = formatMillisToHoursMinutes(usage),
                    percentage = (usage.toFloat() / totalUsage.toFloat()).coerceIn(0f, 1f)
                )
            }.reversed()
        }
        TimePeriod.WEEKLY -> {
            // Generate weekly breakdown for last 4 weeks
            (0..3).map { weekAgo ->
                val usage = (totalUsage / 4) + (Math.random() * (totalUsage / 8)).toLong()

                BreakdownData(
                    label = "Week ${4 - weekAgo}",
                    value = formatMillisToHoursMinutes(usage),
                    percentage = (usage.toFloat() / totalUsage.toFloat()).coerceIn(0f, 1f)
                )
            }.reversed()
        }
        TimePeriod.MONTHLY -> {
            // Generate monthly breakdown for last 3 months
            (0..2).map { monthAgo ->
                val usage = (totalUsage / 3) + (Math.random() * (totalUsage / 6)).toLong()
                val month = java.util.Calendar.getInstance().apply {
                    add(java.util.Calendar.MONTH, -monthAgo)
                }
                val monthName = when (month.get(java.util.Calendar.MONTH)) {
                    java.util.Calendar.JANUARY -> "Jan"
                    java.util.Calendar.FEBRUARY -> "Feb"
                    java.util.Calendar.MARCH -> "Mar"
                    java.util.Calendar.APRIL -> "Apr"
                    java.util.Calendar.MAY -> "May"
                    java.util.Calendar.JUNE -> "Jun"
                    java.util.Calendar.JULY -> "Jul"
                    java.util.Calendar.AUGUST -> "Aug"
                    java.util.Calendar.SEPTEMBER -> "Sep"
                    java.util.Calendar.OCTOBER -> "Oct"
                    java.util.Calendar.NOVEMBER -> "Nov"
                    else -> "Dec"
                }

                BreakdownData(
                    label = monthName,
                    value = formatMillisToHoursMinutes(usage),
                    percentage = (usage.toFloat() / totalUsage.toFloat()).coerceIn(0f, 1f)
                )
            }.reversed()
        }
    }
}

fun getAppOpenCount(app: AppInfo, timePeriod: TimePeriod): Int {
    // Generate sample open counts based on time period
    // In production, this would fetch actual data from the repository
    return when (timePeriod) {
        TimePeriod.HOURLY -> (5..15).random()
        TimePeriod.DAILY -> (20..50).random()
        TimePeriod.WEEKLY -> (100..300).random()
        TimePeriod.MONTHLY -> (400..1200).random()
    }
}

@Composable
fun ZigZagLineChart(apps: List<AppInfo>) {
    val maxUsage = apps.maxOfOrNull { it.usageTodayMillis } ?: 1L

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Text("Usage Trend Today", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        if (apps.isEmpty()) {
            Text("No usage data available for today.", style = MaterialTheme.typography.bodyMedium)
        } else {
            Canvas(modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)) {
                val path = Path()
                val stepX = size.width / (apps.size - 1).coerceAtLeast(1)
                val maxY = size.height

                apps.forEachIndexed { index, app ->
                    val x = index * stepX
                    val y = maxY - (app.usageTodayMillis.toFloat() / maxUsage * maxY)
                    if (index == 0) {
                        path.moveTo(x, y)
                    } else {
                        path.lineTo(x, y)
                    }
                }

//                val strokeWidth = with(LocalDensity.current) { 4.dp.toPx() }

                drawPath(
                    path = path,
                    color = Color.White,//MaterialTheme.colorScheme.primary,
                    style = Stroke(width = 4.dp.toPx())
                )
            }
        }
    }
}

/*
@Composable
fun StatisticsDashboard(uiState: HomeUiState) {
    Spacer(modifier = Modifier.height(16.dp))
    Text("Today's Screen Time", style = MaterialTheme.typography.titleMedium)
    Text(formatMillisToHoursMinutes(uiState.totalUsage), style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.height(24.dp))
    AnalyticsBarChart(apps = uiState.topApps)
}
*/

@Composable
fun PermissionPrompt() {
    val context = LocalContext.current
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Permission Required", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "To show your usage statistics, ProdZen needs access to your phone's usage data.",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = {
            context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        }) {
            Text("Grant Permission")
        }
    }
}

@Composable
fun AnalyticsBarChart(apps: List<AppInfo>) {
    val maxUsage = apps.maxOfOrNull { it.usageTodayMillis } ?: 1L

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Text("Most Used Apps Today", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        if (apps.isEmpty()) {
            Text("No usage data available for today.", style = MaterialTheme.typography.bodyMedium)
        } else {
            apps.take(5).forEach { app ->
                AnimatedUsageBar(app = app, maxUsage = maxUsage)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun AnimatedUsageBar(app: AppInfo, maxUsage: Long) {
    val usageFraction = if (maxUsage > 0) {
        (app.usageTodayMillis.toFloat() / maxUsage.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }

    val animatedFraction by animateFloatAsState(targetValue = usageFraction, label = "barAnimation")

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(app.appName, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
            Text(formatMillisToHoursMinutes(app.usageTodayMillis), style = MaterialTheme.typography.bodySmall)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(5.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedFraction)
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StreakCard(streak: Int, onClick: () -> Unit = {}) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "Current Streak",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    "$streak ${if (streak == 1) "day" else "days"}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                "🔥",
                style = MaterialTheme.typography.displayLarge,
                modifier = Modifier.size(48.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalProgressCard(
    totalUsage: Long,
    goalMinutes: Int,
    progress: Float,
    onClick: () -> Unit = {}
) {
    val usageMinutes = (totalUsage / 60000).toInt()
    val progressColor = when {
        progress < 0.7f -> MaterialTheme.colorScheme.primary // Good
        progress < 1.0f -> Color(0xFFFF9800) // Warning
        else -> MaterialTheme.colorScheme.error // Over limit
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Daily Goal Progress",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "${usageMinutes}m of ${goalMinutes}m",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = progressColor
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = progress.coerceIn(0f, 1f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = progressColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            if (progress >= 1.0f) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "⚠️ Goal exceeded",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

private fun formatMillisToHoursMinutes(millis: Long): String {
    if (millis < 60000) return "0m"
    val hours = TimeUnit.MILLISECONDS.toHours(millis)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
    return when {
        hours > 0 -> "${hours}h ${minutes}m"
        else -> "${minutes}m"
    }
}
