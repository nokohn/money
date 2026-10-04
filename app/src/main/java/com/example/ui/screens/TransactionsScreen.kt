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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import com.example.data.model.TransactionEntity
import com.example.ui.components.SweetAlertType
import com.example.ui.components.TransactionItemCard
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.LaoPrimary
import com.example.ui.viewmodel.FinanceViewModel
import com.example.util.CurrencyFormatter

@Composable
fun TransactionsScreen(
    viewModel: FinanceViewModel,
    onEditTransaction: (TransactionEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val transactions by viewModel.transactions.collectAsState()
    val currency by viewModel.currency.collectAsState()
    val language by viewModel.language.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedTypeFilter by remember { mutableStateOf("ALL") } // ALL, INCOME, EXPENSE
    var isSortAscending by remember { mutableStateOf(false) }

    val filteredList = remember(transactions, searchQuery, selectedTypeFilter, isSortAscending) {
        var list = transactions.filter { item ->
            val matchType = when (selectedTypeFilter) {
                "INCOME" -> item.transaction.type == "INCOME"
                "EXPENSE" -> item.transaction.type == "EXPENSE"
                else -> true
            }
            val q = searchQuery.trim().lowercase()
            val matchSearch = q.isBlank() ||
                item.transaction.note.lowercase().contains(q) ||
                (item.category?.nameLao?.lowercase()?.contains(q) == true) ||
                (item.category?.nameEng?.lowercase()?.contains(q) == true) ||
                (item.account?.name?.lowercase()?.contains(q) == true) ||
                item.transaction.amount.toString().contains(q) ||
                item.transaction.date.contains(q)

            matchType && matchSearch
        }

        if (isSortAscending) {
            list = list.sortedBy { it.transaction.amount }
        } else {
            list = list.sortedByDescending { it.transaction.date + it.transaction.time }
        }
        list
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("transactions_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(if (language == "LAO") "ຄົ້ນຫາທຸລະກຳ (ໝວດໝູ່, ບັນຊີ, ໝາຍເຫດ)..." else "Search transactions...") },
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
                    .testTag("transactions_search_input"),
                shape = RoundedCornerShape(14.dp)
            )
        }

        // Filter Tabs & Sort & Export Row
        item {
            TabRow(
                selectedTabIndex = when (selectedTypeFilter) {
                    "INCOME" -> 1
                    "EXPENSE" -> 2
                    else -> 0
                },
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Tab(
                    selected = selectedTypeFilter == "ALL",
                    onClick = { selectedTypeFilter = "ALL" },
                    text = { Text("ທັງໝົດ (All)", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                )
                Tab(
                    selected = selectedTypeFilter == "INCOME",
                    onClick = { selectedTypeFilter = "INCOME" },
                    text = { Text("ລາຍຮັບ", fontWeight = FontWeight.Bold, color = IncomeGreen, fontSize = 12.sp) }
                )
                Tab(
                    selected = selectedTypeFilter == "EXPENSE",
                    onClick = { selectedTypeFilter = "EXPENSE" },
                    text = { Text("ລາຍຈ່າຍ", fontWeight = FontWeight.Bold, color = ExpenseRed, fontSize = 12.sp) }
                )
            }
        }

        // Sort & CSV Export Buttons
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    onClick = { isSortAscending = !isSortAscending },
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Sort, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.size(4.dp))
                        Text(
                            text = if (isSortAscending) "ຮຽງ: ໜ້ອຍຫາຫຼາຍ" else "ຮຽງ: ໃໝ່ລ່າສຸດ",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                        )
                    }
                }

                Surface(
                    onClick = {
                        val csv = viewModel.exportCsvData()
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, csv)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Export Transactions CSV"))
                    },
                    shape = RoundedCornerShape(10.dp),
                    color = LaoPrimary.copy(alpha = 0.12f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = null, tint = LaoPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.size(4.dp))
                        Text(
                            text = "Export CSV",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = LaoPrimary
                        )
                    }
                }
            }
        }

        // Summary count
        item {
            Text(
                text = if (language == "LAO") "ພົບທັງໝົດ ${filteredList.size} ລາຍການ" else "Showing ${filteredList.size} items",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // List
        if (filteredList.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (language == "LAO") "ບໍ່ມີລາຍການທີ່ຕົງກັບເງື່ອນໄຂ" else "No matching transactions",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(filteredList, key = { it.transaction.id }) { item ->
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
