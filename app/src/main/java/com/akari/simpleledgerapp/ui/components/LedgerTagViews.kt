package com.akari.simpleledgerapp.ui.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.akari.simpleledgerapp.R
import com.akari.simpleledgerapp.background.LedgerTag

data class LedgerTagColors(val container: Color, val content: Color)

private val WeChatLight = LedgerTagColors(Color(0xFFDCF0E1), Color(0xFF1E5B33))
private val WeChatDark = LedgerTagColors(Color(0xFF1C3A27), Color(0xFFA8D6B4))
private val AlipayLight = LedgerTagColors(Color(0xFFDCE8FB), Color(0xFF14468C))
private val AlipayDark = LedgerTagColors(Color(0xFF182B45), Color(0xFFA6C4F0))
private val OtherLight = LedgerTagColors(Color(0xFFFBE7CF), Color(0xFF7A4A17))
private val OtherDark = LedgerTagColors(Color(0xFF3A2A18), Color(0xFFF0C79A))

@Composable
fun ledgerTagColors(tag: LedgerTag): LedgerTagColors {
    val dark = isSystemInDarkTheme()
    return when (tag) {
        LedgerTag.WeChat -> if (dark) WeChatDark else WeChatLight
        LedgerTag.Alipay -> if (dark) AlipayDark else AlipayLight
        LedgerTag.Other -> if (dark) OtherDark else OtherLight
        LedgerTag.None -> LedgerTagColors(Color.Transparent, Color.Transparent)
    }
}

@Composable
fun ledgerTagLabel(tag: LedgerTag, tagText: String): String {
    val otherLabel = stringResource(R.string.tag_other)
    return when (tag) {
        LedgerTag.WeChat -> stringResource(R.string.tag_wechat)
        LedgerTag.Alipay -> stringResource(R.string.tag_alipay)
        LedgerTag.Other -> tagText.ifBlank { otherLabel }
        LedgerTag.None -> ""
    }
}

@Composable
fun LedgerCardTag(tag: LedgerTag, tagText: String, modifier: Modifier = Modifier) {
    val colors = ledgerTagColors(tag)
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraSmall,
        color = colors.container,
    ) {
        Text(
            text = ledgerTagLabel(tag, tagText),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            color = colors.content,
        )
    }
}
