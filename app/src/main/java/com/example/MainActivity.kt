package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.AppDatabase
import com.example.data.model.TransactionEntity
import com.example.data.repository.AuthRepository
import com.example.data.repository.FinanceRepository
import com.example.ui.components.AddTransactionDialog
import com.example.ui.components.AppBottomBar
import com.example.ui.components.AppNavigationDrawerContent
import com.example.ui.components.AppScreen
import com.example.ui.components.GlobalSearchDialog
import com.example.ui.components.MainTopAppBar
import com.example.ui.components.SweetAlertDialog
import com.example.ui.screens.AccountsScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.BudgetScreen
import com.example.ui.screens.CalendarScreen
import com.example.ui.screens.CategoriesScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.ExpenseScreen
import com.example.ui.screens.IncomeScreen
import com.example.ui.screens.NotificationsScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TransactionsScreen
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.LaoFinanceTheme
import com.example.ui.viewmodel.AuthUiState
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.FinanceViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var database: AppDatabase
    private lateinit var authRepository: AuthRepository
    private lateinit var financeRepository: FinanceRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        database = AppDatabase.getDatabase(this)
        authRepository = AuthRepository(database.userDao(), database.settingDao())
        financeRepository = FinanceRepository(
            database.accountDao(),
            database.categoryDao(),
            database.transactionDao(),
            database.budgetDao(),
            database.notificationDao(),
            database.settingDao()
        )

        val authViewModel = AuthViewModel(authRepository)

        setContent {
            val authState by authViewModel.uiState.collectAsState()
            val currentUser by authViewModel.currentUser.collectAsState()

            if (currentUser != null && authState is AuthUiState.Authenticated) {
                val user = currentUser!!
                val financeViewModel = remember(user.id) {
                    FinanceViewModel(financeRepository, user.id)
                }

                val isDarkMode by financeViewModel.isDarkMode.collectAsState()

                LaoFinanceTheme(darkTheme = isDarkMode) {
                    MainAppScaffold(
                        user = user,
                        authViewModel = authViewModel,
                        financeViewModel = financeViewModel
                    )
                }
            } else {
                LaoFinanceTheme(darkTheme = false) {
                    AuthScreen(authViewModel = authViewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScaffold(
    user: com.example.data.model.UserEntity,
    authViewModel: AuthViewModel,
    financeViewModel: FinanceViewModel
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var currentScreen by remember { mutableStateOf(AppScreen.DASHBOARD) }
    val language by financeViewModel.language.collectAsState()
    val currency by financeViewModel.currency.collectAsState()
    val unreadNotificationsCount by financeViewModel.unreadNotificationsCount.collectAsState()
    val sweetAlertState by financeViewModel.sweetAlertState.collectAsState()
    val categories by financeViewModel.categories.collectAsState()
    val accounts by financeViewModel.accounts.collectAsState()

    // Transaction Dialog State
    var isTransactionDialogOpen by remember { mutableStateOf(false) }
    var transactionDialogType by remember { mutableStateOf("EXPENSE") }
    var editingTransaction by remember { mutableStateOf<TransactionEntity?>(null) }

    // Quick Action Bottom Sheet for (+) Button
    var showQuickActionSheet by remember { mutableStateOf(false) }
    var isSearchDialogOpen by remember { mutableStateOf(false) }

    BackHandler(enabled = currentScreen != AppScreen.DASHBOARD) {
        currentScreen = AppScreen.DASHBOARD
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppNavigationDrawerContent(
                user = user,
                currentScreen = currentScreen,
                unreadNotificationCount = unreadNotificationsCount,
                language = language,
                onNavigate = { screen ->
                    currentScreen = screen
                },
                onLogout = {
                    authViewModel.logout()
                },
                onClose = {
                    scope.launch { drawerState.close() }
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                MainTopAppBar(
                    title = if (language == "LAO") "ລາຍຮັບ-ລາຍຈ່າຍ" else "Income & Expense",
                    subtitle = if (language == "LAO") currentScreen.titleLao else currentScreen.titleEng,
                    unreadNotificationCount = unreadNotificationsCount,
                    language = language,
                    onMenuClick = { scope.launch { drawerState.open() } },
                    onSearchClick = { isSearchDialogOpen = true },
                    onNotificationsClick = { currentScreen = AppScreen.NOTIFICATIONS },
                    onToggleLanguage = {
                        val nextLang = if (language == "LAO") "ENG" else "LAO"
                        financeViewModel.setLanguage(nextLang)
                    }
                )
            },
            bottomBar = {
                AppBottomBar(
                    currentScreen = currentScreen,
                    onNavigate = { screen -> currentScreen = screen },
                    onFabClick = { showQuickActionSheet = true },
                    unreadNotificationsCount = unreadNotificationsCount,
                    language = language
                )
            },
            modifier = Modifier.fillMaxSize()
        ) { paddingVals ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingVals)
            ) {
                when (currentScreen) {
                    AppScreen.DASHBOARD -> DashboardScreen(
                        viewModel = financeViewModel,
                        onNavigate = { currentScreen = it },
                        onAddIncome = {
                            editingTransaction = null
                            transactionDialogType = "INCOME"
                            isTransactionDialogOpen = true
                        },
                        onAddExpense = {
                            editingTransaction = null
                            transactionDialogType = "EXPENSE"
                            isTransactionDialogOpen = true
                        },
                        onEditTransaction = { tx ->
                            editingTransaction = tx
                            transactionDialogType = tx.type
                            isTransactionDialogOpen = true
                        }
                    )
                    AppScreen.INCOME -> IncomeScreen(
                        viewModel = financeViewModel,
                        onAddIncome = {
                            editingTransaction = null
                            transactionDialogType = "INCOME"
                            isTransactionDialogOpen = true
                        },
                        onEditTransaction = { tx ->
                            editingTransaction = tx
                            transactionDialogType = tx.type
                            isTransactionDialogOpen = true
                        }
                    )
                    AppScreen.EXPENSES -> ExpenseScreen(
                        viewModel = financeViewModel,
                        onAddExpense = {
                            editingTransaction = null
                            transactionDialogType = "EXPENSE"
                            isTransactionDialogOpen = true
                        },
                        onEditTransaction = { tx ->
                            editingTransaction = tx
                            transactionDialogType = tx.type
                            isTransactionDialogOpen = true
                        }
                    )
                    AppScreen.TRANSACTIONS -> TransactionsScreen(
                        viewModel = financeViewModel,
                        onEditTransaction = { tx ->
                            editingTransaction = tx
                            transactionDialogType = tx.type
                            isTransactionDialogOpen = true
                        }
                    )
                    AppScreen.BUDGET -> BudgetScreen(viewModel = financeViewModel)
                    AppScreen.CALENDAR -> CalendarScreen(
                        viewModel = financeViewModel,
                        onEditTransaction = { tx ->
                            editingTransaction = tx
                            transactionDialogType = tx.type
                            isTransactionDialogOpen = true
                        }
                    )
                    AppScreen.REPORTS -> ReportsScreen(viewModel = financeViewModel)
                    AppScreen.CATEGORIES -> CategoriesScreen(viewModel = financeViewModel)
                    AppScreen.ACCOUNTS -> AccountsScreen(viewModel = financeViewModel)
                    AppScreen.NOTIFICATIONS -> NotificationsScreen(viewModel = financeViewModel)
                    AppScreen.SETTINGS -> SettingsScreen(
                        user = user,
                        viewModel = financeViewModel,
                        authViewModel = authViewModel,
                        onLogout = { authViewModel.logout() }
                    )
                    AppScreen.PROFILE -> SettingsScreen(
                        user = user,
                        viewModel = financeViewModel,
                        authViewModel = authViewModel,
                        onLogout = { authViewModel.logout() }
                    )
                }
            }
        }
    }

    // Quick Action Bottom Sheet for (+) Button
    if (showQuickActionSheet) {
        ModalBottomSheet(
            onDismissRequest = { showQuickActionSheet = false },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (language == "LAO") "ເລືອກປະເພດລາຍການ (Choose Type)" else "Choose Transaction Type",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Add Income Option
                    Surface(
                        onClick = {
                            showQuickActionSheet = false
                            editingTransaction = null
                            transactionDialogType = "INCOME"
                            isTransactionDialogOpen = true
                        },
                        shape = RoundedCornerShape(18.dp),
                        color = IncomeGreen.copy(alpha = 0.12f),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("action_sheet_add_income")
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .background(IncomeGreen, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "+ ລາຍຮັບ",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = IncomeGreen
                            )
                            Text(
                                text = "Add Income",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Add Expense Option
                    Surface(
                        onClick = {
                            showQuickActionSheet = false
                            editingTransaction = null
                            transactionDialogType = "EXPENSE"
                            isTransactionDialogOpen = true
                        },
                        shape = RoundedCornerShape(18.dp),
                        color = ExpenseRed.copy(alpha = 0.12f),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("action_sheet_add_expense")
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .background(ExpenseRed, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "- ລາຍຈ່າຍ",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = ExpenseRed
                            )
                            Text(
                                text = "Add Expense",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Add / Edit Transaction Dialog
    AddTransactionDialog(
        isOpen = isTransactionDialogOpen,
        initialType = transactionDialogType,
        existingTransaction = editingTransaction,
        categories = categories,
        accounts = accounts,
        currency = currency,
        language = language,
        onDismiss = {
            isTransactionDialogOpen = false
            editingTransaction = null
        },
        onSave = { tx ->
            if (editingTransaction != null) {
                financeViewModel.updateTransaction(editingTransaction!!, tx)
            } else {
                financeViewModel.addTransaction(tx)
            }
            isTransactionDialogOpen = false
            editingTransaction = null
        }
    )

    // Global Search Dialog
    GlobalSearchDialog(
        isOpen = isSearchDialogOpen,
        viewModel = financeViewModel,
        onEditTransaction = { tx ->
            editingTransaction = tx
            transactionDialogType = tx.type
            isTransactionDialogOpen = true
        },
        onDismiss = { isSearchDialogOpen = false }
    )

    // SweetAlert2 Feedback Dialog
    SweetAlertDialog(
        state = sweetAlertState,
        onDismissRequest = { financeViewModel.dismissSweetAlert() }
    )
}
