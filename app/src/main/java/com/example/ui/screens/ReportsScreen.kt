package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CategoryDonutChart
import com.example.ui.theme.CriticalRed
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.LaoPrimary
import com.example.ui.viewmodel.FinanceViewModel
import com.example.util.CurrencyFormatter
import com.example.util.DateUtil

@Composable
fun ReportsScreen(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val transactions by viewModel.transactions.collectAsState()
    val monthlyBudget by viewModel.monthlyBudgetAmount.collectAsState()
    val categorySpendItems by viewModel.categorySpendItems.collectAsState()
    val currency by viewModel.currency.collectAsState()
    val language by viewModel.language.collectAsState()

    var selectedPeriodTab by remember { mutableStateOf(2) } // 0: Daily, 1: Weekly, 2: Monthly, 3: Annual

    val currentYm = DateUtil.currentYearMonth()
    val today = DateUtil.todayString()

    val filteredTxs = remember(transactions, selectedPeriodTab) {
        when (selectedPeriodTab) {
            0 -> transactions.filter { it.transaction.date == today }
            1 -> {
                // Last 7 days
                val cal = java.util.Calendar.getInstance()
                cal.add(java.util.Calendar.DAY_OF_YEAR, -7)
                val sevenDaysAgo = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(cal.time)
                transactions.filter { it.transaction.date >= sevenDaysAgo }
            }
            2 -> transactions.filter { it.transaction.date.startsWith(currentYm) }
            else -> {
                val year = currentYm.take(4)
                transactions.filter { it.transaction.date.startsWith(year) }
            }
        }
    }

    val totalIncome = remember(filteredTxs) {
        filteredTxs.filter { it.transaction.type == "INCOME" }.sumOf { it.transaction.amount }
    }
    val totalExpense = remember(filteredTxs) {
        filteredTxs.filter { it.transaction.type == "EXPENSE" }.sumOf { it.transaction.amount }
    }
    val netBalance = totalIncome - totalExpense

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("reports_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (language == "LAO") "ລາຍງານການເງິນ (Financial Reports)" else "Financial Reports",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Period Tabs
        item {
            TabRow(
                selectedTabIndex = selectedPeriodTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ) {
                listOf(
                    "ລາຍວັນ" to "Daily",
                    "ລາຍອາທິດ" to "Weekly",
                    "ລາຍເດືອນ" to "Monthly",
                    "ລາຍປີ" to "Annual"
                ).forEachIndexed { index, (lao, eng) ->
                    Tab(
                        selected = selectedPeriodTab == index,
                        onClick = { selectedPeriodTab = index },
                        text = { Text(text = if (language == "LAO") lao else eng, fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                    )
                }
            }
        }

        // Summary Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = if (language == "LAO") "ພາບລວມລາຍຮັບ-ລາຍຈ່າຍ" else "Executive Summary",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("ລາຍຮັບລວມ (Total Income)", style = MaterialTheme.typography.bodySmall)
                            Text(
                                text = CurrencyFormatter.format(totalIncome, currency),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = IncomeGreen
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("ລາຍຈ່າຍລວມ (Total Expense)", style = MaterialTheme.typography.bodySmall)
                            Text(
                                text = CurrencyFormatter.format(totalExpense, currency),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = ExpenseRed
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ຍອດເງິນສຸດທິ (Net Balance):",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = CurrencyFormatter.format(netBalance, currency),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = if (netBalance >= 0) IncomeGreen else CriticalRed
                            )
                        )
                    }
                }
            }
        }

        // Category Spend Chart
        item {
            CategoryDonutChart(
                items = categorySpendItems,
                totalExpense = totalExpense,
                currency = currency
            )
        }

        // Budget vs Actual Comparison Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = if (language == "LAO") "ງົບປະມານ vs ການໃຊ້ຈ່າຍຕົວຈິງ (Budget vs Actual)" else "Budget vs Actual",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("ງົບປະມານ (Budget)", style = MaterialTheme.typography.bodySmall)
                            Text(
                                text = CurrencyFormatter.format(monthlyBudget, currency),
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        Column {
                            Text("ໃຊ້ຕົວຈິງ (Actual)", style = MaterialTheme.typography.bodySmall)
                            Text(
                                text = CurrencyFormatter.format(totalExpense, currency),
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                color = if (totalExpense > monthlyBudget) CriticalRed else ExpenseRed
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("ຍອດຄົງເຫຼືອ (Variance)", style = MaterialTheme.typography.bodySmall)
                            val diff = monthlyBudget - totalExpense
                            Text(
                                text = CurrencyFormatter.format(diff, currency),
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (diff >= 0) IncomeGreen else CriticalRed
                                )
                            )
                        }
                    }
                }
            }
        }

        // Export Actions Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        val csv = viewModel.exportCsvData()
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, csv)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Export Report CSV"))
                    },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LaoPrimary)
                ) {
                    Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.size(6.dp))
                    Text("Export CSV")
                }

                OutlinedButton(
                    onClick = {
                        val summaryText = """
                            📊 ບົດລາຍງານການເງິນ: ລາຍຮັບ-ລາຍຈ່າຍ
                            - ລາຍຮັບ: ${CurrencyFormatter.format(totalIncome, currency)}
                            - ລາຍຈ່າຍ: ${CurrencyFormatter.format(totalExpense, currency)}
                            - ຍອດສຸດທິ: ${CurrencyFormatter.format(netBalance, currency)}
                        """.trimIndent()
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, summaryText)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Share Report Summary"))
                    },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.size(6.dp))
                    Text(if (language == "LAO") "ແບ່ງປັນ (Share)" else "Share")
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
