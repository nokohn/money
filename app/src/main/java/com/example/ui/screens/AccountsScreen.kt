package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.AccountEntity
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.LaoPrimary
import com.example.ui.viewmodel.FinanceViewModel
import com.example.util.CurrencyFormatter

@Composable
fun AccountsScreen(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    val accounts by viewModel.accounts.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val currency by viewModel.currency.collectAsState()
    val language by viewModel.language.collectAsState()

    var showAddAccountDialog by remember { mutableStateOf(false) }
    var accountNameInput by remember { mutableStateOf("") }
    var accountTypeInput by remember { mutableStateOf("BANK") }
    var accountBalanceInput by remember { mutableStateOf("") }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddAccountDialog = true },
                containerColor = LaoPrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("fab_add_account")
            ) {
                Row(modifier = Modifier.padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("+ ເພີ່ມບັນຊີ", fontWeight = FontWeight.Bold)
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
                    text = if (language == "LAO") "ບັນຊີ ແລະ ກະເປົາເງິນ (Accounts & Wallets)" else "Accounts & Wallets",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (language == "LAO") "ຮອງຮັບ BCEL One, JDB Yes, LDB, ເງິນສົດ ແລະ E-Wallets" else "Supports Cash, BCEL One, JDB, LDB, Bank & Wallets",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            items(accounts, key = { it.id }) { acc ->
                val accTxs = transactions.filter { it.transaction.accountId == acc.id }
                val accIncome = accTxs.filter { it.transaction.type == "INCOME" }.sumOf { it.transaction.amount }
                val accExpense = accTxs.filter { it.transaction.type == "EXPENSE" }.sumOf { it.transaction.amount }

                val cardColor = try {
                    Color(android.graphics.Color.parseColor(acc.colorHex))
                } catch (_: Exception) {
                    LaoPrimary
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .background(cardColor.copy(alpha = 0.15f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccountBalance,
                                        contentDescription = null,
                                        tint = cardColor,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = acc.name,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = acc.type,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            if (!acc.isDefault) {
                                IconButton(onClick = { viewModel.deleteAccount(acc) }, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete account", tint = ExpenseRed, modifier = Modifier.size(18.dp))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = CurrencyFormatter.format(acc.balance, currency),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = cardColor
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "ລາຍຮັບ: +${CurrencyFormatter.format(accIncome, currency)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = IncomeGreen
                            )
                            Text(
                                text = "ລາຍຈ່າຍ: -${CurrencyFormatter.format(accExpense, currency)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = ExpenseRed
                            )
                            Text(
                                text = "${accTxs.size} ລາຍການ",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }

    // Add Account Dialog
    if (showAddAccountDialog) {
        Dialog(onDismissRequest = { showAddAccountDialog = false }) {
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
                            text = if (language == "LAO") "ເພີ່ມບັນຊີ / ກະເປົາເງິນ" else "New Account",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        IconButton(onClick = { showAddAccountDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = accountNameInput,
                        onValueChange = { accountNameInput = it },
                        label = { Text(if (language == "LAO") "ຊື່ບັນຊີ (Account Name)" else "Account Name") },
                        placeholder = { Text("ເຊັ່ນ: BCEL One, ເງິນສົດ, JDB...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = accountBalanceInput,
                        onValueChange = { accountBalanceInput = it },
                        label = { Text(if (language == "LAO") "ຍອດເງິນເລີ່ມຕົ້ນ ($currency)" else "Initial Balance ($currency)") },
                        placeholder = { Text("0") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            val bal = accountBalanceInput.toDoubleOrNull() ?: 0.0
                            if (accountNameInput.isNotBlank()) {
                                viewModel.addAccount(accountNameInput.trim(), accountTypeInput, bal)
                                showAddAccountDialog = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = LaoPrimary)
                    ) {
                        Text(if (language == "LAO") "ບັນທຶກບັນຊີ" else "Save Account", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}
