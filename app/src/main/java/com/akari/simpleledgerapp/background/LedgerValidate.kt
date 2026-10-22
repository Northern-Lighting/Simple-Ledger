package com.akari.simpleledgerapp.background

import kotlin.math.abs

object LedgerValidate {
    const val MAX_ITEM_NAME_LENGTH = 20
    const val MAX_ITEM_NOTE_LENGTH = 50
    const val MAX_TAG_TEXT_LENGTH = 10
    const val MAX_AMOUNT_VALUE = 200_000_000_000L

    fun isValidItemName(value: String): Boolean {
        val trimmed = value.trim()
        return trimmed.isNotEmpty() && trimmed.length <= MAX_ITEM_NAME_LENGTH
    }

    fun isValidItemNote(value: String): Boolean = value.length <= MAX_ITEM_NOTE_LENGTH

    fun isValidAmountValue(value: Long): Boolean = abs(value) <= MAX_AMOUNT_VALUE
}
