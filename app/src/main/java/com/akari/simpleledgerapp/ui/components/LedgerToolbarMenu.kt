package com.akari.simpleledgerapp.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material3.DropdownMenuGroup
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenuPopup
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.akari.simpleledgerapp.R
import com.akari.simpleledgerapp.background.LedgerSortKey

private val ToolbarMenuMinWidth = 112.dp
private val ToolbarMenuMaxWidth = 280.dp
private val ToolbarMenuPreferredWidth = 180.dp
private val ToolbarMenuScreenMargin = 16.dp

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LedgerToolbarMenu(
    sortKey: LedgerSortKey,
    reversed: Boolean,
    groupByDate: Boolean,
    onSortKeyChange: (LedgerSortKey) -> Unit,
    onReversedChange: (Boolean) -> Unit,
    onGroupByDateChange: (Boolean) -> Unit,
    onFilterClick: () -> Unit,
    onSelectAllClick: () -> Unit,
    onAnalysisClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    var sortExpanded by remember { mutableStateOf(false) }
    val density = LocalDensity.current
    val windowWidth = with(density) {
        LocalWindowInfo.current.containerSize.width.toDp()
    }
    val menuWidth = (windowWidth - ToolbarMenuScreenMargin * 2)
        .coerceIn(ToolbarMenuMinWidth, ToolbarMenuPreferredWidth)

    Box(modifier = modifier) {
        IconButton(onClick = { expanded = true }) {
            Icon(
                imageVector = Icons.Filled.MoreVert,
                contentDescription = stringResource(R.string.more_actions),
            )
        }
        DropdownMenuPopup(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .widthIn(min = ToolbarMenuMinWidth, max = ToolbarMenuMaxWidth)
                .width(menuWidth),
        ) {
            DropdownMenuGroup(
                shapes = MenuDefaults.groupShape(index = 0, count = 1),
                containerColor = MenuDefaults.groupVibrantContainerColor,
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.sort_method)) },
                    onClick = {
                        expanded = false
                        sortExpanded = true
                    },
                    shape = MenuDefaults.itemShape(index = 0, count = 4).shape,
                    colors = MenuDefaults.itemVibrantColors(),
                    trailingContent = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            modifier = Modifier.size(MenuDefaults.TrailingIconSize),
                        )
                    },
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.filter_ledger)) },
                    onClick = {
                        expanded = false
                        onFilterClick()
                    },
                    shape = MenuDefaults.itemShape(index = 1, count = 4).shape,
                    colors = MenuDefaults.itemVibrantColors(),
                    trailingContent = {
                        Icon(
                            imageVector = Icons.Outlined.FilterList,
                            contentDescription = null,
                            modifier = Modifier.size(MenuDefaults.TrailingIconSize),
                        )
                    },
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.analysis)) },
                    onClick = {
                        expanded = false
                        onAnalysisClick()
                    },
                    shape = MenuDefaults.itemShape(index = 2, count = 4).shape,
                    colors = MenuDefaults.itemVibrantColors(),
                    trailingContent = {
                        Icon(
                            imageVector = Icons.Outlined.Insights,
                            contentDescription = null,
                            modifier = Modifier.size(MenuDefaults.TrailingIconSize),
                        )
                    },
                )
                HorizontalDivider(modifier = Modifier.padding(MenuDefaults.HorizontalDividerPadding))
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.select_all)) },
                    onClick = {
                        expanded = false
                        onSelectAllClick()
                    },
                    shape = MenuDefaults.itemShape(index = 3, count = 4).shape,
                    colors = MenuDefaults.itemVibrantColors(),
                    trailingContent = {
                        Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(MenuDefaults.TrailingIconSize),
                        )
                    },
                )
            }
        }
        LedgerSortDropdown(
            expanded = sortExpanded,
            onDismissRequest = { sortExpanded = false },
            sortKey = sortKey,
            reversed = reversed,
            groupByDate = groupByDate,
            width = menuWidth,
            onSortKeyChange = onSortKeyChange,
            onReversedChange = onReversedChange,
            onGroupByDateChange = onGroupByDateChange,
        )
    }
}
