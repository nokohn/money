package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtil {
    private val laoMonths = listOf(
        "ມັງກອນ", "ກຸມພາ", "ມີນາ", "ເມສາ", "ພຶດສະພາ", "ມິຖຸນາ",
        "ກໍລະກົດ", "ສິງຫາ", "ກັນຍາ", "ຕຸລາ", "ພະຈິກ", "ທັນວາ"
    )

    private val engMonths = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    private val engShortMonths = listOf(
        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    )

    fun todayString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(Date())
    }

    fun currentTimeString(): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.US)
        return sdf.format(Date())
    }

    fun currentYearMonth(): String {
        val sdf = SimpleDateFormat("yyyy-MM", Locale.US)
        return sdf.format(Date())
    }

    fun formatDate(dateStr: String, language: String = "LAO"): String {
        try {
            val parts = dateStr.split("-")
            if (parts.size == 3) {
                val year = parts[0]
                val monthIndex = parts[1].toInt() - 1
                val day = parts[2].toInt().toString()

                val today = todayString()
                val cal = Calendar.getInstance()
                cal.add(Calendar.DAY_OF_YEAR, -1)
                val yesterday = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)

                if (dateStr == today) {
                    return if (language == "LAO") "ມື້ນີ້ ($day ${laoMonths.getOrElse(monthIndex) { "" }})" else "Today ($day ${engShortMonths.getOrElse(monthIndex) { "" }})"
                }
                if (dateStr == yesterday) {
                    return if (language == "LAO") "ມື້ວານນີ້ ($day ${laoMonths.getOrElse(monthIndex) { "" }})" else "Yesterday ($day ${engShortMonths.getOrElse(monthIndex) { "" }})"
                }

                return if (language == "LAO") {
                    "$day ${laoMonths.getOrElse(monthIndex) { "" }} $year"
                } else {
                    "${engShortMonths.getOrElse(monthIndex) { "" }} $day, $year"
                }
            }
        } catch (_: Exception) { }
        return dateStr
    }

    fun formatMonthYear(yearMonth: String, language: String = "LAO"): String {
        try {
            val parts = yearMonth.split("-")
            if (parts.size >= 2) {
                val year = parts[0]
                val monthIndex = parts[1].toInt() - 1
                return if (language == "LAO") {
                    "ເດືອນ ${laoMonths.getOrElse(monthIndex) { "" }} $year"
                } else {
                    "${engMonths.getOrElse(monthIndex) { "" }} $year"
                }
            }
        } catch (_: Exception) { }
        return yearMonth
    }

    fun getLaoMonthName(monthIndex: Int): String = laoMonths.getOrElse(monthIndex) { "" }
    fun getEngMonthName(monthIndex: Int): String = engShortMonths.getOrElse(monthIndex) { "" }
}
