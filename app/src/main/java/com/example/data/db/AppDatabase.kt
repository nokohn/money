package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.AccountDao
import com.example.data.dao.BudgetDao
import com.example.data.dao.CategoryDao
import com.example.data.dao.NotificationDao
import com.example.data.dao.SettingDao
import com.example.data.dao.TransactionDao
import com.example.data.dao.UserDao
import com.example.data.model.AccountEntity
import com.example.data.model.BudgetEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.NotificationEntity
import com.example.data.model.SettingEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.UserEntity
import com.example.util.DateUtil
import com.example.util.SecurityUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        AccountEntity::class,
        CategoryEntity::class,
        TransactionEntity::class,
        BudgetEntity::class,
        NotificationEntity::class,
        SettingEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun accountDao(): AccountDao
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
    abstract fun budgetDao(): BudgetDao
    abstract fun notificationDao(): NotificationDao
    abstract fun settingDao(): SettingDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "lao_finance_database"
                )
                .addCallback(DatabaseCallback())
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialData(database)
                    }
                }
            }
        }

        suspend fun populateInitialData(database: AppDatabase) {
            val userDao = database.userDao()
            val accountDao = database.accountDao()
            val categoryDao = database.categoryDao()
            val transactionDao = database.transactionDao()
            val budgetDao = database.budgetDao()
            val notificationDao = database.notificationDao()
            val settingDao = database.settingDao()

            // 1. Create Default Demo User
            val salt = SecurityUtil.generateSalt()
            val passwordHash = SecurityUtil.hashPassword("password123", salt)
            val userId = userDao.insertUser(
                UserEntity(
                    username = "somsak",
                    email = "somsak@gmail.com",
                    passwordHash = passwordHash,
                    salt = salt,
                    fullName = "ສົມສັກ ໄຊຍະວົງ",
                    currency = "LAK",
                    language = "LAO",
                    budgetWarningThreshold = 80
                )
            )

            // Settings
            settingDao.saveSetting(
                SettingEntity(
                    userId = userId,
                    isDarkMode = false,
                    currency = "LAK",
                    language = "LAO",
                    notificationsEnabled = true
                )
            )

            // 2. Create Accounts / Wallets
            val bcelId = accountDao.insertAccount(
                AccountEntity(
                    userId = userId,
                    name = "BCEL One",
                    type = "BCEL_ONE",
                    balance = 6500000.0,
                    initialBalance = 1000000.0,
                    colorHex = "#005696",
                    iconName = "account_balance",
                    isDefault = true
                )
            )

            val cashId = accountDao.insertAccount(
                AccountEntity(
                    userId = userId,
                    name = "ເງິນສົດ (Cash)",
                    type = "CASH",
                    balance = 1500000.0,
                    initialBalance = 500000.0,
                    colorHex = "#0D7C59",
                    iconName = "payments",
                    isDefault = false
                )
            )

            val jdbId = accountDao.insertAccount(
                AccountEntity(
                    userId = userId,
                    name = "JDB Yes",
                    type = "JDB_YES",
                    balance = 2000000.0,
                    initialBalance = 2000000.0,
                    colorHex = "#D49B00",
                    iconName = "credit_card",
                    isDefault = false
                )
            )

            val ldbId = accountDao.insertAccount(
                AccountEntity(
                    userId = userId,
                    name = "LDB Trust",
                    type = "LDB",
                    balance = 1000000.0,
                    initialBalance = 1000000.0,
                    colorHex = "#C8102E",
                    iconName = "account_balance",
                    isDefault = false
                )
            )

            // 3. Create Categories
            // Income categories
            val catSalary = categoryDao.insertCategory(
                CategoryEntity(userId = userId, nameLao = "ເງິນເດືອນ", nameEng = "Salary", type = "INCOME", iconName = "payments", colorHex = "#10B981")
            )
            val catBusiness = categoryDao.insertCategory(
                CategoryEntity(userId = userId, nameLao = "ທຸລະກິດ", nameEng = "Business", type = "INCOME", iconName = "storefront", colorHex = "#3B82F6")
            )
            val catFreelance = categoryDao.insertCategory(
                CategoryEntity(userId = userId, nameLao = "ວຽກອິດສະຫຼະ", nameEng = "Freelance", type = "INCOME", iconName = "laptop", colorHex = "#8B5CF6")
            )
            val catBonus = categoryDao.insertCategory(
                CategoryEntity(userId = userId, nameLao = "ເງິນໂບນັດ", nameEng = "Bonus", type = "INCOME", iconName = "card_giftcard", colorHex = "#F59E0B")
            )
            val catCommission = categoryDao.insertCategory(
                CategoryEntity(userId = userId, nameLao = "ຄ່ານາຍໜ້າ", nameEng = "Commission", type = "INCOME", iconName = "handshake", colorHex = "#EC4899")
            )
            val catOtherIncome = categoryDao.insertCategory(
                CategoryEntity(userId = userId, nameLao = "ລາຍຮັບອື່ນໆ", nameEng = "Other Income", type = "INCOME", iconName = "add_circle", colorHex = "#6B7280")
            )

            // Expense categories
            val catFood = categoryDao.insertCategory(
                CategoryEntity(userId = userId, nameLao = "ອາຫານ ແລະ ເຄື່ອງດື່ມ", nameEng = "Food & Beverage", type = "EXPENSE", iconName = "restaurant", colorHex = "#EF4444")
            )
            val catTransport = categoryDao.insertCategory(
                CategoryEntity(userId = userId, nameLao = "ຄ່າເດີນທາງ", nameEng = "Transportation", type = "EXPENSE", iconName = "directions_car", colorHex = "#F97316")
            )
            val catHousing = categoryDao.insertCategory(
                CategoryEntity(userId = userId, nameLao = "ທີ່ຢູ່ອາໄສ", nameEng = "Housing", type = "EXPENSE", iconName = "home", colorHex = "#6366F1")
            )
            val catElectricity = categoryDao.insertCategory(
                CategoryEntity(userId = userId, nameLao = "ຄ່າໄຟຟ້າ", nameEng = "Electricity", type = "EXPENSE", iconName = "bolt", colorHex = "#EAB308")
            )
            val catWater = categoryDao.insertCategory(
                CategoryEntity(userId = userId, nameLao = "ຄ່ານ້ຳປະປາ", nameEng = "Water", type = "EXPENSE", iconName = "water_drop", colorHex = "#06B6D4")
            )
            val catInternet = categoryDao.insertCategory(
                CategoryEntity(userId = userId, nameLao = "ຄ່າອິນເຕີເນັດ", nameEng = "Internet", type = "EXPENSE", iconName = "wifi", colorHex = "#8B5CF6")
            )
            val catShopping = categoryDao.insertCategory(
                CategoryEntity(userId = userId, nameLao = "ຊື້ເຄື່ອງ", nameEng = "Shopping", type = "EXPENSE", iconName = "shopping_bag", colorHex = "#EC4899")
            )
            val catEducation = categoryDao.insertCategory(
                CategoryEntity(userId = userId, nameLao = "ການສຶກສາ", nameEng = "Education", type = "EXPENSE", iconName = "school", colorHex = "#14B8A6")
            )
            val catHealth = categoryDao.insertCategory(
                CategoryEntity(userId = userId, nameLao = "ສຸຂະພາບ", nameEng = "Healthcare", type = "EXPENSE", iconName = "health_and_safety", colorHex = "#10B981")
            )
            val catEntertainment = categoryDao.insertCategory(
                CategoryEntity(userId = userId, nameLao = "ບັນເທີງ", nameEng = "Entertainment", type = "EXPENSE", iconName = "movie", colorHex = "#A855F7")
            )
            val catTravel = categoryDao.insertCategory(
                CategoryEntity(userId = userId, nameLao = "ທ່ອງທ່ຽວ", nameEng = "Travel", type = "EXPENSE", iconName = "flight", colorHex = "#3B82F6")
            )
            val catFamily = categoryDao.insertCategory(
                CategoryEntity(userId = userId, nameLao = "ຄອບຄົວ", nameEng = "Family", type = "EXPENSE", iconName = "diversity_1", colorHex = "#F43F5E")
            )
            val catOtherExpense = categoryDao.insertCategory(
                CategoryEntity(userId = userId, nameLao = "ລາຍຈ່າຍອື່ນໆ", nameEng = "Other Expense", type = "EXPENSE", iconName = "category", colorHex = "#6B7280")
            )

            // 4. Sample Transactions (Prompt explicitly mentions: Salary 8,000,000, Food 1,200,000, Transport 500,000, Shopping 800,000, Utilities 400,000)
            val today = DateUtil.todayString()
            val currentYM = DateUtil.currentYearMonth()

            transactionDao.insertTransactions(
                listOf(
                    TransactionEntity(
                        userId = userId,
                        accountId = bcelId,
                        categoryId = catSalary,
                        type = "INCOME",
                        amount = 8000000.0,
                        date = "$currentYM-01",
                        time = "08:30",
                        note = "ເງິນເດືອນປະຈຳເດືອນ ບໍລິສັດເທັກໂນໂລຢີ"
                    ),
                    TransactionEntity(
                        userId = userId,
                        accountId = cashId,
                        categoryId = catFreelance,
                        type = "INCOME",
                        amount = 2500000.0,
                        date = "$currentYM-03",
                        time = "14:15",
                        note = "ຮັບເງິນສົດ ຄ່າອອກແບບເວັບໄຊ"
                    ),
                    TransactionEntity(
                        userId = userId,
                        accountId = bcelId,
                        categoryId = catFood,
                        type = "EXPENSE",
                        amount = 1200000.0,
                        date = "$currentYM-02",
                        time = "12:00",
                        note = "ຄ່າອາຫານລວມກັບທີມງານ ແລະ ຕະຫຼາດເຊົ້າ"
                    ),
                    TransactionEntity(
                        userId = userId,
                        accountId = cashId,
                        categoryId = catTransport,
                        type = "EXPENSE",
                        amount = 500000.0,
                        date = "$currentYM-03",
                        time = "09:10",
                        note = "ຄ່ານ້ຳມັນລົດ ປ້ຳ ປຕທ"
                    ),
                    TransactionEntity(
                        userId = userId,
                        accountId = bcelId,
                        categoryId = catShopping,
                        type = "EXPENSE",
                        amount = 800000.0,
                        date = "$currentYM-04",
                        time = "16:45",
                        note = "ຊື້ເຄື່ອງໃຊ້ໃນເຮືອນ ທີ່ Parkson"
                    ),
                    TransactionEntity(
                        userId = userId,
                        accountId = bcelId,
                        categoryId = catElectricity,
                        type = "EXPENSE",
                        amount = 300000.0,
                        date = "$currentYM-04",
                        time = "10:20",
                        note = "ຊຳລະຄ່າໄຟຟ້າລາວ EDL ຜ່ານ BCEL"
                    ),
                    TransactionEntity(
                        userId = userId,
                        accountId = bcelId,
                        categoryId = catWater,
                        type = "EXPENSE",
                        amount = 100000.0,
                        date = "$currentYM-04",
                        time = "10:25",
                        note = "ຊຳລະຄ່ານ້ຳປະປາ ນະຄອນຫຼວງ"
                    ),
                    TransactionEntity(
                        userId = userId,
                        accountId = cashId,
                        categoryId = catFood,
                        type = "EXPENSE",
                        amount = 150000.0,
                        date = today,
                        time = "07:45",
                        note = "ກິນເຝີ ແລະ ກາເຟຕອນເຊົ້າ"
                    )
                )
            )

            // 5. Budgets
            budgetDao.insertBudgets(
                listOf(
                    BudgetEntity(
                        userId = userId,
                        categoryId = null, // Overall
                        periodType = "MONTHLY",
                        amount = 6000000.0,
                        yearMonth = currentYM
                    ),
                    BudgetEntity(
                        userId = userId,
                        categoryId = catFood,
                        periodType = "MONTHLY",
                        amount = 2000000.0,
                        yearMonth = currentYM
                    ),
                    BudgetEntity(
                        userId = userId,
                        categoryId = catShopping,
                        periodType = "MONTHLY",
                        amount = 1000000.0,
                        yearMonth = currentYM
                    ),
                    BudgetEntity(
                        userId = userId,
                        categoryId = catTransport,
                        periodType = "MONTHLY",
                        amount = 600000.0,
                        yearMonth = currentYM
                    )
                )
            )

            // 6. Notifications
            notificationDao.insertNotification(
                NotificationEntity(
                    userId = userId,
                    title = "ງົບປະມານໃກ້ເຕັມ (80%)",
                    message = "ໝວດໝູ່ 'ຊື້ເຄື່ອງ' ໄດ້ຖືກໃຊ້ໄປແລ້ວ 80% ຂອງງົບປະມານປະຈຳເດືອນ",
                    type = "WARNING_80",
                    isRead = false
                )
            )
            notificationDao.insertNotification(
                NotificationEntity(
                    userId = userId,
                    title = "ບັນທຶກລາຍຮັບສຳເລັດ",
                    message = "ໄດ້ຮັບເງິນເດືອນ 8,000,000 LAK ເຂົ້າບັນຊີ BCEL One",
                    type = "INFO",
                    isRead = true
                )
            )
        }
    }
}
