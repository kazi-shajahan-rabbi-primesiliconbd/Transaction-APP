package com.rabbi.expensetracker.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import com.rabbi.expensetracker.ui.theme.CategoryPalette

@Composable
fun CategoryPieChart(
    breakdown: Map<String, Double>,
    modifier: Modifier = Modifier
) {
    val total = breakdown.values.sum()
    if (total <= 0.0) {
        Box(modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
            Text("No expenses yet this month", style = MaterialTheme.typography.bodyMedium)
        }
        return
    }

    val entries = breakdown.entries.toList()

    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Canvas(modifier = Modifier.size(140.dp)) {
            var startAngle = -90f
            entries.forEachIndexed { index, entry ->
                val sweep = (entry.value / total * 360.0).toFloat()
                drawArc(
                    color = CategoryPalette[index % CategoryPalette.size],
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = true,
                    size = Size(size.width, size.height)
                )
                startAngle += sweep
            }
        }
        Spacer(Modifier.width(20.dp))
        Column {
            entries.take(6).forEachIndexed { index, entry ->
                val pct = (entry.value / total * 100).toInt()
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(10.dp)
                            .clip(RectangleShape)
                    ) {
                        Canvas(Modifier.size(10.dp)) {
                            drawRect(CategoryPalette[index % CategoryPalette.size])
                        }
                    }
                    Spacer(Modifier.width(8.dp))
                    Text("${entry.key} · $pct%", style = MaterialTheme.typography.bodyMedium)
                }
                Spacer(Modifier.height(4.dp))
            }
        }
    }
}
