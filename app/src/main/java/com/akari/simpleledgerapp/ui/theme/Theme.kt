package com.akari.simpleledgerapp.ui.theme

import android.os.Build
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

val LocalIsDarkTheme = compositionLocalOf { false }

private const val ThemeFadeMillis = 320

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SimpleLedgerAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    ThemeFadeOverlay(colorScheme = colorScheme) {
        CompositionLocalProvider(LocalIsDarkTheme provides darkTheme) {
            MaterialExpressiveTheme(
                colorScheme = colorScheme,
                typography = Typography,
                content = content
            )
        }
    }
}

@Composable
private fun ThemeFadeOverlay(colorScheme: ColorScheme, content: @Composable () -> Unit) {
    val fade = remember { Animatable(0f) }
    var overlayColor by remember { mutableStateOf(colorScheme.surface) }
    var lastSurface by remember { mutableStateOf(colorScheme.surface) }

    LaunchedEffect(colorScheme.surface) {
        if (colorScheme.surface != lastSurface) {
            overlayColor = lastSurface
            lastSurface = colorScheme.surface
            fade.snapTo(1f)
            fade.animateTo(0f, animationSpec = tween(durationMillis = ThemeFadeMillis))
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        content()
        if (fade.value > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = fade.value }
                    .background(overlayColor),
            )
        }
    }
}
