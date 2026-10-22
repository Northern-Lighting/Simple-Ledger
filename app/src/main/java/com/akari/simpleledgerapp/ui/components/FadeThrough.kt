package com.akari.simpleledgerapp.ui.components

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith

private const val FadeOutDuration = 60
private const val FadeInDuration = 90
private const val ScaleStart = 0.92f

internal fun fadeThrough(): ContentTransform =
    (
        fadeIn(
            animationSpec = tween(
                durationMillis = FadeInDuration,
                delayMillis = FadeOutDuration,
            ),
        ) + scaleIn(
            initialScale = ScaleStart,
            animationSpec = tween(
                durationMillis = FadeInDuration,
                delayMillis = FadeOutDuration,
            ),
        )
    ).togetherWith(
        fadeOut(animationSpec = tween(durationMillis = FadeOutDuration)),
    )
