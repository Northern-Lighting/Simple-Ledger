package com.akari.simpleledgerapp.ui.components

import android.os.Build
import android.view.WindowManager
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Label
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowCompat
import com.akari.simpleledgerapp.R
import com.akari.simpleledgerapp.background.LedgerFormat
import com.akari.simpleledgerapp.background.LedgerItem
import com.akari.simpleledgerapp.background.LedgerTag
import com.akari.simpleledgerapp.background.LedgerValidate
import java.time.LocalDate
import java.time.LocalTime
import kotlin.math.abs

private val ChipBorderWidth = 1.dp
private val SectionIconSize = 20.dp
private val AMOUNT_INPUT = Regex("^[0-9]*\\.?[0-9]{0,2}$")
private const val MillisPerDay = 86_400_000L

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LedgerEntryDialog(
    initialItem: LedgerItem?,
    onDismiss: () -> Unit,
    onConfirm: (LedgerItem) -> Unit,
    defaultDate: LocalDate = LocalDate.now(),
) {
    var itemName by remember { mutableStateOf(initialItem?.itemName.orEmpty()) }
    var itemNote by remember { mutableStateOf(initialItem?.itemNote.orEmpty()) }
    var amountText by remember {
        mutableStateOf(initialItem?.let { LedgerFormat.amountFixed(abs(it.amountValue)) }.orEmpty())
    }
    var isIncome by remember { mutableStateOf((initialItem?.amountValue ?: 0L) > 0) }
    var hour by remember { mutableStateOf(initialItem?.happenAt?.hour ?: LocalTime.now().hour) }
    var minute by remember { mutableStateOf(initialItem?.happenAt?.minute ?: LocalTime.now().minute) }
    var nameError by remember { mutableStateOf<String?>(null) }
    var amountError by remember { mutableStateOf<String?>(null) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDate by remember {
        mutableStateOf(initialItem?.happenAt?.toLocalDate() ?: defaultDate)
    }
    var tag by remember { mutableStateOf(initialItem?.tag ?: LedgerTag.None) }
    var tagText by remember { mutableStateOf(initialItem?.tagText.orEmpty()) }
    var showTagEditor by remember { mutableStateOf(false) }
    var noteDraft by remember { mutableStateOf("") }
    var showNoteEditor by remember { mutableStateOf(false) }

    val nameRequiredMessage = stringResource(R.string.item_name_required)
    val amountInvalidMessage = stringResource(R.string.item_amount_invalid)
    val amountTooLargeMessage = stringResource(R.string.item_amount_too_large)
    val saveMessage = stringResource(R.string.dialog_save)
    val cancelMessage = stringResource(R.string.dialog_cancel)
    val confirmMessage = stringResource(R.string.dialog_confirm)

    val currentAmountError = amountError

    val previewAmount = LedgerFormat.parseAmount(amountText)?.let { amount ->
        (if (isIncome) 1 else -1) * abs(amount)
    } ?: 0L
    val previewItem = LedgerItem(
        id = initialItem?.id ?: 0L,
        itemName = itemName.trim(),
        itemNote = itemNote.trim(),
        amountValue = previewAmount,
        happenAt = selectedDate.atTime(hour, minute),
        tag = tag,
        tagText = tagText.trim(),
    )

    fun submit() {
        val parsed = LedgerFormat.parseAmount(amountText)
        val nameValid = LedgerValidate.isValidItemName(itemName)
        nameError = if (nameValid) null else nameRequiredMessage
        amountError = when {
            parsed == null -> amountInvalidMessage
            !LedgerValidate.isValidAmountValue(parsed) -> amountTooLargeMessage
            else -> null
        }
        if (!nameValid || parsed == null || amountError != null) {
            return
        }
        val date = selectedDate
        onConfirm(
            LedgerItem(
                id = initialItem?.id ?: 0L,
                itemName = itemName.trim(),
                itemNote = itemNote.trim(),
                amountValue = (if (isIncome) 1 else -1) * abs(parsed),
                happenAt = date.atTime(hour, minute),
                tag = tag,
                tagText = tagText.trim(),
            ),
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
        ),
    ) {
        val view = LocalView.current
        DisposableEffect(Unit) {
            val window = (view.parent as? DialogWindowProvider)?.window
            if (window != null) {
                window.setDimAmount(0f)
                window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
                WindowCompat.setDecorFitsSystemWindows(window, false)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    window.isNavigationBarContrastEnforced = false
                    window.isStatusBarContrastEnforced = false
                }
                @Suppress("DEPRECATION")
                window.navigationBarColor = android.graphics.Color.TRANSPARENT
                @Suppress("DEPRECATION")
                window.statusBarColor = android.graphics.Color.TRANSPARENT
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = true
            }
            onDispose { }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceContainerLow),
        ) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    ),
                    title = {
                        Text(
                            stringResource(
                                if (initialItem == null) {
                                    R.string.add_ledger_dialog_title
                                } else {
                                    R.string.edit_ledger_entry
                                },
                            ),
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = cancelMessage,
                            )
                        }
                    },
                    actions = {
                        TextButton(onClick = { submit() }) {
                            Text(saveMessage)
                        }
                    },
                )
            },
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                                    LedgerItemCard(
                                        item = previewItem,
                                        modifier = Modifier.fillMaxWidth(),
                                    )
                                    HorizontalDivider()
                                    val labelInset =
                                        OutlinedTextFieldDefaults.contentPaddingWithLabel().calculateTopPadding()

                                    SectionHeader(
                                        icon = Icons.AutoMirrored.Outlined.Label,
                                        label = stringResource(R.string.item_tag_label),
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        LedgerTagFilterChip(
                                            tag = LedgerTag.WeChat,
                                            text = stringResource(R.string.tag_wechat),
                                            selected = tag == LedgerTag.WeChat,
                                            onClick = {
                                                tag = if (tag == LedgerTag.WeChat) {
                                                    LedgerTag.None
                                                } else {
                                                    LedgerTag.WeChat
                                                }
                                            },
                                            modifier = Modifier.weight(1f),
                                        )
                                        LedgerTagFilterChip(
                                            tag = LedgerTag.Alipay,
                                            text = stringResource(R.string.tag_alipay),
                                            selected = tag == LedgerTag.Alipay,
                                            onClick = {
                                                tag = if (tag == LedgerTag.Alipay) {
                                                    LedgerTag.None
                                                } else {
                                                    LedgerTag.Alipay
                                                }
                                            },
                                            modifier = Modifier.weight(1f),
                                        )
                                        LedgerTagFilterChip(
                                            tag = LedgerTag.Other,
                                            text = stringResource(R.string.tag_other),
                                            selected = tag == LedgerTag.Other,
                                            onClick = { showTagEditor = true },
                                            modifier = Modifier.weight(1f),
                                        )
                                    }
                                    HorizontalDivider()
                                    SectionHeader(
                                        icon = Icons.Outlined.Info,
                                        label = stringResource(R.string.section_info),
                                    )
                        OutlinedTextField(
                            value = itemName,
                            onValueChange = { if (it.length <= LedgerValidate.MAX_ITEM_NAME_LENGTH) itemName = it },
                            label = { Text(stringResource(R.string.item_name_label)) },
                            isError = nameError != null,
                            singleLine = true,
                            supportingText = {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text(nameError.orEmpty())
                                    Text("${itemName.length}/${LedgerValidate.MAX_ITEM_NAME_LENGTH}")
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.Top,
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                OutlinedTextField(
                                    value = LedgerFormat.shortDateText(selectedDate),
                                    onValueChange = {},
                                    readOnly = true,
                                    singleLine = true,
                                    label = { Text(stringResource(R.string.item_date_label)) },
                                    trailingIcon = {
                                        Icon(
                                            imageVector = Icons.Filled.ArrowDropDown,
                                            contentDescription = null,
                                        )
                                    },
                                    supportingText = { Text(" ") },
                                    modifier = Modifier.fillMaxWidth(),
                                )
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null,
                                            onClick = { showDatePicker = true },
                                        ),
                                )
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                OutlinedTextField(
                                    value = LedgerFormat.timeText(LocalTime.of(hour, minute)),
                                    onValueChange = {},
                                    readOnly = true,
                                    singleLine = true,
                                    label = { Text(stringResource(R.string.item_time_label)) },
                                    trailingIcon = {
                                        Icon(
                                            imageVector = Icons.Filled.ArrowDropDown,
                                            contentDescription = null,
                                        )
                                    },
                                    supportingText = { Text(" ") },
                                    modifier = Modifier.fillMaxWidth(),
                                )
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null,
                                            onClick = { showTimePicker = true },
                                        ),
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top,
                        ) {
                            FilledTonalButton(
                                onClick = { isIncome = !isIncome },
                                modifier = Modifier
                                    .padding(top = labelInset)
                                    .size(56.dp),
                                contentPadding = PaddingValues(0.dp),
                            ) {
                                Text(
                                    text = if (isIncome) "+" else "-",
                                    style = MaterialTheme.typography.titleMedium,
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            OutlinedTextField(
                                value = amountText,
                                onValueChange = { if (AMOUNT_INPUT.matches(it)) amountText = it },
                                label = { Text(stringResource(R.string.item_amount_label)) },
                                isError = amountError != null,
                                supportingText = { Text(currentAmountError ?: " ") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                            )
                        }
                        HorizontalDivider()
                        SectionHeader(
                            icon = Icons.Outlined.EditNote,
                            label = stringResource(R.string.item_note_label),
                        )
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = itemNote,
                                onValueChange = {},
                                readOnly = true,
                                singleLine = true,
                                label = { Text(stringResource(R.string.item_note_label)) },
                                modifier = Modifier.fillMaxWidth(),
                            )
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                        onClick = {
                                            noteDraft = itemNote
                                            showNoteEditor = true
                                        },
                                    ),
                            )
                        }
            }
        }
    }
    }

    if (showNoteEditor) {
        AlertDialog(
            onDismissRequest = { showNoteEditor = false },
            title = { Text(stringResource(R.string.item_note_label)) },
            text = {
                OutlinedTextField(
                    value = noteDraft,
                    onValueChange = {
                        if (it.length <= LedgerValidate.MAX_ITEM_NOTE_LENGTH) noteDraft = it
                    },
                    minLines = 3,
                    supportingText = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                        ) {
                            Text("${noteDraft.length}/${LedgerValidate.MAX_ITEM_NOTE_LENGTH}")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        itemNote = noteDraft
                        showNoteEditor = false
                    },
                ) {
                    Text(confirmMessage)
                }
            },
            dismissButton = {
                TextButton(onClick = { showNoteEditor = false }) {
                    Text(cancelMessage)
                }
            },
        )
    }

    if (showTagEditor) {
        AlertDialog(
            onDismissRequest = { showTagEditor = false },
            title = { Text(stringResource(R.string.tag_other)) },
            text = {
                TextField(
                    value = tagText,
                    onValueChange = {
                        if (it.length <= LedgerValidate.MAX_TAG_TEXT_LENGTH) tagText = it
                    },
                    singleLine = true,
                    supportingText = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                        ) {
                            Text("${tagText.length}/${LedgerValidate.MAX_TAG_TEXT_LENGTH}")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        tag = LedgerTag.Other
                        showTagEditor = false
                    },
                ) {
                    Text(confirmMessage)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTagEditor = false }) {
                    Text(cancelMessage)
                }
            },
        )
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.toEpochDay() * MillisPerDay,
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            selectedDate = LocalDate.ofEpochDay(it / MillisPerDay)
                        }
                        showDatePicker = false
                    },
                ) {
                    Text(confirmMessage)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(cancelMessage)
                }
            },
        ) {
            DatePicker(
                state = datePickerState,
                showModeToggle = false,
            )
        }
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = hour,
            initialMinute = minute,
            is24Hour = true,
        )
        BasicAlertDialog(onDismissRequest = { showTimePicker = false }) {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                tonalElevation = 6.dp,
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = stringResource(R.string.select_time),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(16.dp))
                    TimePicker(state = timePickerState)
                    Row(modifier = Modifier.align(Alignment.End)) {
                        TextButton(onClick = { showTimePicker = false }) {
                            Text(cancelMessage)
                        }
                        TextButton(
                            onClick = {
                                hour = timePickerState.hour
                                minute = timePickerState.minute
                                showTimePicker = false
                            },
                        ) {
                            Text(confirmMessage)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LedgerTagFilterChip(
    tag: LedgerTag,
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = ledgerTagColors(tag)
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = text,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
        },
        leadingIcon = if (selected) {
            {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    modifier = Modifier.size(FilterChipDefaults.IconSize),
                )
            }
        } else {
            null
        },
        modifier = modifier,
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderWidth = ChipBorderWidth,
            selectedBorderWidth = ChipBorderWidth,
        ),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = Color.Transparent,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            selectedContainerColor = colors.container,
            selectedLabelColor = colors.content,
            selectedLeadingIconColor = colors.content,
        ),
    )
}

@Composable
private fun SectionHeader(icon: ImageVector, label: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(SectionIconSize),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
