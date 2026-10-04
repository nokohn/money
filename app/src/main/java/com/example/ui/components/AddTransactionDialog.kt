package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.data.model.AccountEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.TransactionEntity
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.LaoPrimary
import com.example.util.DateUtil

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddTransactionDialog(
    isOpen: Boolean,
    initialType: String = "EXPENSE",
    existingTransaction: TransactionEntity? = null,
    categories: List<CategoryEntity>,
    accounts: List<AccountEntity>,
    currency: String,
    language: String = "LAO",
    onDismiss: () -> Unit,
    onSave: (TransactionEntity) -> Unit
) {
    if (!isOpen) return

    var type by remember(existingTransaction, initialType) {
        mutableStateOf(existingTransaction?.type ?: initialType)
    }
    var amountText by remember(existingTransaction) {
        mutableStateOf(existingTransaction?.amount?.let { if (it % 1.0 == 0.0) it.toLong().toString() else it.toString() } ?: "")
    }
    var selectedCategoryId by remember(existingTransaction, categories) {
        mutableStateOf(existingTransaction?.categoryId ?: categories.firstOrNull { it.type == type }?.id ?: 0L)
    }
    var selectedAccountId by remember(existingTransaction, accounts) {
        mutableStateOf(existingTransaction?.accountId ?: accounts.firstOrNull { it.isDefault }?.id ?: accounts.firstOrNull()?.id ?: 0L)
    }
    var dateText by remember(existingTransaction) {
        mutableStateOf(existingTransaction?.date ?: DateUtil.todayString())
    }
    var timeText by remember(existingTransaction) {
        mutableStateOf(existingTransaction?.time ?: DateUtil.currentTimeString())
    }
    var noteText by remember(existingTransaction) {
        mutableStateOf(existingTransaction?.note ?: "")
    }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val filteredCategories = remember(categories, type) {
        categories.filter { it.type == type }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("add_transaction_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (existingTransaction != null) {
                            if (language == "LAO") "ແກ້ໄຂທຸລະກຳ" else "Edit Transaction"
                        } else {
                            if (language == "LAO") "ບັນທຶກທຸລະກຳໃໝ່" else "New Transaction"
                        },
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Income / Expense Tab Selector
                TabRow(
                    selectedTabIndex = if (type == "EXPENSE") 0 else 1,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Tab(
                        selected = type == "EXPENSE",
                        onClick = {
                            type = "EXPENSE"
                            selectedCategoryId = categories.firstOrNull { it.type == "EXPENSE" }?.id ?: 0L
                        },
                        text = {
                            Text(
                                text = if (language == "LAO") "ລາຍຈ່າຍ (Expense)" else "Expense",
                                fontWeight = FontWeight.Bold,
                                color = if (type == "EXPENSE") ExpenseRed else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        modifier = Modifier.testTag("tab_expense")
                    )
                    Tab(
                        selected = type == "INCOME",
                        onClick = {
                            type = "INCOME"
                            selectedCategoryId = categories.firstOrNull { it.type == "INCOME" }?.id ?: 0L
                        },
                        text = {
                            Text(
                                text = if (language == "LAO") "ລາຍຮັບ (Income)" else "Income",
                                fontWeight = FontWeight.Bold,
                                color = if (type == "INCOME") IncomeGreen else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        modifier = Modifier.testTag("tab_income")
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Amount Field
                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) {
                            amountText = it
                            errorMessage = null
                        }
                    },
                    label = { Text(if (language == "LAO") "ຈຳນວນເງິນ ($currency)" else "Amount ($currency)") },
                    placeholder = { Text("0") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_transaction_amount"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Category Selector Section
                Text(
                    text = if (language == "LAO") "ໝວດໝູ່ (Category):" else "Category:",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    filteredCategories.forEach { cat ->
                        val isSelected = selectedCategoryId == cat.id
                        Surface(
                            onClick = { selectedCategoryId = cat.id },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) LaoPrimary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.testTag("category_chip_${cat.id}")
                        ) {
                            Text(
                                text = if (language == "LAO") cat.nameLao else cat.nameEng,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal),
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Account / Wallet Selector
                Text(
                    text = if (language == "LAO") "ບັນຊີ / ກະເປົາເງິນ (Account):" else "Account / Wallet:",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    accounts.forEach { acc ->
                        val isSelected = selectedAccountId == acc.id
                        Surface(
                            onClick = { selectedAccountId = acc.id },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) LaoPrimary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("account_chip_${acc.id}")
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = acc.name,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Date & Time Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = dateText,
                        onValueChange = { dateText = it },
                        label = { Text(if (language == "LAO") "ວັນທີ" else "Date") },
                        leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("input_transaction_date"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = timeText,
                        onValueChange = { timeText = it },
                        label = { Text(if (language == "LAO") "ເວລາ" else "Time") },
                        leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_transaction_time"),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Note Field
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text(if (language == "LAO") "ໝາຍເຫດ (Note)" else "Note (Optional)") },
                    placeholder = { Text(if (language == "LAO") "ລາຍລະອຽດເພີ່ມເຕີມ..." else "Add note...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_transaction_note"),
                    shape = RoundedCornerShape(12.dp)
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Submit Button
                Button(
                    onClick = {
                        val parsedAmount = amountText.toDoubleOrNull()
                        if (parsedAmount == null || parsedAmount <= 0) {
                            errorMessage = if (language == "LAO") "ກະລຸນາປ້ອນຈຳນວນເງິນທີ່ຖືກຕ້ອງ" else "Please enter a valid amount"
                            return@Button
                        }
                        if (selectedCategoryId == 0L) {
                            errorMessage = if (language == "LAO") "ກະລຸນາເລືອກໝວດໝູ່" else "Please select a category"
                            return@Button
                        }
                        if (selectedAccountId == 0L) {
                            errorMessage = if (language == "LAO") "ກະລຸນາເລືອກບັນຊີ" else "Please select an account"
                            return@Button
                        }

                        val transaction = TransactionEntity(
                            id = existingTransaction?.id ?: 0L,
                            userId = existingTransaction?.userId ?: 0L,
                            accountId = selectedAccountId,
                            categoryId = selectedCategoryId,
                            type = type,
                            amount = parsedAmount,
                            date = dateText.trim(),
                            time = timeText.trim(),
                            note = noteText.trim()
                        )
                        onSave(transaction)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("save_transaction_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (type == "EXPENSE") ExpenseRed else IncomeGreen
                    )
                ) {
                    Text(
                        text = if (language == "LAO") "ບັນທຶກ (Save)" else "Save",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}
