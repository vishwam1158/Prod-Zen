package com.viz.prodzen.ui.components.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.viz.prodzen.data.models.HourlyUsagePoint
import java.util.Calendar

@Composable
fun HourlyHeatmap(
    data: List<HourlyUsagePoint>,
    modifier: Modifier = Modifier,
    barColor: Color = Color.Blue
) {
    if (data.isEmpty()) return

    val maxUsage = data.maxOfOrNull { it.usageInMillis }?.toFloat() ?: 1f

    Canvas(modifier = modifier.fillMaxWidth().height(200.dp)) {
        val barWidth = size.width / 24
        val height = size.height

        // Draw 24 bars for hours
        for (i in 0..23) {
            // Find data for this hour
            val point = data.find {
                val cal = Calendar.getInstance().apply { timeInMillis = it.hourTimestamp }
                cal.get(Calendar.HOUR_OF_DAY) == i
            }

            val usage = point?.usageInMillis?.toFloat() ?: 0f
            val barHeight = (usage / maxUsage) * height

            drawRect(
                color = barColor.copy(alpha = if (usage > 0) 1f else 0.1f),
                topLeft = Offset(i * barWidth, height - barHeight),
                size = Size(barWidth - 4f, barHeight)
            )
        }
    }
}

