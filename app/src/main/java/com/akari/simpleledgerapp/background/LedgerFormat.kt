package com.akari.simpleledgerapp.background

import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs

object LedgerFormat {
    const val CURRENCY_SYMBOL = "￥"

    private const val CENTS_PER_YUAN = 100L
    private val TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm")
    private val DATE_FORMATTER = DateTimeFormatter.ofPattern("M月d日", Locale.SIMPLIFIED_CHINESE)
    private val DATE_WITH_YEAR_FORMATTER =
        DateTimeFormatter.ofPattern("yyyy年M月d日", Locale.SIMPLIFIED_CHINESE)
    private val WEEKDAY_LABELS = listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")

    fun dateText(date: LocalDate, today: LocalDate = LocalDate.now()): String {
        val formatter = if (date.year == today.year) DATE_FORMATTER else DATE_WITH_YEAR_FORMATTER
        return date.format(formatter) + " " + WEEKDAY_LABELS[date.dayOfWeek.value - 1]
    }

    fun shortDateText(date: LocalDate): String = date.format(DATE_FORMATTER)

    fun amountWithSymbol(amountValue: Long): String {
        val sign = when {
            amountValue > 0 -> "+"
            amountValue < 0 -> "-"
            else -> ""
        }
        return "$sign$CURRENCY_SYMBOL${amountFixed(abs(amountValue))}"
    }

    fun amountFixed(amountValue: Long): String {
        val absolute = abs(amountValue)
        val sign = if (amountValue < 0) "-" else ""
        val yuan = absolute / CENTS_PER_YUAN
        val cents = (absolute % CENTS_PER_YUAN).toString().padStart(2, '0')
        return "$sign$yuan.$cents"
    }

    fun parseAmount(text: String): Long? {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) {
            return null
        }
        val negative = trimmed.startsWith('-')
        val parts = (if (negative) trimmed.substring(1) else trimmed).split('.')
        if (parts.size > 2) {
            return null
        }
        val centText = if (parts.size == 2) parts[1] else ""
        if (centText.length > 2) {
            return null
        }
        val yuan = parts[0].ifEmpty { "0" }.toLongOrNull() ?: return null
        val cents = centText.padEnd(2, '0').toLongOrNull() ?: return null
        val value = yuan * CENTS_PER_YUAN + cents
        return if (negative) -value else value
    }

    fun timeText(time: LocalTime): String = time.format(TIME_FORMATTER)
}
