package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TransactionEntity
import com.example.ui.components.TransactionItemCard
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.LaoPrimary
import com.example.ui.viewmodel.FinanceViewModel
import com.example.util.CurrencyFormatter

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExpenseScreen(
    viewModel: FinanceViewModel,
    onAddExpense: () -> Unit,
    onEditTransaction: (TransactionEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val transactions by viewModel.transactions.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val currency by viewModel.currency.collectAsState()
    val language by viewModel.language.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf<Long?>(null) }

    val expenseCategories = remember(categories) {
        categories.filter { it.type == "EXPENSE" }
    }

    val expenseTransactions = remember(transactions, searchQuery, selectedCategoryId) {
        transactions.filter { item ->
            val isExpense = item.transaction.type == "EXPENSE"
            val matchCategory = selectedCategoryId == null || item.transaction.categoryId == selectedCategoryId
            val matchSearch = searchQuery.isBlank() ||
                item.transaction.note.contains(searchQuery, ignoreCase = true) ||
                (item.category?.nameLao?.contains(searchQuery, ignoreCase = true) == true) ||
                (item.category?.nameEng?.contains(searchQuery, ignoreCase = true) == true) ||
                (item.account?.name?.contains(searchQuery, ignoreCase = true) == true)

            isExpense && matchCategory && matchSearch
        }
    }

    val totalFilteredExpense = remember(expenseTransactions) {
        expenseTransactions.sumOf { it.transaction.amount }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddExpense,
                containerColor = ExpenseRed,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("fab_add_expense")
            ) {
                Row(modifier = Modifier.padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.size(6.dp))
                    Text("- ລາຍຈ່າຍ", fontWeight = FontWeight.Bold)
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { paddingVals ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingVals)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                // Banner Total Expense Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = ExpenseRed.copy(alpha = 0.12f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = if (language == "LAO") "ລວມລາຍຈ່າຍທັງໝົດ" else "Total Expenses",
                                style = MaterialTheme.typography.titleMedium,
                                color = ExpenseRed,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = CurrencyFormatter.format(totalFilteredExpense, currency),
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = ExpenseRed
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.TrendingDown,
                            contentDescription = null,
                            tint = ExpenseRed,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(if (language == "LAO") "ຄົ້ນຫາລາຍຈ່າຍ..." else "Search expenses...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("expense_search_bar"),
                    shape = RoundedCornerShape(14.dp)
                )
            }

            // Category Filter Chips
            item {
                Column {
                    Text(
                        text = if (language == "LAO") "ກັ່ນຕອງຕາມໝວດໝູ່:" else "Filter by category:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            onClick = { selectedCategoryId = null },
                            shape = RoundedCornerShape(10.dp),
                            color = if (selectedCategoryId == null) LaoPrimary else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = if (language == "LAO") "ທັງໝົດ (All)" else "All",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = if (selectedCategoryId == null) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (selectedCategoryId == null) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        expenseCategories.forEach { cat ->
                            val isSelected = selectedCategoryId == cat.id
                            Surface(
                                onClick = { selectedCategoryId = if (isSelected) null else cat.id },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) LaoPrimary else MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = if (language == "LAO") cat.nameLao else cat.nameEng,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // List of Expense items
            if (expenseTransactions.isEmpty()) {
                item {
                    Text(
                        text = if (language == "LAO") "ບໍ່ພົບຂໍ້ມູນລາຍຈ່າຍ" else "No expense records found",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 24.dp)
                    )
                }
            } else {
                items(expenseTransactions, key = { it.transaction.id }) { item ->
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
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }
}
