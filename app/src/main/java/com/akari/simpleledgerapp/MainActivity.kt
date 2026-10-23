package com.akari.simpleledgerapp

import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import com.akari.simpleledgerapp.background.AppSettings
import com.akari.simpleledgerapp.background.LanguageMode
import com.akari.simpleledgerapp.background.LedgerViewModel
import com.akari.simpleledgerapp.background.SettingsStore
import com.akari.simpleledgerapp.background.ThemeMode
import com.akari.simpleledgerapp.ui.navigation.MainScreen
import com.akari.simpleledgerapp.ui.theme.SimpleLedgerAppTheme
import java.util.Locale

class MainActivity : ComponentActivity() {

    private val ledgerViewModel: LedgerViewModel by viewModels()
    private val settingsStore by lazy { SettingsStore(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            navigationBarStyle = SystemBarStyle.auto(
                lightScrim = Color.TRANSPARENT,
                darkScrim = Color.TRANSPARENT,
            ),
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
        setContent {
            var settings by remember { mutableStateOf(settingsStore.load()) }
            val darkTheme = when (settings.themeMode) {
                ThemeMode.System -> isSystemInDarkTheme()
                ThemeMode.AlwaysDark -> true
                ThemeMode.AlwaysLight -> false
            }
            val baseContext = LocalContext.current
            val localeContext = remember(settings.language, baseContext) {
                when (settings.language) {
                    LanguageMode.System -> baseContext
                    LanguageMode.SimplifiedChinese -> localizedContext(baseContext, Locale.SIMPLIFIED_CHINESE)
                    LanguageMode.TraditionalChinese -> localizedContext(baseContext, Locale.TRADITIONAL_CHINESE)
                    LanguageMode.English -> localizedContext(baseContext, Locale.ENGLISH)
                }
            }
            CompositionLocalProvider(
                LocalContext provides localeContext,
                LocalConfiguration provides localeContext.resources.configuration,
            ) {
                SimpleLedgerAppTheme(darkTheme = darkTheme) {
                    MainScreen(
                        ledgerViewModel = ledgerViewModel,
                        settings = settings,
                        onSettingsChange = { updated: AppSettings ->
                            settings = updated
                            settingsStore.save(updated)
                        },
                        onLanguageChange = { language: LanguageMode ->
                            val updated = settings.copy(language = language)
                            settings = updated
                            settingsStore.save(updated)
                        },
                    )
                }
            }
        }
    }

    private fun localizedContext(base: android.content.Context, locale: Locale): android.content.Context {
        val configuration = Configuration(base.resources.configuration)
        configuration.setLocale(locale)
        return base.createConfigurationContext(configuration)
    }
}