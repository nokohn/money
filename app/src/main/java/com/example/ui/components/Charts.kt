package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.LaoPrimary
import com.example.ui.theme.WarningOrange
import com.example.util.CurrencyFormatter

data class BarChartItem(
    val label: String,
    val income: Double,
    val expense: Double
)

@Composable
fun IncomeExpenseBarChart(
    items: List<BarChartItem>,
    currency: String = "LAK",
    modifier: Modifier = Modifier
) {
    if (items.isEmpty()) return

    val maxVal = remember(items) {
        val maxIn = items.maxOfOrNull { it.income } ?: 1.0
        val maxEx = items.maxOfOrNull { it.expense } ?: 1.0
        maxOf(maxIn, maxEx, 1000.0)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ລາຍຮັບ vs ລາຍຈ່າຍ (Income vs Expense)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Legend
            Row(
                modifier = Modifier.padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp).background(IncomeGreen, CircleShape))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("ລາຍຮັບ (Income)", style = MaterialTheme.typography.bodySmall)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp).background(ExpenseRed, CircleShape))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("ລາຍຈ່າຍ (Expense)", style = MaterialTheme.typography.bodySmall)
                }
            }

            // Canvas Bar Chart
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .padding(vertical = 8.dp)
            ) {
                val chartHeight = size.height - 30.dp.toPx()
                val slotWidth = size.width / items.size
                val barWidth = (slotWidth * 0.28f).coerceAtMost(24.dp.toPx())

                // Draw horizontal guide lines
                val gridColor = Color.LightGray.copy(alpha = 0.35f)
                drawLine(gridColor, Offset(0f, chartHeight), Offset(size.width, chartHeight), strokeWidth = 1.dp.toPx())
                drawLine(gridColor, Offset(0f, chartHeight * 0.5f), Offset(size.width, chartHeight * 0.5f), strokeWidth = 1.dp.toPx())

                items.forEachIndexed { index, item ->
                    val centerX = slotWidth * index + slotWidth / 2f
                    val incomeHeight = ((item.income / maxVal) * chartHeight).toFloat().coerceIn(0f, chartHeight)
                    val expenseHeight = ((item.expense / maxVal) * chartHeight).toFloat().coerceIn(0f, chartHeight)

                    // Income bar (green)
                    val inLeft = centerX - barWidth - 2.dp.toPx()
                    drawRoundRect(
                        color = IncomeGreen,
                        topLeft = Offset(inLeft, chartHeight - incomeHeight),
                        size = Size(barWidth, incomeHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx(), 6.dp.toPx())
                    )

                    // Expense bar (red)
                    val exLeft = centerX + 2.dp.toPx()
                    drawRoundRect(
                        color = ExpenseRed,
                        topLeft = Offset(exLeft, chartHeight - expenseHeight),
                        size = Size(barWidth, expenseHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx(), 6.dp.toPx())
                    )
                }
            }

            // Labels row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                items.forEach { item ->
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

data class CategorySpendItem(
    val categoryName: String,
    val amount: Double,
    val color: Color,
    val percent: Float
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoryDonutChart(
    items: List<CategorySpendItem>,
    totalExpense: Double,
    currency: String = "LAK",
    modifier: Modifier = Modifier
) {
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(items) {
        animatedProgress.animateTo(1f, animationSpec = tween(700))
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "ລາຍຈ່າຍຕາມໝວດໝູ່ (Expense by Category)",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (items.isEmpty() || totalExpense <= 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "ຍັງບໍ່ມີຂໍ້ມູນລາຍຈ່າຍ (No expense data)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    // Donut Chart with Center Text
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(170.dp)) {
                        Canvas(modifier = Modifier.size(160.dp)) {
                            val strokeWidth = 26.dp.toPx()
                            var startAngle = -90f

                            items.forEach { cat ->
                                val sweepAngle = (cat.percent / 100f) * 360f * animatedProgress.value
                                drawArc(
                                    color = cat.color,
                                    startAngle = startAngle,
                                    sweepAngle = sweepAngle,
                                    useCenter = false,
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                )
                                startAngle += sweepAngle
                            }
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "ລວມທັງໝົດ",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = CurrencyFormatter.format(totalExpense, currency),
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Legend Grid
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items.forEach { item ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(10.dp).background(item.color, CircleShape))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${item.categoryName} (${item.percent.toInt()}%)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MonthlyTrendLineChart(
    incomePoints: List<Double>,
    expensePoints: List<Double>,
    labels: List<String>,
    currency: String = "LAK",
    modifier: Modifier = Modifier
) {
    if (labels.isEmpty()) return

    val maxVal = remember(incomePoints, expensePoints) {
        val maxIn = incomePoints.maxOrNull() ?: 1.0
        val maxEx = expensePoints.maxOrNull() ?: 1.0
        maxOf(maxIn, maxEx, 1000.0)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "ທ່າອ່ຽງລາຍຮັບ-ລາຍຈ່າຍ (Monthly Trends)",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                modifier = Modifier.padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(12.dp, 3.dp).background(IncomeGreen))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("ລາຍຮັບ", style = MaterialTheme.typography.bodySmall)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(12.dp, 3.dp).background(ExpenseRed))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("ລາຍຈ່າຍ", style = MaterialTheme.typography.bodySmall)
                }
            }

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .padding(vertical = 8.dp)
            ) {
                val chartHeight = size.height - 20.dp.toPx()
                val stepX = if (labels.size > 1) size.width / (labels.size - 1) else size.width

                // Income Line
                val incomePath = Path()
                incomePoints.forEachIndexed { i, value ->
                    val x = i * stepX
                    val y = chartHeight - ((value / maxVal) * chartHeight).toFloat()
                    if (i == 0) incomePath.moveTo(x, y) else incomePath.lineTo(x, y)
                }
                drawPath(incomePath, color = IncomeGreen, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))

                // Expense Line
                val expensePath = Path()
                expensePoints.forEachIndexed { i, value ->
                    val x = i * stepX
                    val y = chartHeight - ((value / maxVal) * chartHeight).toFloat()
                    if (i == 0) expensePath.moveTo(x, y) else expensePath.lineTo(x, y)
                }
                drawPath(expensePath, color = ExpenseRed, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                labels.forEach { label ->
                    Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
