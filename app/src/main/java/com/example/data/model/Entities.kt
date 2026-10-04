package com.example.data.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(
    tableName = "users",
    indices = [Index(value = ["username"], unique = true), Index(value = ["email"], unique = true)]
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val username: String,
    val email: String,
    val passwordHash: String,
    val salt: String,
    val fullName: String,
    val currency: String = "LAK",
    val language: String = "LAO",
    val budgetWarningThreshold: Int = 80,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "accounts",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("userId")]
)
data class AccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val name: String,
    val type: String = "CASH",
    val balance: Double = 0.0,
    val initialBalance: Double = 0.0,
    val colorHex: String = "#007A5E",
    val iconName: String = "account_balance_wallet",
    val isDefault: Boolean = false
)

@Entity(
    tableName = "categories",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("userId")]
)
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val nameLao: String,
    val nameEng: String,
    val type: String, // INCOME or EXPENSE
    val iconName: String,
    val colorHex: String
)

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("userId"), Index("accountId"), Index("categoryId"), Index("date")]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val accountId: Long,
    val categoryId: Long,
    val type: String, // INCOME or EXPENSE
    val amount: Double,
    val date: String, // YYYY-MM-DD
    val time: String, // HH:mm
    val note: String = "",
    val receiptUri: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

data class TransactionWithDetails(
    @Embedded val transaction: TransactionEntity,
    @Relation(
        parentColumn = "accountId",
        entityColumn = "id"
    )
    val account: AccountEntity?,
    @Relation(
        parentColumn = "categoryId",
        entityColumn = "id"
    )
    val category: CategoryEntity?
)

@Entity(
    tableName = "budgets",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("userId"), Index("categoryId")]
)
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val categoryId: Long? = null, // null = overall monthly budget
    val periodType: String = "MONTHLY",
    val amount: Double,
    val yearMonth: String // YYYY-MM
)

data class BudgetWithCategory(
    @Embedded val budget: BudgetEntity,
    @Relation(
        parentColumn = "categoryId",
        entityColumn = "id"
    )
    val category: CategoryEntity?
)

@Entity(
    tableName = "notifications",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("userId"), Index("timestamp")]
)
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val title: String,
    val message: String,
    val type: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

@Entity(
    tableName = "settings",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class SettingEntity(
    @PrimaryKey val userId: Long,
    val isDarkMode: Boolean = false,
    val currency: String = "LAK",
    val language: String = "LAO",
    val notificationsEnabled: Boolean = true
)
