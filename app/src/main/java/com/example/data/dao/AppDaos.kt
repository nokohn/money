package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.model.AccountEntity
import com.example.data.model.BudgetEntity
import com.example.data.model.BudgetWithCategory
import com.example.data.model.CategoryEntity
import com.example.data.model.NotificationEntity
import com.example.data.model.SettingEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionWithDetails
import com.example.data.model.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: UserEntity): Long

    @Query("SELECT * FROM users WHERE username = :login OR email = :login LIMIT 1")
    suspend fun getUserByUsernameOrEmail(login: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    fun getUserByIdFlow(id: Long): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: Long): UserEntity?

    @Query("UPDATE users SET passwordHash = :hash, salt = :salt WHERE id = :id")
    suspend fun updatePassword(id: Long, hash: String, salt: String)

    @Update
    suspend fun updateUser(user: UserEntity)
}

@Dao
interface AccountDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: AccountEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccounts(accounts: List<AccountEntity>)

    @Update
    suspend fun updateAccount(account: AccountEntity)

    @Delete
    suspend fun deleteAccount(account: AccountEntity)

    @Query("SELECT * FROM accounts WHERE userId = :userId ORDER BY isDefault DESC, name ASC")
    fun getAccountsForUser(userId: Long): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE id = :id LIMIT 1")
    suspend fun getAccountById(id: Long): AccountEntity?

    @Query("UPDATE accounts SET balance = balance + :amount WHERE id = :id")
    suspend fun updateBalance(id: Long, amount: Double)
}

@Dao
interface CategoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>)

    @Update
    suspend fun updateCategory(category: CategoryEntity)

    @Delete
    suspend fun deleteCategory(category: CategoryEntity)

    @Query("SELECT * FROM categories WHERE userId = :userId ORDER BY nameLao ASC")
    fun getCategoriesForUser(userId: Long): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE userId = :userId AND type = :type ORDER BY nameLao ASC")
    fun getCategoriesByType(userId: Long, type: String): Flow<List<CategoryEntity>>
}

@Dao
interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<TransactionEntity>)

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Transaction
    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY date DESC, time DESC, id DESC")
    fun getAllWithDetails(userId: Long): Flow<List<TransactionWithDetails>>

    @Transaction
    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY date DESC, time DESC, id DESC LIMIT :limit")
    fun getRecentWithDetails(userId: Long, limit: Int = 5): Flow<List<TransactionWithDetails>>

    @Transaction
    @Query("SELECT * FROM transactions WHERE userId = :userId AND date = :date ORDER BY time DESC")
    fun getByDate(userId: Long, date: String): Flow<List<TransactionWithDetails>>

    @Transaction
    @Query("SELECT * FROM transactions WHERE userId = :userId AND date LIKE :yearMonth || '%' ORDER BY date DESC, time DESC")
    fun getByMonth(userId: Long, yearMonth: String): Flow<List<TransactionWithDetails>>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM transactions WHERE userId = :userId AND type = 'INCOME' AND date = :date")
    fun getTodayIncome(userId: Long, date: String): Flow<Double>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM transactions WHERE userId = :userId AND type = 'EXPENSE' AND date = :date")
    fun getTodayExpense(userId: Long, date: String): Flow<Double>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM transactions WHERE userId = :userId AND type = 'INCOME' AND date LIKE :yearMonth || '%'")
    fun getMonthlyIncome(userId: Long, yearMonth: String): Flow<Double>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM transactions WHERE userId = :userId AND type = 'EXPENSE' AND date LIKE :yearMonth || '%'")
    fun getMonthlyExpense(userId: Long, yearMonth: String): Flow<Double>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM transactions WHERE userId = :userId AND type = 'INCOME'")
    fun getTotalIncome(userId: Long): Flow<Double>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM transactions WHERE userId = :userId AND type = 'EXPENSE'")
    fun getTotalExpense(userId: Long): Flow<Double>
}

@Dao
interface BudgetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: BudgetEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudgets(budgets: List<BudgetEntity>)

    @Update
    suspend fun updateBudget(budget: BudgetEntity)

    @Delete
    suspend fun deleteBudget(budget: BudgetEntity)

    @Transaction
    @Query("SELECT * FROM budgets WHERE userId = :userId ORDER BY id DESC")
    fun getAllBudgets(userId: Long): Flow<List<BudgetWithCategory>>

    @Transaction
    @Query("SELECT * FROM budgets WHERE userId = :userId AND yearMonth = :yearMonth")
    fun getBudgetsForMonth(userId: Long, yearMonth: String): Flow<List<BudgetWithCategory>>
}

@Dao
interface NotificationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity): Long

    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY timestamp DESC")
    fun getNotificationsForUser(userId: Long): Flow<List<NotificationEntity>>

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: Long)

    @Query("UPDATE notifications SET isRead = 1 WHERE userId = :userId")
    suspend fun markAllAsRead(userId: Long)

    @Query("DELETE FROM notifications WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM notifications WHERE userId = :userId")
    suspend fun deleteAllForUser(userId: Long)
}

@Dao
interface SettingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSetting(setting: SettingEntity)

    @Query("SELECT * FROM settings WHERE userId = :userId LIMIT 1")
    fun getSettingFlow(userId: Long): Flow<SettingEntity?>

    @Query("SELECT * FROM settings WHERE userId = :userId LIMIT 1")
    suspend fun getSetting(userId: Long): SettingEntity?
}
