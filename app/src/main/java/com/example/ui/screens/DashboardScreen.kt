package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TransactionEntity
import com.example.ui.components.AppScreen
import com.example.ui.components.BudgetProgressBar
import com.example.ui.components.CategoryDonutChart
import com.example.ui.components.HeroBalanceCard
import com.example.ui.components.IncomeExpenseBarChart
import com.example.ui.components.MetricSummaryCard
import com.example.ui.components.MonthlyTrendLineChart
import com.example.ui.components.TransactionItemCard
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.LaoPrimary
import com.example.ui.theme.LaoSecondary
import com.example.ui.theme.WarningOrange
import com.example.ui.viewmodel.FinanceViewModel

@Composable
fun DashboardScreen(
    viewModel: FinanceViewModel,
    onNavigate: (AppScreen) -> Unit,
    onAddIncome: () -> Unit,
    onAddExpense: () -> Unit,
    onEditTransaction: (TransactionEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalBalance by viewModel.totalBalance.collectAsState()
    val todayIncome by viewModel.todayIncome.collectAsState()
    val todayExpense by viewModel.todayExpense.collectAsState()
    val monthlyIncome by viewModel.monthlyIncome.collectAsState()
    val monthlyExpense by viewModel.monthlyExpense.collectAsState()
    val monthlyBudget by viewModel.monthlyBudgetAmount.collectAsState()
    val recentTxs by viewModel.recentTransactions.collectAsState()
    val barChartData by viewModel.barChartData.collectAsState()
    val categorySpendItems by viewModel.categorySpendItems.collectAsState()
    val currency by viewModel.currency.collectAsState()
    val language by viewModel.language.collectAsState()

    val budgetRemaining = (monthlyBudget - monthlyExpense).coerceAtLeast(0.0)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("dashboard_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            // Hero Total Balance Card
            HeroBalanceCard(
                currentBalance = totalBalance,
                currency = currency,
                onAddIncomeClick = onAddIncome,
                onAddExpenseClick = onAddExpense
            )
        }

        // Today's Financial Summary Row
        item {
            Text(
                text = if (language == "LAO") "ສະຫຼຸບການເງິນມື້ນີ້ (Today's Summary)" else "Today's Financial Summary",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricSummaryCard(
                    titleLao = "ລາຍຮັບມື້ນີ້",
                    titleEng = "Today Income",
                    amount = todayIncome,
                    currency = currency,
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    accentColor = IncomeGreen,
                    modifier = Modifier.weight(1f).testTag("card_today_income")
                )
                MetricSummaryCard(
                    titleLao = "ລາຍຈ່າຍມື້ນີ້",
                    titleEng = "Today Expense",
                    amount = todayExpense,
                    currency = currency,
                    icon = Icons.AutoMirrored.Filled.TrendingDown,
                    accentColor = ExpenseRed,
                    modifier = Modifier.weight(1f).testTag("card_today_expense")
                )
            }
        }

        // Monthly Financial Summary Row
        item {
            Text(
                text = if (language == "LAO") "ສະຫຼຸບປະຈຳເດືອນ (Monthly Overview)" else "Monthly Financial Overview",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricSummaryCard(
                    titleLao = "ລາຍຮັບເດືອນນີ້",
                    titleEng = "Month Income",
                    amount = monthlyIncome,
                    currency = currency,
                    icon = Icons.Default.CalendarToday,
                    accentColor = LaoPrimary,
                    modifier = Modifier.weight(1f)
                )
                MetricSummaryCard(
                    titleLao = "ລາຍຈ່າຍເດືອນນີ້",
                    titleEng = "Month Expense",
                    amount = monthlyExpense,
                    currency = currency,
                    icon = Icons.Default.DateRange,
                    accentColor = ExpenseRed,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Budget Card
        item {
            BudgetProgressBar(
                title = if (language == "LAO") "ງົບປະມານລວມປະຈຳເດືອນ" else "Total Monthly Budget",
                periodLabel = if (language == "LAO") "ເດືອນປັດຈຸບັນ (Current Month)" else "Current Month",
                budgetAmount = monthlyBudget,
                usedAmount = monthlyExpense,
                currency = currency,
                onEdit = { onNavigate(AppScreen.BUDGET) },
                onDelete = { onNavigate(AppScreen.BUDGET) }
            )
        }

        // Charts Section
        item {
            // A. Dual Bar Chart (Income vs Expense)
            IncomeExpenseBarChart(
                items = barChartData,
                currency = currency
            )
        }

        item {
            // B. Donut Chart (Category Spending)
            CategoryDonutChart(
                items = categorySpendItems,
                totalExpense = monthlyExpense,
                currency = currency
            )
        }

        item {
            // C. Monthly Trend Line Chart
            val dummyTrendIncome = listOf(monthlyIncome * 0.7, monthlyIncome * 0.85, monthlyIncome * 0.9, monthlyIncome)
            val dummyTrendExpense = listOf(monthlyExpense * 0.6, monthlyExpense * 0.75, monthlyExpense * 0.88, monthlyExpense)
            val trendLabels = listOf("W1", "W2", "W3", "W4")
            MonthlyTrendLineChart(
                incomePoints = dummyTrendIncome,
                expensePoints = dummyTrendExpense,
                labels = trendLabels,
                currency = currency
            )
        }

        // Recent Transactions Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (language == "LAO") "ທຸລະກຳຫຼ້າສຸດ (Recent Transactions)" else "Recent Transactions",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                TextButton(onClick = { onNavigate(AppScreen.TRANSACTIONS) }) {
                    Text(
                        text = if (language == "LAO") "ເບິ່ງທັງໝົດ >" else "View All >",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = LaoPrimary
                    )
                }
            }
        }

        // Recent Transactions List
        if (recentTxs.isEmpty()) {
            item {
                Text(
                    text = if (language == "LAO") "ຍັງບໍ່ມີລາຍການທຸລະກຳ" else "No recent transactions",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(recentTxs, key = { it.transaction.id }) { item ->
                TransactionItemCard(
                    item = item,
                    currency = currency,
                    language = language,
                    onEditClick = { onEditTransaction(item.transaction) },
                    onDeleteClick = { viewModel.deleteTransaction(item.transaction) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
