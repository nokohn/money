package com.example.data.model

enum class TransactionType(val labelLao: String, val labelEng: String) {
    INCOME("ລາຍຮັບ", "Income"),
    EXPENSE("ລາຍຈ່າຍ", "Expense")
}

enum class AccountType(val labelLao: String, val labelEng: String) {
    CASH("ເງິນສົດ", "Cash"),
    BCEL_ONE("BCEL One", "BCEL One"),
    JDB_YES("JDB Yes", "JDB Yes"),
    LDB("LDB Trust", "LDB Trust"),
    BANK("ບັນຊີທະນາຄານ", "Bank Account"),
    EWALLET("ກະເປົາເງິນອີເລັກໂທຣນິກ", "E-Wallet")
}

enum class BudgetPeriod(val labelLao: String, val labelEng: String) {
    DAILY("ລາຍວັນ", "Daily"),
    WEEKLY("ລາຍອາທິດ", "Weekly"),
    MONTHLY("ລາຍເດືອນ", "Monthly")
}

enum class CurrencyType(val symbol: String, val code: String, val labelLao: String) {
    LAK("₭", "LAK", "ກີບ (LAK)"),
    THB("฿", "THB", "ບາດ (THB)"),
    USD("$", "USD", "ໂດລາ (USD)")
}

enum class LanguageType(val code: String, val displayName: String) {
    LAO("lo", "ພາສາລາວ (Lao)"),
    ENG("en", "English")
}

enum class NotificationType(val titleLao: String, val titleEng: String) {
    WARNING_80("ງົບປະມານໃກ້ເຕັມ (80%)", "Budget Reached 80%"),
    CRITICAL_100("ງົບປະມານຖືກໃຊ້ເກີນແລ້ວ!", "Budget Exceeded!"),
    HIGH_SPENDING("ການໃຊ້ຈ່າຍສູງຜິດປົກກະຕິ", "Unusually High Spending"),
    INFO("ແຈ້ງເຕືອນທົ່ວໄປ", "Information")
}
