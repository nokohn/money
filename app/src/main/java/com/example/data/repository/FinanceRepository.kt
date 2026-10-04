package com.example.data.repository

import com.example.data.dao.AccountDao
import com.example.data.dao.BudgetDao
import com.example.data.dao.CategoryDao
import com.example.data.dao.NotificationDao
import com.example.data.dao.SettingDao
import com.example.data.dao.TransactionDao
import com.example.data.model.AccountEntity
import com.example.data.model.BudgetEntity
import com.example.data.model.BudgetWithCategory
import com.example.data.model.CategoryEntity
import com.example.data.model.NotificationEntity
import com.example.data.model.SettingEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionWithDetails
import com.example.util.CurrencyFormatter
import com.example.util.DateUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

data class BudgetAlert(
    val hasAlert: Boolean,
    val isCritical: Boolean, // true if >= 100%, false if >= 80%
    val categoryName: String,
    val percentUsed: Int,
    val remaining: Double
)

class FinanceRepository(
    private val accountDao: AccountDao,
    private val categoryDao: CategoryDao,
    private val transactionDao: TransactionDao,
    private val budgetDao: BudgetDao,
    private val notificationDao: NotificationDao,
    private val settingDao: SettingDao
) {
    // Accounts
    fun getAccounts(userId: Long): Flow<List<AccountEntity>> = accountDao.getAccountsForUser(userId)
    suspend fun addAccount(account: AccountEntity): Long = accountDao.insertAccount(account)
    suspend fun updateAccount(account: AccountEntity) = accountDao.updateAccount(account)
    suspend fun deleteAccount(account: AccountEntity) = accountDao.deleteAccount(account)

    // Categories
    fun getCategories(userId: Long): Flow<List<CategoryEntity>> = categoryDao.getCategoriesForUser(userId)
    fun getCategoriesByType(userId: Long, type: String): Flow<List<CategoryEntity>> =
        categoryDao.getCategoriesByType(userId, type)
    suspend fun addCategory(category: CategoryEntity): Long = categoryDao.insertCategory(category)
    suspend fun updateCategory(category: CategoryEntity) = categoryDao.updateCategory(category)
    suspend fun deleteCategory(category: CategoryEntity) = categoryDao.deleteCategory(category)

    // Transactions
    fun getTransactions(userId: Long): Flow<List<TransactionWithDetails>> =
        transactionDao.getAllWithDetails(userId)
    fun getRecentTransactions(userId: Long, limit: Int = 10): Flow<List<TransactionWithDetails>> =
        transactionDao.getRecentWithDetails(userId, limit)
    fun getTransactionsByMonth(userId: Long, yearMonth: String): Flow<List<TransactionWithDetails>> =
        transactionDao.getByMonth(userId, yearMonth)

    fun getTodayIncome(userId: Long, date: String): Flow<Double> = transactionDao.getTodayIncome(userId, date)
    fun getTodayExpense(userId: Long, date: String): Flow<Double> = transactionDao.getTodayExpense(userId, date)
    fun getMonthlyIncome(userId: Long, yearMonth: String): Flow<Double> =
        transactionDao.getMonthlyIncome(userId, yearMonth)
    fun getMonthlyExpense(userId: Long, yearMonth: String): Flow<Double> =
        transactionDao.getMonthlyExpense(userId, yearMonth)
    fun getTotalIncome(userId: Long): Flow<Double> = transactionDao.getTotalIncome(userId)
    fun getTotalExpense(userId: Long): Flow<Double> = transactionDao.getTotalExpense(userId)

    suspend fun addTransaction(transaction: TransactionEntity): BudgetAlert {
        val id = transactionDao.insertTransaction(transaction)

        // Update Account Balance
        val balanceDelta = if (transaction.type == "INCOME") transaction.amount else -transaction.amount
        accountDao.updateBalance(transaction.accountId, balanceDelta)

        // Check Budget if Expense
        var alert = BudgetAlert(false, false, "", 0, 0.0)
        if (transaction.type == "EXPENSE") {
            alert = checkBudgetStatus(transaction.userId, transaction.categoryId, transaction.date)
        }

        return alert
    }

    suspend fun updateTransaction(
        oldTransaction: TransactionEntity,
        newTransaction: TransactionEntity
    ): BudgetAlert {
        // Revert old balance
        val revertDelta = if (oldTransaction.type == "INCOME") -oldTransaction.amount else oldTransaction.amount
        accountDao.updateBalance(oldTransaction.accountId, revertDelta)

        // Apply new balance
        val applyDelta = if (newTransaction.type == "INCOME") newTransaction.amount else -newTransaction.amount
        accountDao.updateBalance(newTransaction.accountId, applyDelta)

        transactionDao.updateTransaction(newTransaction)

        var alert = BudgetAlert(false, false, "", 0, 0.0)
        if (newTransaction.type == "EXPENSE") {
            alert = checkBudgetStatus(newTransaction.userId, newTransaction.categoryId, newTransaction.date)
        }
        return alert
    }

    suspend fun deleteTransaction(transaction: TransactionEntity) {
        // Revert balance
        val revertDelta = if (transaction.type == "INCOME") -transaction.amount else transaction.amount
        accountDao.updateBalance(transaction.accountId, revertDelta)
        transactionDao.deleteTransaction(transaction)
    }

    private suspend fun checkBudgetStatus(
        userId: Long,
        categoryId: Long,
        date: String
    ): BudgetAlert {
        val ym = if (date.length >= 7) date.substring(0, 7) else DateUtil.currentYearMonth()
        val budgets = budgetDao.getBudgetsForMonth(userId, ym).firstOrNull() ?: emptyList()

        // Match category budget or overall budget
        val catBudget = budgets.find { it.budget.categoryId == categoryId }
        val overallBudget = budgets.find { it.budget.categoryId == null }

        val targetBudget = catBudget ?: overallBudget ?: return BudgetAlert(false, false, "", 0, 0.0)

        // Calculate total monthly expense for that category (or overall)
        val monthTxs = transactionDao.getByMonth(userId, ym).firstOrNull() ?: emptyList()
        val currentCategoryExpense = if (targetBudget.budget.categoryId != null) {
            monthTxs.filter { it.transaction.type == "EXPENSE" && it.transaction.categoryId == categoryId }
                .sumOf { it.transaction.amount }
        } else {
            monthTxs.filter { it.transaction.type == "EXPENSE" }
                .sumOf { it.transaction.amount }
        }

        val budgetAmount = targetBudget.budget.amount
        if (budgetAmount <= 0) return BudgetAlert(false, false, "", 0, 0.0)

        val pct = ((currentCategoryExpense / budgetAmount) * 100).toInt()
        val remaining = budgetAmount - currentCategoryExpense
        val catName = targetBudget.category?.nameLao ?: "ງົບປະມານລວມປະຈຳເດືອນ"

        if (pct >= 100) {
            notificationDao.insertNotification(
                NotificationEntity(
                    userId = userId,
                    title = "ງົບປະມານຖືກໃຊ້ເກີນແລ້ວ! ($pct%)",
                    message = "ໝວດໝູ່ '$catName' ໃຊ້ຈ່າຍເກີນງົບແລ້ວ (${CurrencyFormatter.format(currentCategoryExpense)} / ${CurrencyFormatter.format(budgetAmount)})",
                    type = "CRITICAL_100"
                )
            )
            return BudgetAlert(true, true, catName, pct, remaining)
        } else if (pct >= 80) {
            notificationDao.insertNotification(
                NotificationEntity(
                    userId = userId,
                    title = "ງົບປະມານໃກ້ເຕັມ ($pct%)",
                    message = "ໝວດໝູ່ '$catName' ຖືກໃຊ້ໄປແລ້ວ $pct% ເຫຼືອ ${CurrencyFormatter.format(remaining)}",
                    type = "WARNING_80"
                )
            )
            return BudgetAlert(true, false, catName, pct, remaining)
        }

        return BudgetAlert(false, false, "", pct, remaining)
    }

    // Budgets
    fun getBudgets(userId: Long): Flow<List<BudgetWithCategory>> = budgetDao.getAllBudgets(userId)
    fun getBudgetsForMonth(userId: Long, yearMonth: String): Flow<List<BudgetWithCategory>> =
        budgetDao.getBudgetsForMonth(userId, yearMonth)
    suspend fun addBudget(budget: BudgetEntity): Long = budgetDao.insertBudget(budget)
    suspend fun updateBudget(budget: BudgetEntity) = budgetDao.updateBudget(budget)
    suspend fun deleteBudget(budget: BudgetEntity) = budgetDao.deleteBudget(budget)

    // Notifications
    fun getNotifications(userId: Long): Flow<List<NotificationEntity>> =
        notificationDao.getNotificationsForUser(userId)
    suspend fun markNotificationAsRead(id: Long) = notificationDao.markAsRead(id)
    suspend fun markAllNotificationsAsRead(userId: Long) = notificationDao.markAllAsRead(userId)
    suspend fun deleteNotification(id: Long) = notificationDao.deleteById(id)
    suspend fun clearAllNotifications(userId: Long) = notificationDao.deleteAllForUser(userId)

    // Settings
    fun getSettingFlow(userId: Long): Flow<SettingEntity?> = settingDao.getSettingFlow(userId)
    suspend fun saveSetting(setting: SettingEntity) = settingDao.saveSetting(setting)

    // CSV Export
    fun generateCsvExport(transactions: List<TransactionWithDetails>): String {
        val sb = StringBuilder()
        sb.append("ID,Date,Time,Type,Category,Amount,Account,Note\n")
        transactions.forEach { item ->
            val t = item.transaction
            val catName = item.category?.nameLao ?: ""
            val accName = item.account?.name ?: ""
            val cleanNote = t.note.replace(",", " ").replace("\n", " ")
            sb.append("${t.id},${t.date},${t.time},${t.type},\"$catName\",${t.amount},\"$accName\",\"$cleanNote\"\n")
        }
        return sb.toString()
    }
}
