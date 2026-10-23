package com.akari.simpleledgerapp.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import com.akari.simpleledgerapp.R
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun ledgerDateText(date: LocalDate): String {
    val locale = LocalConfiguration.current.locales[0]
    val today = LocalDate.now()
    val pattern = stringResource(
        if (date.year == today.year) R.string.date_pattern_short else R.string.date_pattern_with_year,
    )
    val weekdayPattern = stringResource(R.string.date_pattern_weekday)

    return remember(date, locale, pattern, weekdayPattern) {
        val day = date.format(DateTimeFormatter.ofPattern(pattern, locale))
        val weekday = date.format(DateTimeFormatter.ofPattern(weekdayPattern, locale))
        "$day $weekday"
    }
}

@Composable
fun ledgerShortDateText(date: LocalDate): String {
    val locale = LocalConfiguration.current.locales[0]
    val pattern = stringResource(R.string.date_pattern_short)

    return remember(date, locale, pattern) {
        date.format(DateTimeFormatter.ofPattern(pattern, locale))
    }
}
