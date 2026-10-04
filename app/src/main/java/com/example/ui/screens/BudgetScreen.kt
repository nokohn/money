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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.BudgetWithCategory
import com.example.ui.components.BudgetProgressBar
import com.example.ui.theme.LaoPrimary
import com.example.ui.viewmodel.FinanceViewModel
import com.example.util.DateUtil

@Composable
fun BudgetScreen(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    val budgets by viewModel.budgets.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val currency by viewModel.currency.collectAsState()
    val language by viewModel.language.collectAsState()

    var showAddBudgetDialog by remember { mutableStateOf(false) }

    // Dialog form state
    var selectedCategoryId by remember { mutableStateOf<Long?>(null) } // null = Overall
    var budgetAmountInput by remember { mutableStateOf("") }
    var selectedPeriod by remember { mutableStateOf("MONTHLY") } // DAILY, WEEKLY, MONTHLY

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddBudgetDialog = true },
                containerColor = LaoPrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("fab_add_budget")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("+ ສ້າງງົບປະມານ", fontWeight = FontWeight.Bold)
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (language == "LAO") "ການຈັດການງົບປະມານ (Budget Management)" else "Budget Management",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (language == "LAO") "ລະບົບຈະແຈ້ງເຕືອນອັດຕະໂນມັດເມື່ອໃຊ້ຈ່າຍຮອດ 80% ແລະ ເກີນ 100%"
                           else "System notifies at 80% and 100% threshold automatically.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (budgets.isEmpty()) {
                item {
                    Text(
                        text = if (language == "LAO") "ຍັງບໍ່ມີການຕັ້ງງົບປະມານ. ກົດປຸ່ມ + ເພື່ອເລີ່ມຕົ້ນ."
                               else "No budgets set. Click + to create one.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 20.dp)
                    )
                }
            } else {
                items(budgets, key = { it.budget.id }) { item ->
                    val ym = item.budget.yearMonth
                    val expenseAmount = if (item.budget.categoryId != null) {
                        transactions.filter {
                            it.transaction.type == "EXPENSE" &&
                            it.transaction.categoryId == item.budget.categoryId &&
                            it.transaction.date.startsWith(ym)
                        }.sumOf { it.transaction.amount }
                    } else {
                        transactions.filter {
                            it.transaction.type == "EXPENSE" &&
                            it.transaction.date.startsWith(ym)
                        }.sumOf { it.transaction.amount }
                    }

                    val catName = if (item.budget.categoryId == null) {
                        if (language == "LAO") "ງົບປະມານລວມທັງໝົດ (Overall)" else "Overall Budget"
                    } else {
                        if (language == "LAO") item.category?.nameLao ?: "ທົ່ວໄປ" else item.category?.nameEng ?: "General"
                    }

                    val periodLabel = when (item.budget.periodType) {
                        "DAILY" -> if (language == "LAO") "ລາຍວັນ (Daily)" else "Daily"
                        "WEEKLY" -> if (language == "LAO") "ລາຍອາທິດ (Weekly)" else "Weekly"
                        else -> if (language == "LAO") "ລາຍເດືອນ (${DateUtil.formatMonthYear(ym, language)})" else "Monthly (${DateUtil.formatMonthYear(ym, language)})"
                    }

                    BudgetProgressBar(
                        title = catName,
                        periodLabel = periodLabel,
                        budgetAmount = item.budget.amount,
                        usedAmount = expenseAmount,
                        currency = currency,
                        onDelete = { viewModel.deleteBudget(item.budget) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }

    // Add Budget Dialog
    if (showAddBudgetDialog) {
        Dialog(onDismissRequest = { showAddBudgetDialog = false }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (language == "LAO") "ສ້າງງົບປະມານໃໝ່" else "New Budget",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        IconButton(onClick = { showAddBudgetDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Period Selector (Daily, Weekly, Monthly)
                    Text(
                        text = if (language == "LAO") "ໄລຍະເວລາງົບປະມານ:" else "Budget Period:",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("MONTHLY" to "ລາຍເດືອນ", "WEEKLY" to "ລາຍອາທິດ", "DAILY" to "ລາຍວັນ").forEach { (code, label) ->
                            val isSel = selectedPeriod == code
                            Surface(
                                onClick = { selectedPeriod = code },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSel) LaoPrimary else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = label,
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Category Selector
                    Text(
                        text = if (language == "LAO") "ເລືອກໝວດໝູ່ (Category):" else "Select Category:",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        onClick = { selectedCategoryId = null },
                        shape = RoundedCornerShape(10.dp),
                        color = if (selectedCategoryId == null) LaoPrimary else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (language == "LAO") "🌟 ງົບປະມານລວມທັງໝົດ (Overall Budget)" else "🌟 Overall Budget",
                            modifier = Modifier.padding(10.dp),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (selectedCategoryId == null) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    categories.filter { it.type == "EXPENSE" }.take(5).forEach { cat ->
                        val isSel = selectedCategoryId == cat.id
                        Surface(
                            onClick = { selectedCategoryId = cat.id },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSel) LaoPrimary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                        ) {
                            Text(
                                text = cat.nameLao,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal),
                                color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Amount input
                    OutlinedTextField(
                        value = budgetAmountInput,
                        onValueChange = { budgetAmountInput = it },
                        label = { Text(if (language == "LAO") "ຈຳນວນເງິນງົບປະມານ ($currency)" else "Budget Amount ($currency)") },
                        placeholder = { Text("2,000,000") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            val amt = budgetAmountInput.toDoubleOrNull()
                            if (amt != null && amt > 0) {
                                viewModel.addBudget(selectedCategoryId, amt, selectedPeriod)
                                showAddBudgetDialog = false
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = LaoPrimary)
                    ) {
                        Text(if (language == "LAO") "ບັນທຶກງົບປະມານ" else "Save Budget", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}
