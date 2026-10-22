package com.akari.simpleledgerapp.background

import java.time.LocalDate

data class LedgerData(val items: List<LedgerItem> = emptyList()) {

    fun add(item: LedgerItem): LedgerData = LedgerData(insert(item.copy(id = nextId())))

    fun restore(item: LedgerItem): LedgerData {
        val restored = if (items.any { it.id == item.id }) item.copy(id = nextId()) else item
        return LedgerData(insert(restored))
    }

    fun update(item: LedgerItem): LedgerData {
        if (items.none { it.id == item.id }) {
            return this
        }
        return LedgerData(insert(item, items.filterNot { it.id == item.id }))
    }

    fun remove(item: LedgerItem): LedgerData = LedgerData(items.filterNot { it.id == item.id })

    fun itemsOfDay(date: LocalDate): List<LedgerItem> =
        items.filter { it.happenAt.toLocalDate() == date }

    private fun nextId(): Long = (items.maxOfOrNull { it.id } ?: 0L) + 1

    private fun insert(item: LedgerItem, source: List<LedgerItem> = items): List<LedgerItem> {
        val index = source.indexOfFirst { it.happenAt > item.happenAt }
        return if (index < 0) source + item else source.toMutableList().apply { add(index, item) }
    }
}
