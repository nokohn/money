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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.LaoPrimary
import com.example.ui.viewmodel.FinanceViewModel

@Composable
fun CategoriesScreen(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    val categories by viewModel.categories.collectAsState()
    val language by viewModel.language.collectAsState()

    var selectedTab by remember { mutableStateOf(0) } // 0: Expense, 1: Income
    val activeType = if (selectedTab == 0) "EXPENSE" else "INCOME"

    val filteredCategories = remember(categories, activeType) {
        categories.filter { it.type == activeType }
    }

    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var newNameLao by remember { mutableStateOf("") }
    var newNameEng by remember { mutableStateOf("") }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddCategoryDialog = true },
                containerColor = LaoPrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("fab_add_category")
            ) {
                Row(modifier = Modifier.padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("+ ເພີ່ມໝວດໝູ່", fontWeight = FontWeight.Bold)
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
                Text(
                    text = if (language == "LAO") "ການຈັດການໝວດໝູ່ (Categories)" else "Categories Management",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            item {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("ໝວດໝູ່ລາຍຈ່າຍ (Expense)", fontWeight = FontWeight.Bold, color = ExpenseRed) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("ໝວດໝູ່ລາຍຮັບ (Income)", fontWeight = FontWeight.Bold, color = IncomeGreen) }
                    )
                }
            }

            items(filteredCategories, key = { it.id }) { cat ->
                val catColor = try {
                    Color(android.graphics.Color.parseColor(cat.colorHex))
                } catch (_: Exception) {
                    LaoPrimary
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(catColor.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Category,
                                    contentDescription = null,
                                    tint = catColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = cat.nameLao,
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = cat.nameEng,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        IconButton(onClick = { viewModel.deleteCategory(cat) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ExpenseRed, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }

    if (showAddCategoryDialog) {
        Dialog(onDismissRequest = { showAddCategoryDialog = false }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (language == "LAO") "ເພີ່ມໝວດໝູ່ໃໝ່ ($activeType)" else "New Category ($activeType)",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        IconButton(onClick = { showAddCategoryDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = newNameLao,
                        onValueChange = { newNameLao = it },
                        label = { Text("ຊື່ໝວດໝູ່ພາສາລາວ (Lao Name)") },
                        placeholder = { Text("ເຊັ່ນ: ກາເຟ, ຊື້ຂອງຂວັນ...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = newNameEng,
                        onValueChange = { newNameEng = it },
                        label = { Text("ຊື່ພາສາອັງກິດ (English Name)") },
                        placeholder = { Text("e.g. Coffee, Gifts...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            if (newNameLao.isNotBlank()) {
                                viewModel.addCategory(
                                    nameLao = newNameLao.trim(),
                                    nameEng = if (newNameEng.isNotBlank()) newNameEng.trim() else newNameLao.trim(),
                                    type = activeType
                                )
                                showAddCategoryDialog = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = LaoPrimary)
                    ) {
                        Text("ບັນທຶກໝວດໝູ່", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}
