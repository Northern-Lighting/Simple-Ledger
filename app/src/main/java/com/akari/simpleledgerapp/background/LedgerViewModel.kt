package com.akari.simpleledgerapp.background

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class LedgerViewModel(application: Application) : AndroidViewModel(application) {

    private val store = LedgerStore(File(application.filesDir, LedgerStore.FILE_NAME))
    private val saves = Channel<LedgerData>(Channel.UNLIMITED)

    private val _data = MutableStateFlow(LedgerData())
    val data: StateFlow<LedgerData> = _data.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            _data.value = store.load()
            for (data in saves) {
                store.save(data)
            }
        }
    }

    fun add(item: LedgerItem) = commit { it.add(item) }

    fun update(item: LedgerItem) = commit { it.update(item) }

    fun remove(item: LedgerItem) = commit { it.remove(item) }

    fun restore(items: List<LedgerItem>) = commit { data ->
        items.fold(data) { restored, item -> restored.restore(item) }
    }

    private fun commit(transform: (LedgerData) -> LedgerData) {
        val next = transform(_data.value)
        _data.value = next
        saves.trySend(next)
    }
}
