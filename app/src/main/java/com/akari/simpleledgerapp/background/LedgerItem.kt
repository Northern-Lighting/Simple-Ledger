package com.akari.simpleledgerapp.background

import java.time.LocalDateTime

enum class LedgerTag { None, WeChat, Alipay, Other }

data class LedgerItem(
    val id: Long,
    val itemName: String,
    val amountValue: Long,
    val happenAt: LocalDateTime,
    val itemNote: String = "",
    val tag: LedgerTag = LedgerTag.None,
    val tagText: String = "",
)
