package com.akari.simpleledgerapp.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.akari.simpleledgerapp.R

@Composable
fun AddLedgerFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    extended: Boolean = false,
    expanded: Boolean = true,
) {
    if (extended) {
        ExtendedFloatingActionButton(
            text = {
                Text(
                    text = stringResource(R.string.add_ledger_entry),
                )
            },
            icon = {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = null,
                )
            },
            onClick = onClick,
            modifier = modifier,
            expanded = expanded,
        )
    } else {
        FloatingActionButton(onClick = onClick, modifier = modifier) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = stringResource(R.string.add_ledger_entry),
            )
        }
    }
}
