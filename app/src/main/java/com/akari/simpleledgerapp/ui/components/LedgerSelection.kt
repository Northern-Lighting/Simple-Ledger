package com.akari.simpleledgerapp.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.akari.simpleledgerapp.background.LedgerItem

@Stable
class LedgerSelectionState {
    var selectedIds by mutableStateOf(emptySet<Long>())
        private set

    var visibleIds by mutableStateOf(emptySet<Long>())
        private set

    var onDeleteSelected: (() -> Unit)? = null

    val selectionMode: Boolean
        get() = selectedIds.isNotEmpty()

    val selectedCount: Int
        get() = selectedIds.size

    val allSelected: Boolean
        get() = visibleIds.isNotEmpty() && selectedIds.size == visibleIds.size

    fun toggle(item: LedgerItem) {
        selectedIds = if (item.id in selectedIds) {
            selectedIds - item.id
        } else {
            selectedIds + item.id
        }
    }

    fun select(item: LedgerItem) {
        selectedIds = selectedIds + item.id
    }

    fun selectAll() {
        selectedIds = visibleIds
    }

    fun toggleAll(ids: Collection<Long>) {
        val target = ids.toSet()
        if (target.isEmpty()) {
            return
        }
        selectedIds = if (selectedIds.containsAll(target)) selectedIds - target else selectedIds + target
    }

    fun isAllSelected(ids: Collection<Long>): Boolean =
        ids.isNotEmpty() && selectedIds.containsAll(ids.toSet())

    fun clear() {
        selectedIds = emptySet()
    }

    fun selectedItems(items: List<LedgerItem>): List<LedgerItem> =
        items.filter { it.id in selectedIds }

    fun publishVisible(items: List<LedgerItem>) {
        if (items === publishedItems) {
            return
        }
        publishedItems = items
        visibleIds = items.mapTo(HashSet(items.size)) { it.id }
    }

    private var publishedItems: List<LedgerItem>? = null
}

@Composable
fun rememberLedgerSelection(): LedgerSelectionState = remember { LedgerSelectionState() }
