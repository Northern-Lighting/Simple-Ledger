package com.akari.simpleledgerapp.ui.pages

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.animateFloatingActionButton
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.akari.simpleledgerapp.background.LedgerItem
import com.akari.simpleledgerapp.background.LedgerSort
import com.akari.simpleledgerapp.background.LedgerSortKey
import com.akari.simpleledgerapp.ui.components.AddLedgerFab
import com.akari.simpleledgerapp.ui.components.LedgerEmptyHint
import com.akari.simpleledgerapp.ui.components.LedgerEntryDialog
import com.akari.simpleledgerapp.ui.components.LedgerItemCard
import com.akari.simpleledgerapp.ui.components.LedgerSelectionState
import com.akari.simpleledgerapp.ui.components.fadeThrough
import com.akari.simpleledgerapp.ui.components.rememberDeleteWithUndo
import java.time.LocalDate

private const val MillisPerDay = 86_400_000L
private val DefaultSheetPeek = 240.dp
private val MinSheetPeek = 120.dp
private val EmptyHintHeight = 160.dp
private val ListContentPadding = PaddingValues(
    start = 16.dp,
    top = 8.dp,
    end = 16.dp,
    bottom = 88.dp,
)
private val ListSpacing = Arrangement.spacedBy(8.dp)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryLedgerScreen(
    items: List<LedgerItem>,
    sortKey: LedgerSortKey,
    reversed: Boolean,
    onAddItem: (LedgerItem) -> Unit,
    onUpdateItem: (LedgerItem) -> Unit,
    onDeleteItem: (LedgerItem) -> Unit,
    onRestoreItems: (List<LedgerItem>) -> Unit,
    selection: LedgerSelectionState,
    modifier: Modifier = Modifier,
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = remember { LocalDate.now().toEpochDay() * MillisPerDay },
    )
    val selectedDate = datePickerState.selectedDateMillis
        ?.let { LocalDate.ofEpochDay(it / MillisPerDay) }
        ?: LocalDate.now()

    val sheetState = rememberStandardBottomSheetState(
        initialValue = SheetValue.PartiallyExpanded,
        skipHiddenState = true,
    )
    val scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState = sheetState)

    val density = LocalDensity.current
    val baseCorner = with(density) {
        MaterialTheme.shapes.extraLarge.topStart.toPx(Size.Zero, this).toDp()
    }
    val expandedFraction by animateFloatAsState(
        targetValue = if (sheetState.targetValue == SheetValue.Expanded) 1f else 0f,
        animationSpec = tween(durationMillis = 220),
        label = "sheetCornerExpansion",
    )
    val sheetCorner = baseCorner * (1f - expandedFraction)
    var datePickerHeight by remember { mutableStateOf(0.dp) }

    var editingItem by remember { mutableStateOf<LedgerItem?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    var snackbarHeight by remember { mutableIntStateOf(0) }
    val snackbarVisible = snackbarHostState.currentSnackbarData != null
    val fabLift by animateDpAsState(
        targetValue = if (snackbarVisible) {
            with(density) { snackbarHeight.toDp() } + 16.dp
        } else {
            0.dp
        },
        label = "fabLift",
    )
    val deleteWithUndo = rememberDeleteWithUndo(
        snackbarHostState = snackbarHostState,
        onDeleteItem = onDeleteItem,
        onRestoreItems = onRestoreItems,
    )

    fun itemsOf(date: LocalDate): List<LedgerItem> = LedgerSort.sort(
        items = items.filter { it.happenAt.toLocalDate() == date },
        key = sortKey,
        reversed = reversed,
    )

    val dayItems = remember(items, selectedDate, sortKey, reversed) { itemsOf(selectedDate) }

    LaunchedEffect(selectedDate) { selection.clear() }

    SideEffect {
        selection.publishVisible(dayItems)
        selection.onDeleteSelected = {
            val targets = selection.selectedItems(dayItems)
            selection.clear()
            deleteWithUndo(targets)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val sheetPeekHeight = if (datePickerHeight > 0.dp) {
                (maxHeight - datePickerHeight).coerceAtLeast(MinSheetPeek)
            } else {
                DefaultSheetPeek
            }

            BottomSheetScaffold(
                modifier = Modifier.fillMaxSize(),
                scaffoldState = scaffoldState,
                sheetPeekHeight = sheetPeekHeight,
                sheetShape = RoundedCornerShape(topStart = sheetCorner, topEnd = sheetCorner),
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                sheetContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                snackbarHost = {},
                sheetContent = {
                    AnimatedContent(
                        targetState = selectedDate,
                        transitionSpec = { fadeThrough() },
                        label = "historyDay",
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(),
                    ) { date ->
                        val animatedDayItems = remember(date, items, sortKey, reversed) {
                            itemsOf(date)
                        }
                        if (animatedDayItems.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(EmptyHintHeight),
                            ) {
                                LedgerEmptyHint()
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(
                                        enabled = selection.selectionMode,
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                        onClick = { selection.clear() },
                                    ),
                                contentPadding = ListContentPadding,
                                verticalArrangement = ListSpacing,
                            ) {
                                items(items = animatedDayItems, key = { it.id }) { item ->
                                    LedgerItemCard(
                                        item = item,
                                        selectionMode = selection.selectionMode,
                                        selected = item.id in selection.selectedIds,
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
                    }
                },
            ) {
                DatePicker(
                    state = datePickerState,
                    title = null,
                    headline = null,
                    showModeToggle = false,
                    modifier = Modifier
                        .fillMaxWidth()
                        .onSizeChanged { datePickerHeight = with(density) { it.height.toDp() } },
                )
            }
        }
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
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
        ) {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.onSizeChanged { snackbarHeight = it.height },
            )
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
            defaultDate = selectedDate,
        )
    }
}
