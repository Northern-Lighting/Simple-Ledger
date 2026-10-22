package com.akari.simpleledgerapp.ui.components

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.akari.simpleledgerapp.R
import com.akari.simpleledgerapp.background.LedgerItem
import kotlinx.coroutines.launch

@Composable
fun rememberDeleteWithUndo(
    snackbarHostState: SnackbarHostState,
    onDeleteItem: (LedgerItem) -> Unit,
    onRestoreItems: (List<LedgerItem>) -> Unit,
): (List<LedgerItem>) -> Unit {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val undoLabel = stringResource(R.string.dialog_undo)

    return remember(snackbarHostState, onDeleteItem, onRestoreItems, undoLabel, context) {
        { targets: List<LedgerItem> ->
            if (targets.isNotEmpty()) {
                targets.forEach(onDeleteItem)
                scope.launch {
                    snackbarHostState.currentSnackbarData?.dismiss()
                    val result = snackbarHostState.showSnackbar(
                        message = context.getString(R.string.deleted_items, targets.size),
                        actionLabel = undoLabel,
                        withDismissAction = true,
                        duration = SnackbarDuration.Short,
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        onRestoreItems(targets)
                    }
                }
            }
        }
    }
}
