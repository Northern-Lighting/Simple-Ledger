package com.akari.simpleledgerapp.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import com.akari.simpleledgerapp.background.AppSettings
import com.akari.simpleledgerapp.background.HomeStyle
import com.akari.simpleledgerapp.background.LanguageMode
import com.akari.simpleledgerapp.background.LedgerSortKey
import com.akari.simpleledgerapp.background.LedgerTagFilter
import com.akari.simpleledgerapp.background.filterItemsByTag
import com.akari.simpleledgerapp.R
import com.akari.simpleledgerapp.background.LedgerItem
import com.akari.simpleledgerapp.background.LedgerViewModel
import com.akari.simpleledgerapp.ui.components.LedgerAnalysisDialog
import com.akari.simpleledgerapp.ui.components.LedgerFilterDialog
import com.akari.simpleledgerapp.ui.components.LedgerToolbarMenu
import com.akari.simpleledgerapp.ui.components.fadeThrough
import com.akari.simpleledgerapp.ui.components.rememberLedgerSelection
import com.akari.simpleledgerapp.ui.pages.DailyLedgerScreen
import com.akari.simpleledgerapp.ui.pages.HistoryLedgerScreen
import com.akari.simpleledgerapp.ui.pages.SettingsScreen

private const val BarFadeDuration = 180
private const val BarEnterColorDuration = 225
private const val BarExitColorDuration = 195

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    ledgerViewModel: LedgerViewModel,
    settings: AppSettings,
    onSettingsChange: (AppSettings) -> Unit,
    onLanguageChange: (LanguageMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val data by ledgerViewModel.data.collectAsState()
    var selectedIndex by rememberSaveable { mutableIntStateOf(0) }
    var sortKey by rememberSaveable { mutableStateOf(LedgerSortKey.Time) }
    var sortReversed by rememberSaveable { mutableStateOf(true) }
    var groupByDate by rememberSaveable { mutableStateOf(true) }
    var showFilterDialog by remember { mutableStateOf(false) }
    var showAnalysisDialog by remember { mutableStateOf(false) }
    var tagFilters by remember { mutableStateOf(LedgerTagFilter.entries.toSet()) }
    val destination = TopLevelDestination.entries[selectedIndex]
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val selection = rememberLedgerSelection()

    BackHandler(enabled = selection.selectionMode) { selection.clear() }

    LaunchedEffect(destination) { selection.clear() }

    LaunchedEffect(selection.selectionMode) {
        if (selection.selectionMode) {
            val start = scrollBehavior.state.heightOffset
            if (start != 0f) {
                animate(
                    initialValue = start,
                    targetValue = 0f,
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                ) { value, _ ->
                    scrollBehavior.state.heightOffset = value
                }
            }
        } else {
            scrollBehavior.state.heightOffset = 0f
            scrollBehavior.state.contentOffset = 0f
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            val contextual = selection.selectionMode
            val colorSpec = if (contextual) {
                tween<Color>(durationMillis = BarEnterColorDuration)
            } else {
                tween<Color>(durationMillis = BarExitColorDuration)
            }
            val containerColor by animateColorAsState(
                targetValue = if (contextual) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceContainer
                },
                animationSpec = colorSpec,
                label = "topBarContainerColor",
            )
            val contentColor by animateColorAsState(
                targetValue = if (contextual) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                animationSpec = colorSpec,
                label = "topBarContentColor",
            )
            val activeScrollBehavior = if (contextual) null else scrollBehavior

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        alpha = if (contextual) {
                            1f
                        } else {
                            val limit = scrollBehavior.state.heightOffsetLimit
                            if (limit == 0f) {
                                1f
                            } else {
                                (1f + scrollBehavior.state.heightOffset / limit).coerceIn(0f, 1f)
                            }
                        }
                    },
            ) {
                AnimatedContent(
                    targetState = contextual,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(durationMillis = BarFadeDuration))
                            .togetherWith(
                                fadeOut(animationSpec = tween(durationMillis = BarFadeDuration)),
                            )
                    },
                    label = "topBarMode",
                ) { inSelection ->
                    if (inSelection) {
                        ContextualTopBar(
                            selectedCount = selection.selectedCount,
                            containerColor = containerColor,
                            contentColor = contentColor,
                            onClearSelection = { selection.clear() },
                            onDelete = { selection.onDeleteSelected?.invoke() },
                            scrollBehavior = activeScrollBehavior,
                        )
                    } else {
                        DefaultTopBar(
                            destination = destination,
                            sortKey = sortKey,
                            sortReversed = sortReversed,
                            groupByDate = groupByDate,
                            containerColor = containerColor,
                            contentColor = contentColor,
                            onSortKeyChange = { sortKey = it },
                            onReversedChange = { sortReversed = it },
                            onGroupByDateChange = { groupByDate = it },
                            onFilterClick = { showFilterDialog = true },
                            onSelectAllClick = { selection.selectAll() },
                            onAnalysisClick = { showAnalysisDialog = true },
                            scrollBehavior = activeScrollBehavior,
                        )
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar {
                TopLevelDestination.entries.forEachIndexed { index, item ->
                    val selected = index == selectedIndex
                    NavigationBarItem(
                        selected = selected,
                        onClick = { selectedIndex = index },
                        icon = {
                            Icon(
                                imageVector = if (selected) item.selectedIcon else item.icon,
                                contentDescription = null,
                            )
                        },
                        label = { Text(stringResource(item.labelRes)) },
                    )
                }
            }
        },
    ) { innerPadding ->
        val onAddItem = remember(ledgerViewModel) { { item: LedgerItem -> ledgerViewModel.add(item) } }
        val onUpdateItem = remember(ledgerViewModel) { { item: LedgerItem -> ledgerViewModel.update(item) } }
        val onDeleteItem = remember(ledgerViewModel) { { item: LedgerItem -> ledgerViewModel.remove(item) } }
        val onRestoreItems = remember(ledgerViewModel) { { targets: List<LedgerItem> -> ledgerViewModel.restore(targets) } }
        val filteredItems = remember(data.items, tagFilters) {
            filterItemsByTag(data.items, tagFilters)
        }

        Box(modifier = Modifier.padding(innerPadding)) {
            AnimatedContent(
                targetState = destination,
                transitionSpec = { fadeThrough() },
                label = "pageTransition",
            ) { target ->
                when (target) {
                    TopLevelDestination.DailyLedger -> if (settings.homeStyle == HomeStyle.Calendar) {
                        HistoryLedgerScreen(
                            items = filteredItems,
                            sortKey = sortKey,
                            reversed = sortReversed,
                            onAddItem = onAddItem,
                            onUpdateItem = onUpdateItem,
                            onDeleteItem = onDeleteItem,
                            onRestoreItems = onRestoreItems,
                            selection = selection,
                        )
                    } else {
                        DailyLedgerScreen(
                            items = filteredItems,
                            sortKey = sortKey,
                            reversed = sortReversed,
                            groupByDate = groupByDate,
                            onAddItem = onAddItem,
                            onUpdateItem = onUpdateItem,
                            onDeleteItem = onDeleteItem,
                            onRestoreItems = onRestoreItems,
                            selection = selection,
                        )
                    }

                    TopLevelDestination.Settings -> SettingsScreen(
                        settings = settings,
                        onSettingsChange = onSettingsChange,
                        onLanguageChange = onLanguageChange,
                    )
                }
            }
        }
    }

    if (showFilterDialog) {
        LedgerFilterDialog(
            selectedFilters = tagFilters,
            onSelectionChange = { tagFilters = it },
            onDismiss = { showFilterDialog = false },
        )
    }

    if (showAnalysisDialog) {
        LedgerAnalysisDialog(onDismiss = { showAnalysisDialog = false })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DefaultTopBar(
    destination: TopLevelDestination,
    sortKey: LedgerSortKey,
    sortReversed: Boolean,
    groupByDate: Boolean,
    contentColor: Color,
    containerColor: Color,
    onSortKeyChange: (LedgerSortKey) -> Unit,
    onReversedChange: (Boolean) -> Unit,
    onGroupByDateChange: (Boolean) -> Unit,
    onFilterClick: () -> Unit,
    onSelectAllClick: () -> Unit,
    onAnalysisClick: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior?,
) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = containerColor,
            scrolledContainerColor = containerColor,
            titleContentColor = contentColor,
            actionIconContentColor = contentColor,
            navigationIconContentColor = contentColor,
        ),
        title = {
            AnimatedContent(
                targetState = destination,
                transitionSpec = { fadeThrough() },
                label = "titleTransition",
            ) { target ->
                Text(stringResource(target.labelRes))
            }
        },
        actions = {
            AnimatedContent(
                targetState = destination,
                transitionSpec = { fadeThrough() },
                label = "actionsTransition",
            ) { target ->
                if (target != TopLevelDestination.Settings) {
                    LedgerToolbarMenu(
                        sortKey = sortKey,
                        reversed = sortReversed,
                        groupByDate = groupByDate,
                        onSortKeyChange = onSortKeyChange,
                        onReversedChange = onReversedChange,
                        onGroupByDateChange = onGroupByDateChange,
                        onFilterClick = onFilterClick,
                        onSelectAllClick = onSelectAllClick,
                        onAnalysisClick = onAnalysisClick,
                    )
                }
            }
        },
        scrollBehavior = scrollBehavior,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContextualTopBar(
    selectedCount: Int,
    contentColor: Color,
    containerColor: Color,
    onClearSelection: () -> Unit,
    onDelete: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior?,
) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = containerColor,
            scrolledContainerColor = containerColor,
            titleContentColor = contentColor,
            actionIconContentColor = contentColor,
            navigationIconContentColor = contentColor,
        ),
        navigationIcon = {
            IconButton(onClick = onClearSelection) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.dialog_cancel),
                )
            }
        },
        title = {
            Text(
                text = stringResource(R.string.selected_items, selectedCount),
                style = MaterialTheme.typography.titleLarge,
            )
        },
        actions = {
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Outlined.DeleteOutline,
                    contentDescription = stringResource(R.string.delete_selected_items),
                )
            }
        },
        scrollBehavior = scrollBehavior,
    )
}
