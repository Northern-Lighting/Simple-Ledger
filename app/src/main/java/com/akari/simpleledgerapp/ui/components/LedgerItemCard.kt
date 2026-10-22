package com.akari.simpleledgerapp.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateBounds
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.AnimationVector4D
import androidx.compose.animation.core.TwoWayConverter
import androidx.compose.animation.core.animateValueAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenuGroup
import androidx.compose.material3.DropdownMenuPopup
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.DropdownMenuPopupPositionProvider
import androidx.compose.ui.unit.DpOffset
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LookaheadScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import androidx.compose.ui.graphics.TransformOrigin
import com.akari.simpleledgerapp.R
import com.akari.simpleledgerapp.background.LedgerFormat
import com.akari.simpleledgerapp.background.LedgerItem
import com.akari.simpleledgerapp.background.LedgerTag
import java.time.LocalDate
import kotlinx.coroutines.launch

private val SwipeSelectThreshold = 64.dp
private const val SwipeSelectTriggerFraction = 0.75f
private val MenuItemMinWidth = 112.dp
private val MenuIconInset = 12.dp

val LedgerCardCornerRadius = 16.dp
private const val CornerAnimationMillis = 200

@Immutable
data class LedgerCardCorners(
    val topStart: Dp = LedgerCardCornerRadius,
    val topEnd: Dp = LedgerCardCornerRadius,
    val bottomStart: Dp = LedgerCardCornerRadius,
    val bottomEnd: Dp = LedgerCardCornerRadius,
)

private val LedgerCardCornersConverter = TwoWayConverter<LedgerCardCorners, AnimationVector4D>(
    convertToVector = {
        AnimationVector4D(
            it.topStart.value,
            it.topEnd.value,
            it.bottomStart.value,
            it.bottomEnd.value,
        )
    },
    convertFromVector = {
        LedgerCardCorners(
            topStart = it.v1.dp,
            topEnd = it.v2.dp,
            bottomStart = it.v3.dp,
            bottomEnd = it.v4.dp,
        )
    },
)

val FlatCardCorners = LedgerCardCorners(0.dp, 0.dp, 0.dp, 0.dp)
val TopRoundedCardCorners = LedgerCardCorners(
    topStart = LedgerCardCornerRadius,
    topEnd = LedgerCardCornerRadius,
    bottomStart = 0.dp,
    bottomEnd = 0.dp,
)
val BottomRoundedCardCorners = LedgerCardCorners(
    topStart = 0.dp,
    topEnd = 0.dp,
    bottomStart = LedgerCardCornerRadius,
    bottomEnd = LedgerCardCornerRadius,
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LedgerItemCard(
    item: LedgerItem,
    modifier: Modifier = Modifier,
    selectionMode: Boolean = false,
    selected: Boolean = false,
    showDate: Boolean = false,
    corners: LedgerCardCorners = LedgerCardCorners(),
    onToggleSelection: () -> Unit = {},
    onSwipeSelect: (() -> Unit)? = null,
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
) {
    val animatedCorners by animateValueAsState(
        targetValue = corners,
        typeConverter = LedgerCardCornersConverter,
        animationSpec = tween(durationMillis = CornerAnimationMillis, easing = FastOutSlowInEasing),
        label = "cardCorners",
    )
    val shape = RoundedCornerShape(
        topStart = animatedCorners.topStart,
        topEnd = animatedCorners.topEnd,
        bottomEnd = animatedCorners.bottomEnd,
        bottomStart = animatedCorners.bottomStart,
    )
    val density = LocalDensity.current
    val swipeThreshold = with(density) { SwipeSelectThreshold.toPx() }
    val swipeOffset = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val swipeSelect = onSwipeSelect
    val editAction = onEdit
    val deleteAction = onDelete
    val containerColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.tertiaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerLowest
        },
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (swipeSelect == null) {
                    Modifier
                } else {
                    Modifier
                        .graphicsLayer { translationX = swipeOffset.value }
                        .pointerInput(item.id) {
                            detectHorizontalDragGestures(
                                onHorizontalDrag = { change, dragAmount ->
                                    change.consume()
                                    scope.launch {
                                        swipeOffset.snapTo(
                                            (swipeOffset.value + dragAmount)
                                                .coerceIn(-swipeThreshold, 0f),
                                        )
                                    }
                                },
                                onDragEnd = {
                                    if (swipeOffset.value <= -swipeThreshold * SwipeSelectTriggerFraction) {
                                        swipeSelect()
                                    }
                                    scope.launch {
                                        swipeOffset.animateTo(0f, tween(durationMillis = 150))
                                    }
                                },
                                onDragCancel = {
                                    scope.launch {
                                        swipeOffset.animateTo(0f, tween(durationMillis = 150))
                                    }
                                },
                            )
                        }
                },
            )
            .clip(shape)
            .combinedClickable(
                onClick = { if (selectionMode) onToggleSelection() },
                onLongClick = onToggleSelection,
            ),
        shape = shape,
        color = containerColor,
    ) {
        Column(
            modifier = Modifier.padding(start = 16.dp, top = 12.dp, end = 8.dp, bottom = 16.dp),
        ) {
            val rowContent: @Composable (Modifier) -> Unit = { tagModifier ->
                Row(
                    modifier = Modifier.heightIn(min = 48.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AnimatedVisibility(
                        visible = selectionMode,
                        enter = expandHorizontally(),
                        exit = shrinkHorizontally(),
                    ) {
                        Checkbox(
                            checked = selected,
                            onCheckedChange = null,
                            modifier = Modifier.padding(end = 8.dp),
                        )
                    }
                    TimeTag(
                        text = LedgerFormat.timeText(item.happenAt.toLocalTime()),
                        dateText = if (showDate) {
                            val date = item.happenAt.toLocalDate()
                            if (date == LocalDate.now()) {
                                stringResource(R.string.date_today)
                            } else {
                                LedgerFormat.dateText(date)
                            }
                        } else {
                            null
                        },
                    )
                    Spacer(Modifier.weight(1f))
                    if (item.tag != LedgerTag.None) {
                        LedgerCardTag(
                            tag = item.tag,
                            tagText = item.tagText,
                            modifier = tagModifier,
                        )
                        Spacer(Modifier.width(4.dp))
                    }
                    if (!selectionMode && editAction != null && deleteAction != null) {
                        ItemMenuButton(onEdit = editAction, onDelete = deleteAction)
                    }
                }
            }
            if (selectionMode) {
                LookaheadScope {
                    rowContent(Modifier.animateBounds(this@LookaheadScope))
                }
            } else {
                rowContent(Modifier)
            }
            Column(modifier = Modifier.padding(end = 8.dp)) {
                Text(
                    text = LedgerFormat.amountWithSymbol(item.amountValue),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleLarge,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = item.itemName,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (item.itemNote.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = item.itemNote,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun TimeTag(text: String, dateText: String?) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = MaterialTheme.shapes.extraSmall,
        modifier = Modifier.animateContentSize(),
    ) {
        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
            AnimatedVisibility(
                visible = dateText != null,
                enter = expandHorizontally(expandFrom = Alignment.Start) + fadeIn(),
                exit = shrinkHorizontally(shrinkTowards = Alignment.Start) + fadeOut(),
                modifier = Modifier.alignByBaseline(),
            ) {
                Text(
                    text = "${dateText.orEmpty()} ",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    maxLines = 1,
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                maxLines = 1,
                modifier = Modifier.alignByBaseline(),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ItemMenuButton(onEdit: () -> Unit, onDelete: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val density = LocalDensity.current
    val positionProvider = remember(density) {
        MenuEndAlignedPositionProvider(
            endInsetPx = with(density) { MenuIconInset.toPx().roundToInt() },
        )
    }

    Box {
        IconButton(
            onClick = { expanded = true },
            shape = MaterialTheme.shapes.small,
        ) {
            Icon(
                imageVector = Icons.Filled.MoreVert,
                contentDescription = stringResource(R.string.open_edit_menu),
            )
        }
        DropdownMenuPopup(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.widthIn(min = MenuItemMinWidth),
            popupPositionProvider = positionProvider,
        ) {
            DropdownMenuGroup(
                shapes = MenuDefaults.groupShape(index = 0, count = 1),
                containerColor = MenuDefaults.groupStandardContainerColor,
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.edit_ledger_entry)) },
                    onClick = {
                        expanded = false
                        onEdit()
                    },
                    shape = MenuDefaults.itemShape(index = 0, count = 2).shape,
                    trailingContent = {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(MenuDefaults.TrailingIconSize),
                        )
                    },
                )
                DropdownMenuItem(
                    text = {
                        Text(
                            text = stringResource(R.string.delete_ledger_entry),
                            color = MaterialTheme.colorScheme.error,
                        )
                    },
                    onClick = {
                        expanded = false
                        onDelete()
                    },
                    shape = MenuDefaults.itemShape(index = 1, count = 2).shape,
                    trailingContent = {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(MenuDefaults.TrailingIconSize),
                        )
                    },
                )
            }
        }
    }
}

private class MenuEndAlignedPositionProvider(
    private val endInsetPx: Int,
) : DropdownMenuPopupPositionProvider {

    override val transformOrigin: TransformOrigin = TransformOrigin(1f, 0f)

    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset {
        val end = when (layoutDirection) {
            LayoutDirection.Ltr -> anchorBounds.right - endInsetPx
            LayoutDirection.Rtl -> anchorBounds.left + endInsetPx
        }
        val x = when (layoutDirection) {
            LayoutDirection.Ltr -> end - popupContentSize.width
            LayoutDirection.Rtl -> end
        }
        return IntOffset(
            x = x.coerceIn(0, (windowSize.width - popupContentSize.width).coerceAtLeast(0)),
            y = anchorBounds.bottom,
        )
    }
}
