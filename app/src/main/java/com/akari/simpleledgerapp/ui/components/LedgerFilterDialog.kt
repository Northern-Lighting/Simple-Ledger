package com.akari.simpleledgerapp.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.akari.simpleledgerapp.R
import com.akari.simpleledgerapp.background.LedgerTagFilter

@Composable
fun LedgerFilterDialog(
    selectedFilters: Set<LedgerTagFilter>,
    onSelectionChange: (Set<LedgerTagFilter>) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.filter_ledger)) },
        text = {
            Column {
                LedgerTagFilter.entries.forEach { filter ->
                    val checked = filter in selectedFilters
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .toggleable(
                                value = checked,
                                role = Role.Checkbox,
                                onValueChange = { enabled ->
                                    onSelectionChange(
                                        if (enabled) {
                                            selectedFilters + filter
                                        } else {
                                            selectedFilters - filter
                                        },
                                    )
                                },
                            )
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(
                            checked = checked,
                            onCheckedChange = null,
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(text = stringResource(filterLabel(filter)))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.dialog_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.dialog_cancel))
            }
        },
    )
}

private fun filterLabel(filter: LedgerTagFilter): Int = when (filter) {
    LedgerTagFilter.WeChat -> R.string.tag_wechat
    LedgerTagFilter.Alipay -> R.string.tag_alipay
    LedgerTagFilter.Other -> R.string.tag_other
    LedgerTagFilter.Untagged -> R.string.tag_untagged
}
