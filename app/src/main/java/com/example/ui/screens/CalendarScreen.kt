package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TransactionEntity
import com.example.ui.components.TransactionItemCard
import com.example.ui.theme.CriticalRed
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.LaoPrimary
import com.example.ui.viewmodel.FinanceViewModel
import com.example.util.CurrencyFormatter
import com.example.util.DateUtil

@Composable
fun CalendarScreen(
    viewModel: FinanceViewModel,
    onEditTransaction: (TransactionEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val calendarDays by viewModel.calendarDays.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val currency by viewModel.currency.collectAsState()
    val language by viewModel.language.collectAsState()

    var selectedDateString by remember { mutableStateOf(DateUtil.todayString()) }

    val dayTransactions = remember(transactions, selectedDateString) {
        transactions.filter { it.transaction.date == selectedDateString }
    }

    val dayIncome = remember(dayTransactions) {
        dayTransactions.filter { it.transaction.type == "INCOME" }.sumOf { it.transaction.amount }
    }
    val dayExpense = remember(dayTransactions) {
        dayTransactions.filter { it.transaction.type == "EXPENSE" }.sumOf { it.transaction.amount }
    }
    val dayNet = dayIncome - dayExpense

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("calendar_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (language == "LAO") "ປະຕິທິນການເງິນ (Financial Calendar)" else "Financial Calendar",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = DateUtil.formatMonthYear(DateUtil.currentYearMonth(), language),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Calendar Days Grid Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Weekday headers
                    val headers = if (language == "LAO") listOf("ທິດ", "ຈັນ", "ອັງ", "ພຸດ", "ພະ", "ສຸກ", "ເສົາ")
                                  else listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        headers.forEach { h ->
                            Text(
                                text = h,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Days in chunks of 7
                    calendarDays.chunked(7).forEach { week ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            week.forEach { day ->
                                val isSelected = day.dateString == selectedDateString
                                val hasActivity = day.income > 0 || day.expense > 0

                                Surface(
                                    onClick = { selectedDateString = day.dateString },
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) LaoPrimary else if (hasActivity) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(2.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(vertical = 6.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = day.dayNumber.toString(),
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontWeight = if (isSelected || hasActivity) FontWeight.Bold else FontWeight.Normal
                                            ),
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                        )

                                        if (hasActivity) {
                                            Row(modifier = Modifier.padding(top = 2.dp)) {
                                                if (day.income > 0) {
                                                    Box(modifier = Modifier.size(5.dp).background(IncomeGreen, CircleShape))
                                                }
                                                if (day.expense > 0) {
                                                    Spacer(modifier = Modifier.size(2.dp))
                                                    Box(modifier = Modifier.size(5.dp).background(ExpenseRed, CircleShape))
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Fill remaining cells if week < 7
                            repeat(7 - week.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }

        // Daily Summary Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = if (language == "LAO") "ສະຫຼຸບປະຈຳວັນ: ${DateUtil.formatDate(selectedDateString, language)}"
                               else "Daily Summary: ${DateUtil.formatDate(selectedDateString, language)}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("ລາຍຮັບ (Income)", style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = CurrencyFormatter.format(dayIncome, currency),
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                color = IncomeGreen
                            )
                        }

                        Column {
                            Text("ລາຍຈ່າຍ (Expense)", style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = CurrencyFormatter.format(dayExpense, currency),
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                color = ExpenseRed
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("ຍອດຄົງເຫຼືອ (Balance)", style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = CurrencyFormatter.format(dayNet, currency),
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                color = if (dayNet >= 0) IncomeGreen else CriticalRed
                            )
                        }
                    }
                }
            }
        }

        // Transactions on selected date
        item {
            Text(
                text = if (language == "LAO") "ທຸລະກຳໃນວັນນີ້ (${dayTransactions.size} ລາຍການ)"
                       else "Transactions on this day (${dayTransactions.size})",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        if (dayTransactions.isEmpty()) {
            item {
                Text(
                    text = if (language == "LAO") "ບໍ່ມີທຸລະກຳໃນວັນທີນີ້" else "No transactions recorded for this date",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }
        } else {
            items(dayTransactions, key = { it.transaction.id }) { item ->
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
