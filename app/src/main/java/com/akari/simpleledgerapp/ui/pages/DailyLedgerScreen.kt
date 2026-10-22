package com.akari.simpleledgerapp.ui.pages

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.animateFloatingActionButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.akari.simpleledgerapp.R
import com.akari.simpleledgerapp.background.LedgerFormat
import com.akari.simpleledgerapp.background.LedgerItem
import com.akari.simpleledgerapp.background.LedgerSort
import com.akari.simpleledgerapp.background.LedgerSortKey
import com.akari.simpleledgerapp.ui.components.AddLedgerFab
import com.akari.simpleledgerapp.ui.components.BottomRoundedCardCorners
import com.akari.simpleledgerapp.ui.components.FlatCardCorners
import com.akari.simpleledgerapp.ui.components.LedgerCardCorners
import com.akari.simpleledgerapp.ui.components.LedgerEmptyHint
import com.akari.simpleledgerapp.ui.components.LedgerEntryDialog
import com.akari.simpleledgerapp.ui.components.LedgerItemCard
import com.akari.simpleledgerapp.ui.components.LedgerSelectionState
import com.akari.simpleledgerapp.ui.components.TopRoundedCardCorners
import com.akari.simpleledgerapp.ui.components.rememberDeleteWithUndo
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate

private val ListContentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 88.dp)
private val ListSpacing = Arrangement.spacedBy(8.dp)
private val DateHeaderHeight = 48.dp
private val ScrollbarWidth = 3.dp
private val ScrollbarEndInset = 4.dp
private const val ScrollbarHideDelayMillis = 700L
private val ScrollbarMinThumb = 24.dp
private const val ScrollbarThumbAlpha = 0.3f
private const val FabCollapseScrollDelta = 2f
private const val MillisPerDay = 86_400_000L

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyLedgerScreen(
    items: List<LedgerItem>,
    sortKey: LedgerSortKey,
    reversed: Boolean,
    groupByDate: Boolean,
    onAddItem: (LedgerItem) -> Unit,
    onUpdateItem: (LedgerItem) -> Unit,
    onDeleteItem: (LedgerItem) -> Unit,
    onRestoreItems: (List<LedgerItem>) -> Unit,
    selection: LedgerSelectionState,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    var snackbarHeight by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current
    val snackbarVisible = snackbarHostState.currentSnackbarData != null
    val fabLift by animateDpAsState(
        targetValue = if (snackbarVisible) {
            with(density) { snackbarHeight.toDp() } + 16.dp
        } else {
            0.dp
        },
        label = "fabLift",
    )

    var editingItem by remember { mutableStateOf<LedgerItem?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var fabExpanded by remember { mutableStateOf(true) }
    val listState = rememberLazyListState()

    val deleteWithUndo = rememberDeleteWithUndo(
        snackbarHostState = snackbarHostState,
        onDeleteItem = onDeleteItem,
        onRestoreItems = onRestoreItems,
    )

    val showHeaders = when (sortKey) {
        LedgerSortKey.Time -> true
        LedgerSortKey.Amount -> groupByDate
    }
    val sortedItems = remember(items, sortKey, reversed) {
        LedgerSort.sort(items = items, key = sortKey, reversed = reversed)
    }
    val groups = remember(sortedItems) {
        sortedItems.groupBy { it.happenAt.toLocalDate() }
            .toSortedMap(compareByDescending { it })
    }

    SideEffect {
        selection.publishVisible(items)
        selection.onDeleteSelected = {
            val targets = selection.selectedItems(items)
            selection.clear()
            deleteWithUndo(targets)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = {
            Box(modifier = Modifier.padding(16.dp)) {
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.onSizeChanged { snackbarHeight = it.height },
                )
            }
        },
    ) { innerPadding ->
        val scrollConnection = remember {
            object : NestedScrollConnection {
                override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                    if (source == NestedScrollSource.UserInput) {
                        if (available.y < -FabCollapseScrollDelta) {
                            fabExpanded = false
                        } else if (available.y > FabCollapseScrollDelta) {
                            fabExpanded = true
                        }
                    }
                    return Offset.Zero
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .nestedScroll(scrollConnection),
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        enabled = selection.selectionMode,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { selection.clear() },
                    ),
                contentPadding = ListContentPadding,
                verticalArrangement = ListSpacing,
            ) {
                groups.forEach { (date, dayItems) ->
                    item(key = "date-$date") {
                        AnimatedVisibility(
                            visible = showHeaders,
                            enter = expandVertically(expandFrom = Alignment.Top) +
                                fadeIn(animationSpec = tween(durationMillis = 180, delayMillis = 40)),
                            exit = shrinkVertically(shrinkTowards = Alignment.Top) +
                                fadeOut(animationSpec = tween(durationMillis = 120)),
                        ) {
                            DateHeader(
                                date = date,
                                selectedCount = dayItems.count { it.id in selection.selectedIds },
                                totalCount = dayItems.size,
                                onToggleSelectAll = { selection.toggleAll(dayItems.map { it.id }) },
                                onDateClick = { showDatePicker = true },
                            )
                        }
                    }
                    if (showHeaders) {
                        itemsIndexed(items = dayItems, key = { _, item -> item.id }) { index, item ->
                            val selected = item.id in selection.selectedIds
                            val corners = when {
                                selected -> LedgerCardCorners()
                                dayItems.size == 1 -> LedgerCardCorners()
                                index == 0 -> TopRoundedCardCorners
                                index == dayItems.lastIndex -> BottomRoundedCardCorners
                                else -> FlatCardCorners
                            }
                            LedgerItemCard(
                                item = item,
                                selectionMode = selection.selectionMode,
                                selected = selected,
                                corners = corners,
                                onToggleSelection = { selection.toggle(item) },
                                onSwipeSelect = { selection.select(item) },
                                onEdit = {
                                    editingItem = item
                                    showDialog = true
                                },
                                onDelete = { deleteWithUndo(listOf(item)) },
                                modifier = Modifier.animateItem(),
                            )
                        }
                    }
                }
                if (!showHeaders) {
                    items(items = sortedItems, key = { it.id }) { item ->
                        LedgerItemCard(
                            item = item,
                            selectionMode = selection.selectionMode,
                            selected = item.id in selection.selectedIds,
                            showDate = true,
                            onToggleSelection = { selection.toggle(item) },
                            onSwipeSelect = { selection.select(item) },
                            onEdit = {
                                editingItem = item
                                showDialog = true
                            },
                            onDelete = { deleteWithUndo(listOf(item)) },
                            modifier = Modifier.animateItem(),
                        )
                    }
                }
            }
            if (items.isEmpty()) {
                LedgerEmptyHint()
            }
            LedgerScrollbar(
                listState = listState,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = ScrollbarEndInset),
            )
            AddLedgerFab(
                onClick = {
                    if (!selection.selectionMode) {
                        editingItem = null
                        showDialog = true
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .offset(y = -fabLift)
                    .animateFloatingActionButton(
                        visible = !selection.selectionMode,
                        alignment = Alignment.BottomEnd,
                    ),
                extended = true,
                expanded = fabExpanded,
            )
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = LocalDate.now().toEpochDay() * MillisPerDay,
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val picked = datePickerState.selectedDateMillis?.let {
                            LocalDate.ofEpochDay(it / MillisPerDay)
                        }
                        showDatePicker = false
                        if (picked != null) {
                            val targetIndex = groups.keys.indexOfFirst { !it.isAfter(picked) }
                            if (targetIndex >= 0) {
                                var itemIndex = 0
                                groups.entries.forEachIndexed { index, entry ->
                                    if (index < targetIndex) {
                                        itemIndex += 1 + entry.value.size
                                    }
                                }
                                scope.launch { listState.animateScrollToItem(itemIndex) }
                            }
                        }
                    },
                ) {
                    Text(stringResource(R.string.dialog_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.dialog_cancel))
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showDialog) {
        LedgerEntryDialog(
            initialItem = editingItem,
            onDismiss = {
                showDialog = false
                editingItem = null
            },
            onConfirm = { item ->
                if (editingItem == null) {
                    onAddItem(item)
                } else {
                    onUpdateItem(item)
                }
                showDialog = false
                editingItem = null
            },
        )
    }
}

@Composable
private fun DateHeader(
    date: LocalDate,
    selectedCount: Int,
    totalCount: Int,
    onToggleSelectAll: () -> Unit,
    onDateClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = DateHeaderHeight)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDateClick,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = if (date == LocalDate.now()) {
                stringResource(R.string.date_today)
            } else {
                LedgerFormat.dateText(date)
            },
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.weight(1f))
        if (totalCount > 0) {
            IconButton(onClick = onToggleSelectAll) {
                Icon(
                    imageVector = if (selectedCount == totalCount) {
                        Icons.Filled.CheckCircle
                    } else {
                        Icons.Outlined.CheckCircle
                    },
                    contentDescription = stringResource(R.string.select_all),
                    tint = if (selectedCount == totalCount) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
        }
    }
}

@Composable
private fun LedgerScrollbar(
    listState: LazyListState,
    modifier: Modifier = Modifier,
) {
    var active by remember { mutableStateOf(false) }
    LaunchedEffect(listState.isScrollInProgress) {
        if (listState.isScrollInProgress) {
            active = true
        } else {
            delay(ScrollbarHideDelayMillis)
            active = false
        }
    }
    val alpha by animateFloatAsState(
        targetValue = if (active) 1f else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "scrollbarAlpha",
    )
    val thumbColor = MaterialTheme.colorScheme.onSurface.copy(alpha = ScrollbarThumbAlpha)

    Canvas(
        modifier = modifier
            .width(ScrollbarWidth)
            .fillMaxHeight()
            .graphicsLayer { this.alpha = alpha },
    ) {
        val layoutInfo = listState.layoutInfo
        val totalItems = layoutInfo.totalItemsCount
        val visibleItems = layoutInfo.visibleItemsInfo.size
        if (totalItems == 0 || visibleItems >= totalItems) {
            return@Canvas
        }
        val thumbHeight = (size.height * visibleItems / totalItems)
            .coerceAtLeast(ScrollbarMinThumb.toPx())
        val scrollFraction = (
            listState.firstVisibleItemIndex.toFloat() / (totalItems - visibleItems).toFloat()
            ).coerceIn(0f, 1f)
        drawRoundRect(
            color = thumbColor,
            topLeft = Offset(0f, (size.height - thumbHeight) * scrollFraction),
            size = Size(size.width, thumbHeight),
            cornerRadius = CornerRadius(size.width / 2f),
        )
    }
}
