package com.example.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object CurrencyFormatter {
    private val formatSymbols = DecimalFormatSymbols(Locale.US).apply {
        groupingSeparator = ','
        decimalSeparator = '.'
    }

    private val formatterNoDecimals = DecimalFormat("#,##0", formatSymbols)
    private val formatterWithDecimals = DecimalFormat("#,##0.00", formatSymbols)

    fun format(amount: Double, currency: String = "LAK"): String {
        return when (currency.uppercase()) {
            "LAK" -> {
                // Lao Kip is typically whole numbers
                val formatted = formatterNoDecimals.format(amount)
                "$formatted ₭"
            }
            "THB" -> {
                val formatted = if (amount % 1.0 == 0.0) {
                    formatterNoDecimals.format(amount)
                } else {
                    formatterWithDecimals.format(amount)
                }
                "฿$formatted"
            }
            "USD" -> {
                val formatted = formatterWithDecimals.format(amount)
                "$$formatted"
            }
            else -> {
                val formatted = formatterNoDecimals.format(amount)
                "$formatted $currency"
            }
        }
    }

    fun formatNumberOnly(amount: Double, currency: String = "LAK"): String {
        return if (currency.uppercase() == "LAK") {
            formatterNoDecimals.format(amount)
        } else {
            if (amount % 1.0 == 0.0) formatterNoDecimals.format(amount) else formatterWithDecimals.format(amount)
        }
    }
}
