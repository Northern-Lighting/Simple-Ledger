package com.akari.simpleledgerapp.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.CheckableDropdownMenuItem
import androidx.compose.material3.DropdownMenuGroup
import androidx.compose.material3.DropdownMenuPopup
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.SelectableDropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import com.akari.simpleledgerapp.R
import com.akari.simpleledgerapp.background.LedgerSortKey

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LedgerSortDropdown(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    sortKey: LedgerSortKey,
    reversed: Boolean,
    groupByDate: Boolean,
    width: Dp,
    onSortKeyChange: (LedgerSortKey) -> Unit,
    onReversedChange: (Boolean) -> Unit,
    onGroupByDateChange: (Boolean) -> Unit,
) {
    val showGroupOption = sortKey == LedgerSortKey.Amount

    DropdownMenuPopup(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = Modifier.width(width),
    ) {
        DropdownMenuGroup(
            modifier = Modifier.width(width),
            shapes = MenuDefaults.groupShape(index = 0, count = 1),
            containerColor = MenuDefaults.groupStandardContainerColor,
        ) {
            SortOptionItems(
                sortKey = sortKey,
                reversed = reversed,
                groupByDate = groupByDate,
                itemCount = if (showGroupOption) 4 else 3,
                onSortKeyChange = onSortKeyChange,
                onReversedChange = onReversedChange,
                onGroupByDateChange = onGroupByDateChange,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SortOptionItems(
    sortKey: LedgerSortKey,
    reversed: Boolean,
    groupByDate: Boolean,
    itemCount: Int,
    onSortKeyChange: (LedgerSortKey) -> Unit,
    onReversedChange: (Boolean) -> Unit,
    onGroupByDateChange: (Boolean) -> Unit,
) {
    SelectableDropdownMenuItem(
        selected = sortKey == LedgerSortKey.Time,
        onClick = { onSortKeyChange(LedgerSortKey.Time) },
        text = { Text(stringResource(R.string.sort_by_time)) },
        shapes = MenuDefaults.itemShape(index = 0, count = itemCount),
        colors = MenuDefaults.selectableItemColors(),
        trailingContent = {
            Icon(
                imageVector = Icons.Outlined.Schedule,
                contentDescription = null,
                modifier = Modifier.size(MenuDefaults.TrailingIconSize),
            )
        },
    )
    SelectableDropdownMenuItem(
        selected = sortKey == LedgerSortKey.Amount,
        onClick = { onSortKeyChange(LedgerSortKey.Amount) },
        text = { Text(stringResource(R.string.sort_by_amount)) },
        shapes = MenuDefaults.itemShape(index = 1, count = itemCount),
        colors = MenuDefaults.selectableItemColors(),
        trailingContent = {
            Icon(
                imageVector = Icons.Outlined.AccountBalanceWallet,
                contentDescription = null,
                modifier = Modifier.size(MenuDefaults.TrailingIconSize),
            )
        },
    )
    CheckableDropdownMenuItem(
        checked = reversed,
        onCheckedChange = onReversedChange,
        text = { Text(stringResource(R.string.sort_descending)) },
        shapes = MenuDefaults.itemShape(index = 2, count = itemCount),
        colors = MenuDefaults.selectableItemColors(),
        trailingContent = {
            if (reversed) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    modifier = Modifier.size(MenuDefaults.TrailingIconSize),
                )
            }
        },
    )
    AnimatedVisibility(
        visible = itemCount == 4,
        enter = expandVertically(expandFrom = Alignment.Bottom) +
            fadeIn(animationSpec = tween(durationMillis = 160, delayMillis = 40)),
        exit = shrinkVertically(shrinkTowards = Alignment.Bottom) +
            fadeOut(animationSpec = tween(durationMillis = 120)),
    ) {
        CheckableDropdownMenuItem(
            checked = groupByDate,
            onCheckedChange = onGroupByDateChange,
            text = { Text(stringResource(R.string.group_by_date)) },
            shapes = MenuDefaults.itemShape(index = 3, count = 4),
            colors = MenuDefaults.selectableItemColors(),
            trailingContent = {
                if (groupByDate) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        modifier = Modifier.size(MenuDefaults.TrailingIconSize),
                    )
                }
            },
        )
    }
}
