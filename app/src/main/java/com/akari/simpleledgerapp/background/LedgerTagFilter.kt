package com.akari.simpleledgerapp.background

enum class LedgerTagFilter { WeChat, Alipay, Other, Untagged }

fun LedgerItem.matchesTagFilter(filters: Set<LedgerTagFilter>): Boolean {
    if (filters.isEmpty()) {
        return true
    }
    return when (tag) {
        LedgerTag.None -> LedgerTagFilter.Untagged in filters
        LedgerTag.WeChat -> LedgerTagFilter.WeChat in filters
        LedgerTag.Alipay -> LedgerTagFilter.Alipay in filters
        LedgerTag.Other -> LedgerTagFilter.Other in filters
    }
}

fun filterItemsByTag(items: List<LedgerItem>, filters: Set<LedgerTagFilter>): List<LedgerItem> =
    if (filters.isEmpty()) items else items.filter { it.matchesTagFilter(filters) }
