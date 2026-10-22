package com.akari.simpleledgerapp.background

enum class LedgerSortKey { Time, Amount }

object LedgerSort {
    fun sort(items: List<LedgerItem>, key: LedgerSortKey, reversed: Boolean): List<LedgerItem> {
        val comparator = when (key) {
            LedgerSortKey.Time -> compareBy<LedgerItem> { it.happenAt }
            LedgerSortKey.Amount -> compareBy<LedgerItem> { it.amountValue }
        }
        return items.sortedWith(if (reversed) comparator.reversed() else comparator)
    }
}
