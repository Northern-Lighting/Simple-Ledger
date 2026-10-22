package com.akari.simpleledgerapp.background

import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.time.LocalDateTime

class LedgerStore(private val file: File) {

    fun load(): LedgerData {
        if (!file.exists()) {
            return LedgerData()
        }
        return runCatching { decode(file.readText()) }.getOrDefault(LedgerData())
    }

    fun save(data: LedgerData) {
        file.parentFile?.mkdirs()
        val temporary = File(file.absoluteFile.parentFile, "${file.name}.tmp")
        temporary.writeText(encode(data))
        Files.move(
            temporary.toPath(),
            file.toPath(),
            StandardCopyOption.REPLACE_EXISTING,
        )
    }

    private fun encode(data: LedgerData): String {
        val items = JSONArray()
        data.items.forEach { item ->
            items.put(
                JSONObject()
                    .put(KEY_ID, item.id)
                    .put(KEY_ITEM_NAME, item.itemName)
                    .put(KEY_ITEM_NOTE, item.itemNote)
                    .put(KEY_AMOUNT_VALUE, item.amountValue)
                    .put(KEY_HAPPEN_AT, item.happenAt.toString())
                    .put(KEY_TAG, item.tag.name)
                    .put(KEY_TAG_TEXT, item.tagText),
            )
        }
        return JSONObject().put(KEY_ITEMS, items).toString()
    }

    private fun decode(source: String): LedgerData {
        val items = JSONObject(source).getJSONArray(KEY_ITEMS)
        return LedgerData(
            (0 until items.length())
                .map { items.getJSONObject(it) }
                .map { json ->
                    LedgerItem(
                        id = json.getLong(KEY_ID),
                        itemName = json.getString(KEY_ITEM_NAME),
                        amountValue = json.getLong(KEY_AMOUNT_VALUE),
                        happenAt = LocalDateTime.parse(json.getString(KEY_HAPPEN_AT)),
                        itemNote = json.optString(KEY_ITEM_NOTE),
                        tag = runCatching {
                            LedgerTag.valueOf(json.optString(KEY_TAG))
                        }.getOrDefault(LedgerTag.None),
                        tagText = json.optString(KEY_TAG_TEXT),
                    )
                }
                .sortedBy { it.happenAt },
        )
    }

    companion object {
        const val FILE_NAME = "ledger.json"

        private const val KEY_ITEMS = "items"
        private const val KEY_ID = "id"
        private const val KEY_ITEM_NAME = "itemName"
        private const val KEY_ITEM_NOTE = "itemNote"
        private const val KEY_AMOUNT_VALUE = "amountValue"
        private const val KEY_HAPPEN_AT = "happenAt"
        private const val KEY_TAG = "tag"
        private const val KEY_TAG_TEXT = "tagText"
    }
}
