package com.akari.simpleledgerapp.ui.pages

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.akari.simpleledgerapp.R
import com.akari.simpleledgerapp.background.AppSettings
import com.akari.simpleledgerapp.background.HomeStyle
import com.akari.simpleledgerapp.background.LanguageMode
import com.akari.simpleledgerapp.background.ThemeMode

@Composable
fun SettingsScreen(
    settings: AppSettings,
    onSettingsChange: (AppSettings) -> Unit,
    onLanguageChange: (LanguageMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val appIcon = remember(context) {
        runCatching {
            val drawable = context.packageManager.getApplicationIcon(context.packageName)
            val size = (AppIconSize.value * context.resources.displayMetrics.density).toInt()
            drawable.setBounds(0, 0, size, size)
            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            drawable.draw(Canvas(bitmap))
            bitmap.asImageBitmap()
        }.getOrNull()
    }
    val versionName = remember(context) {
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        }.getOrNull().orEmpty()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        Spacer(Modifier.height(32.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = SettingsHorizontalPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (appIcon != null) {
                Image(
                    bitmap = appIcon,
                    contentDescription = null,
                    modifier = Modifier.size(AppIconSize),
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = if (versionName.isNullOrEmpty()) {
                    stringResource(R.string.app_version_unknown)
                } else {
                    stringResource(R.string.app_version, versionName)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }

        Spacer(Modifier.height(24.dp))
        SettingsSectionHeader(stringResource(R.string.settings_section_appearance))
        SettingsOptionRow(
            title = stringResource(R.string.settings_theme),
            value = stringResource(themeLabelRes(settings.themeMode)),
            options = ThemeMode.entries.map { it to themeLabelRes(it) },
            selected = settings.themeMode,
            onSelect = { onSettingsChange(settings.copy(themeMode = it)) },
        )
        SettingsOptionRow(
            title = stringResource(R.string.settings_section_home_style),
            value = stringResource(homeStyleLabelRes(settings.homeStyle)),
            options = HomeStyle.entries.map { it to homeStyleLabelRes(it) },
            selected = settings.homeStyle,
            onSelect = { onSettingsChange(settings.copy(homeStyle = it)) },
        )

        Spacer(Modifier.height(16.dp))
        SettingsSectionHeader(stringResource(R.string.settings_section_language))
        SettingsOptionRow(
            title = stringResource(R.string.settings_section_language),
            value = stringResource(languageLabelRes(settings.language)),
            options = LanguageMode.entries.map { it to languageLabelRes(it) },
            selected = settings.language,
            onSelect = onLanguageChange,
        )

        Spacer(Modifier.height(16.dp))
        SettingsSectionHeader(stringResource(R.string.settings_section_other))
        SettingsTextRow(
            title = stringResource(R.string.settings_project_url),
            supportingText = stringResource(R.string.settings_view_on_github),
            onClick = { uriHandler.openUri(ProjectUrl) },
        )
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun SettingsTextRow(
    title: String,
    supportingText: String,
    onClick: (() -> Unit)?,
) {
    val rowModifier = Modifier
        .fillMaxWidth()
        .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
        .padding(
            start = SettingsHorizontalPadding,
            end = SettingsHorizontalPadding,
            top = 12.dp,
            bottom = 12.dp,
        )
    Column(modifier = rowModifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = supportingText,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(
            start = SettingsHorizontalPadding,
            end = SettingsHorizontalPadding,
            top = 8.dp,
            bottom = 8.dp,
        ),
    )
}

@Composable
private fun <T> SettingsOptionRow(
    title: String,
    value: String,
    options: List<Pair<T, Int>>,
    selected: T,
    onSelect: (T) -> Unit,
) {
    var showDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showDialog = true }
            .padding(
                start = SettingsHorizontalPadding,
                end = SettingsHorizontalPadding,
                top = 12.dp,
                bottom = 12.dp,
            ),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(title) },
            text = {
                Column(modifier = Modifier.selectableGroup()) {
                    options.forEach { (option, labelRes) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = option == selected,
                                    role = Role.RadioButton,
                                    onClick = {
                                        showDialog = false
                                        onSelect(option)
                                    },
                                )
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(
                                selected = option == selected,
                                onClick = null,
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = stringResource(labelRes),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(stringResource(R.string.dialog_cancel))
                }
            },
        )
    }
}

private fun themeLabelRes(mode: ThemeMode): Int = when (mode) {
    ThemeMode.System -> R.string.theme_follow_system
    ThemeMode.AlwaysDark -> R.string.theme_always_on
    ThemeMode.AlwaysLight -> R.string.theme_always_off
}

private fun homeStyleLabelRes(style: HomeStyle): Int = when (style) {
    HomeStyle.List -> R.string.home_style_list
    HomeStyle.Calendar -> R.string.home_style_calendar
}

private fun languageLabelRes(language: LanguageMode): Int = when (language) {
    LanguageMode.System -> R.string.language_follow_system
    LanguageMode.SimplifiedChinese -> R.string.language_simplified_chinese
}

private const val ProjectUrl = "https://github.com/Northern-Lighting/Simple-Ledger/tree/main"
private val AppIconSize = 88.dp
private val SettingsHorizontalPadding = 24.dp


