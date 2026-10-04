package com.example.ui.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AccountEntity
import com.example.data.model.BudgetEntity
import com.example.data.model.BudgetWithCategory
import com.example.data.model.CategoryEntity
import com.example.data.model.NotificationEntity
import com.example.data.model.SettingEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionWithDetails
import com.example.data.repository.BudgetAlert
import com.example.data.repository.FinanceRepository
import com.example.ui.components.BarChartItem
import com.example.ui.components.CategorySpendItem
import com.example.ui.components.SweetAlertState
import com.example.ui.components.SweetAlertType
import com.example.util.CurrencyFormatter
import com.example.util.DateUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CalendarDayData(
    val dayNumber: Int,
    val dateString: String,
    val income: Double,
    val expense: Double,
    val net: Double
)

class FinanceViewModel(
    private val repository: FinanceRepository,
    private val userId: Long
) : ViewModel() {

    val accounts: StateFlow<List<AccountEntity>> = repository.getAccounts(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<CategoryEntity>> = repository.getCategories(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transactions: StateFlow<List<TransactionWithDetails>> = repository.getTransactions(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentTransactions: StateFlow<List<TransactionWithDetails>> = repository.getRecentTransactions(userId, 6)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val budgets: StateFlow<List<BudgetWithCategory>> = repository.getBudgets(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notifications: StateFlow<List<NotificationEntity>> = repository.getNotifications(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unreadNotificationsCount: StateFlow<Int> = notifications
        .map { list -> list.count { !it.isRead } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // User Settings
    private val _currency = MutableStateFlow("LAK")
    val currency: StateFlow<String> = _currency.asStateFlow()

    private val _language = MutableStateFlow("LAO")
    val language: StateFlow<String> = _language.asStateFlow()

    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    // SweetAlert State
    private val _sweetAlertState = MutableStateFlow(SweetAlertState())
    val sweetAlertState: StateFlow<SweetAlertState> = _sweetAlertState.asStateFlow()

    // Global Search State
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        // Load initial setting
        viewModelScope.launch {
            repository.getSettingFlow(userId).collect { setting ->
                if (setting != null) {
                    _currency.value = setting.currency
                    _language.value = setting.language
                    _isDarkMode.value = setting.isDarkMode
                }
            }
        }
    }

    // Calculations & Metrics
    val totalBalance: StateFlow<Double> = accounts
        .map { list -> list.sumOf { it.balance } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val todayIncome: StateFlow<Double> = transactions
        .map { list ->
            val today = DateUtil.todayString()
            list.filter { it.transaction.type == "INCOME" && it.transaction.date == today }
                .sumOf { it.transaction.amount }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val todayExpense: StateFlow<Double> = transactions
        .map { list ->
            val today = DateUtil.todayString()
            list.filter { it.transaction.type == "EXPENSE" && it.transaction.date == today }
                .sumOf { it.transaction.amount }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val monthlyIncome: StateFlow<Double> = transactions
        .map { list ->
            val ym = DateUtil.currentYearMonth()
            list.filter { it.transaction.type == "INCOME" && it.transaction.date.startsWith(ym) }
                .sumOf { it.transaction.amount }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val monthlyExpense: StateFlow<Double> = transactions
        .map { list ->
            val ym = DateUtil.currentYearMonth()
            list.filter { it.transaction.type == "EXPENSE" && it.transaction.date.startsWith(ym) }
                .sumOf { it.transaction.amount }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val monthlyBudgetAmount: StateFlow<Double> = budgets
        .map { list ->
            val overall = list.find { it.budget.categoryId == null }
            overall?.budget?.amount ?: list.sumOf { it.budget.amount }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Chart Data: Bar Chart (Last 5 days)
    val barChartData: StateFlow<List<BarChartItem>> = transactions
        .map { txList ->
            val today = DateUtil.todayString()
            val cal = java.util.Calendar.getInstance()
            val formatSdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
            val labelSdf = java.text.SimpleDateFormat("dd/MM", java.util.Locale.US)

            val days = (4 downTo 0).map { offset ->
                val c = java.util.Calendar.getInstance()
                c.add(java.util.Calendar.DAY_OF_YEAR, -offset)
                val dStr = formatSdf.format(c.time)
                val lStr = labelSdf.format(c.time)
                val inVal = txList.filter { it.transaction.type == "INCOME" && it.transaction.date == dStr }.sumOf { it.transaction.amount }
                val exVal = txList.filter { it.transaction.type == "EXPENSE" && it.transaction.date == dStr }.sumOf { it.transaction.amount }
                BarChartItem(lStr, inVal, exVal)
            }
            days
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Chart Data: Category Donut Chart
    val categorySpendItems: StateFlow<List<CategorySpendItem>> = combine(transactions, categories) { txList, catList ->
        val ym = DateUtil.currentYearMonth()
        val expenseTxs = txList.filter { it.transaction.type == "EXPENSE" && it.transaction.date.startsWith(ym) }
        val totalExp = expenseTxs.sumOf { it.transaction.amount }

        if (totalExp <= 0) emptyList()
        else {
            val grouped = expenseTxs.groupBy { it.transaction.categoryId }
            grouped.map { (catId, txs) ->
                val sum = txs.sumOf { it.transaction.amount }
                val cat = catList.find { it.id == catId }
                val name = cat?.nameLao ?: "ອື່ນໆ"
                val color = try {
                    Color(android.graphics.Color.parseColor(cat?.colorHex ?: "#6B7280"))
                } catch (_: Exception) {
                    Color.Gray
                }
                val pct = ((sum / totalExp) * 100f).toFloat()
                CategorySpendItem(name, sum, color, pct)
            }.sortedByDescending { it.amount }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Calendar Data: Current Month Days
    val calendarDays: StateFlow<List<CalendarDayData>> = transactions
        .map { txList ->
            val cal = java.util.Calendar.getInstance()
            val daysInMonth = cal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
            val currentYm = DateUtil.currentYearMonth()

            (1..daysInMonth).map { day ->
                val dayStr = if (day < 10) "0$day" else "$day"
                val fullDate = "$currentYm-$dayStr"
                val inVal = txList.filter { it.transaction.type == "INCOME" && it.transaction.date == fullDate }.sumOf { it.transaction.amount }
                val exVal = txList.filter { it.transaction.type == "EXPENSE" && it.transaction.date == fullDate }.sumOf { it.transaction.amount }
                CalendarDayData(day, fullDate, inVal, exVal, inVal - exVal)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Search Results
    val searchResults: StateFlow<List<TransactionWithDetails>> = combine(transactions, _searchQuery) { list, query ->
        if (query.isBlank()) emptyList()
        else {
            val q = query.trim().lowercase()
            list.filter { item ->
                item.transaction.note.lowercase().contains(q) ||
                (item.category?.nameLao?.lowercase()?.contains(q) == true) ||
                (item.category?.nameEng?.lowercase()?.contains(q) == true) ||
                (item.account?.name?.lowercase()?.contains(q) == true) ||
                item.transaction.amount.toString().contains(q) ||
                item.transaction.date.contains(q)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Actions
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun addTransaction(tx: TransactionEntity) {
        viewModelScope.launch {
            val alert = repository.addTransaction(tx.copy(userId = userId))
            if (alert.hasAlert) {
                if (alert.isCritical) {
                    showSweetAlert(
                        type = SweetAlertType.CRITICAL,
                        title = if (_language.value == "LAO") "ງົບປະມານຖືກໃຊ້ເກີນແລ້ວ!" else "Budget Exceeded!",
                        message = if (_language.value == "LAO") "ໝວດໝູ່ ${alert.categoryName} ໃຊ້ເກີນງົບແລ້ວ (${alert.percentUsed}%)"
                                  else "${alert.categoryName} exceeded budget (${alert.percentUsed}%)"
                    )
                } else {
                    showSweetAlert(
                        type = SweetAlertType.WARNING,
                        title = if (_language.value == "LAO") "ງົບປະມານໃກ້ເຕັມແລ້ວ" else "Budget Alert (80%)",
                        message = if (_language.value == "LAO") "ໝວດໝູ່ ${alert.categoryName} ຖືກໃຊ້ໄປແລ້ວ ${alert.percentUsed}%"
                                  else "${alert.categoryName} reached ${alert.percentUsed}% of budget"
                    )
                }
            } else {
                showSweetAlert(
                    type = SweetAlertType.SUCCESS,
                    title = if (_language.value == "LAO") "ບັນທຶກສຳເລັດ" else "Saved Successfully",
                    message = if (_language.value == "LAO") "ບັນທຶກທຸລະກຳ ແລະ ອັບເດດຍອດເງິນຮຽບຮ້ອຍແລ້ວ" else "Transaction saved and balance updated"
                )
            }
        }
    }

    fun updateTransaction(oldTx: TransactionEntity, newTx: TransactionEntity) {
        viewModelScope.launch {
            val alert = repository.updateTransaction(oldTx, newTx.copy(userId = userId))
            showSweetAlert(
                type = SweetAlertType.SUCCESS,
                title = if (_language.value == "LAO") "ແກ້ໄຂສຳເລັດ" else "Updated Successfully",
                message = if (_language.value == "LAO") "ອັບເດດຂໍ້ມູນທຸລະກຳຮຽບຮ້ອຍ" else "Transaction updated successfully"
            )
        }
    }

    fun deleteTransaction(tx: TransactionEntity) {
        showSweetAlert(
            type = SweetAlertType.CONFIRM,
            title = if (_language.value == "LAO") "ລຶບລາຍການນີ້ບໍ?" else "Delete Transaction?",
            message = if (_language.value == "LAO") "ທ່ານແນ່ໃຈບໍ່ວ່າຕ້ອງການລຶບທຸລະກຳນີ້? ຍອດເງິນໃນບັນຊີຈະຖືກປັບອັດຕະໂນມັດ." else "Are you sure you want to delete this transaction?",
            showCancel = true,
            confirmText = if (_language.value == "LAO") "ລຶບ (Delete)" else "Delete",
            cancelText = if (_language.value == "LAO") "ຍົກເລີກ" else "Cancel",
            onConfirm = {
                viewModelScope.launch {
                    repository.deleteTransaction(tx)
                    showSweetAlert(
                        type = SweetAlertType.SUCCESS,
                        title = if (_language.value == "LAO") "ລຶບສຳເລັດ" else "Deleted",
                        message = if (_language.value == "LAO") "ລຶບລາຍການ ແລະ ປັບຍອດເງິນສຳເລັດແລ້ວ" else "Transaction deleted successfully"
                    )
                }
            }
        )
    }

    fun addBudget(categoryId: Long?, amount: Double, period: String = "MONTHLY") {
        viewModelScope.launch {
            val ym = DateUtil.currentYearMonth()
            repository.addBudget(
                BudgetEntity(
                    userId = userId,
                    categoryId = categoryId,
                    amount = amount,
                    periodType = period,
                    yearMonth = ym
                )
            )
            showSweetAlert(
                type = SweetAlertType.SUCCESS,
                title = if (_language.value == "LAO") "ບັນທຶກງົບປະມານສຳເລັດ" else "Budget Created",
                message = if (_language.value == "LAO") "ກຳນົດງົບປະມານໃໝ່ຮຽບຮ້ອຍ" else "Budget saved successfully"
            )
        }
    }

    fun deleteBudget(budget: BudgetEntity) {
        showSweetAlert(
            type = SweetAlertType.CONFIRM,
            title = if (_language.value == "LAO") "ລຶບງົບປະມານນີ້ບໍ?" else "Delete Budget?",
            message = if (_language.value == "LAO") "ທ່ານຕ້ອງການລຶບງົບປະມານນີ້ແທ້ບໍ?" else "Delete this budget?",
            showCancel = true,
            onConfirm = {
                viewModelScope.launch {
                    repository.deleteBudget(budget)
                }
            }
        )
    }

    fun addAccount(name: String, type: String, balance: Double, color: String = "#007A5E") {
        viewModelScope.launch {
            repository.addAccount(
                AccountEntity(
                    userId = userId,
                    name = name,
                    type = type,
                    balance = balance,
                    initialBalance = balance,
                    colorHex = color
                )
            )
            showSweetAlert(
                type = SweetAlertType.SUCCESS,
                title = if (_language.value == "LAO") "ເພີ່ມບັນຊີສຳເລັດ" else "Account Added",
                message = if (_language.value == "LAO") "ເພີ່ມບັນຊີ / ກະເປົາເງິນໃໝ່ແລ້ວ" else "New wallet account created"
            )
        }
    }

    fun deleteAccount(account: AccountEntity) {
        showSweetAlert(
            type = SweetAlertType.CONFIRM,
            title = if (_language.value == "LAO") "ລຶບບັນຊີນີ້ບໍ?" else "Delete Account?",
            message = if (_language.value == "LAO") "ທຸລະກຳທັງໝົດໃນບັນຊີ ${account.name} ຈະຖືກລຶບອອກເຊັ່ນກັນ." else "All transactions under this account will be removed.",
            showCancel = true,
            onConfirm = {
                viewModelScope.launch {
                    repository.deleteAccount(account)
                }
            }
        )
    }

    fun addCategory(nameLao: String, nameEng: String, type: String, colorHex: String = "#007A5E", icon: String = "category") {
        viewModelScope.launch {
            repository.addCategory(
                CategoryEntity(
                    userId = userId,
                    nameLao = nameLao,
                    nameEng = nameEng,
                    type = type,
                    colorHex = colorHex,
                    iconName = icon
                )
            )
            showSweetAlert(
                type = SweetAlertType.SUCCESS,
                title = if (_language.value == "LAO") "ເພີ່ມໝວດໝູ່ສຳເລັດ" else "Category Added",
                message = if (_language.value == "LAO") "ໝວດໝູ່ໃໝ່ພ້ອມໃຊ້ງານແລ້ວ" else "New category added"
            )
        }
    }

    fun deleteCategory(cat: CategoryEntity) {
        showSweetAlert(
            type = SweetAlertType.CONFIRM,
            title = if (_language.value == "LAO") "ລຶບໝວດໝູ່ນີ້ບໍ?" else "Delete Category?",
            message = if (_language.value == "LAO") "ຕ້ອງການລຶບ '${cat.nameLao}' ບໍ?" else "Delete category?",
            showCancel = true,
            onConfirm = {
                viewModelScope.launch {
                    try {
                        repository.deleteCategory(cat)
                    } catch (_: Exception) {
                        showSweetAlert(
                            type = SweetAlertType.ERROR,
                            title = if (_language.value == "LAO") "ບໍ່ສາມາດລຶບໄດ້" else "Cannot Delete",
                            message = if (_language.value == "LAO") "ມີທຸລະກຳທີ່ເຊື່ອມໂຍງກັບໝວດໝູ່ນີ້ຢູ່" else "Transactions are using this category."
                        )
                    }
                }
            }
        )
    }

    fun setCurrency(newCurrency: String) {
        _currency.value = newCurrency
        saveSettingsToDb()
    }

    fun setLanguage(newLanguage: String) {
        _language.value = newLanguage
        saveSettingsToDb()
    }

    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
        saveSettingsToDb()
    }

    fun markNotificationAsRead(id: Long) {
        viewModelScope.launch {
            repository.markNotificationAsRead(id)
        }
    }

    fun clearAllNotifications() {
        viewModelScope.launch {
            repository.clearAllNotifications(userId)
        }
    }

    private fun saveSettingsToDb() {
        viewModelScope.launch {
            repository.saveSetting(
                SettingEntity(
                    userId = userId,
                    currency = _currency.value,
                    language = _language.value,
                    isDarkMode = _isDarkMode.value
                )
            )
        }
    }

    fun showSweetAlert(
        type: SweetAlertType,
        title: String,
        message: String,
        confirmText: String = "ຕົກລົງ (OK)",
        cancelText: String = "ຍົກເລີກ (Cancel)",
        showCancel: Boolean = false,
        onConfirm: () -> Unit = {},
        onCancel: () -> Unit = {}
    ) {
        _sweetAlertState.value = SweetAlertState(
            isVisible = true,
            type = type,
            title = title,
            message = message,
            confirmText = confirmText,
            cancelText = cancelText,
            showCancel = showCancel,
            onConfirm = onConfirm,
            onCancel = onCancel
        )
    }

    fun dismissSweetAlert() {
        _sweetAlertState.value = _sweetAlertState.value.copy(isVisible = false)
    }

    fun exportCsvData(): String {
        return repository.generateCsvExport(transactions.value)
    }
}
