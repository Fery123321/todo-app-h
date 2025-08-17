package com.example.todoapp.util

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

/**
 * Animation specifications for consistent micro-interactions
 */
object AnimationSpecs {
    val fastSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessHigh
    )
    
    val mediumSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessMedium
    )
    
    val slowSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessLow
    )
    
    val quickFade = tween<Float>(durationMillis = 150)
    val mediumFade = tween<Float>(durationMillis = 300)
    val slowFade = tween<Float>(durationMillis = 500)
}

/**
 * Modifier that adds a bouncy press animation to clickable elements
 */
@Composable
fun Modifier.bouncyClickable(
    enabled: Boolean = true,
    scaleDown: Float = 0.95f,
    animationSpec: AnimationSpec<Float> = AnimationSpecs.fastSpring,
    onClick: () -> Unit
): Modifier {
    val scale = remember { Animatable(1f) }
    val interactionSource = remember { MutableInteractionSource() }
    
    return this
        .scale(scale.value)
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            enabled = enabled
        ) {
            onClick()
        }
        .graphicsLayer {
            scaleX = scale.value
            scaleY = scale.value
        }
}

/**
 * Modifier that adds a subtle hover/press effect
 */
@Composable
fun Modifier.pressEffect(
    scaleDown: Float = 0.98f,
    animationSpec: AnimationSpec<Float> = AnimationSpecs.quickFade
): Modifier {
    val scale = remember { Animatable(1f) }
    
    return this.scale(scale.value)
}

/**
 * Composable that provides a pulsing animation effect
 */
@Composable
fun PulseEffect(
    enabled: Boolean = true,
    minScale: Float = 0.9f,
    maxScale: Float = 1.1f,
    duration: Int = 1000,
    content: @Composable () -> Unit
) {
    val scale = remember { Animatable(1f) }
    
    LaunchedEffect(enabled) {
        if (enabled) {
            while (true) {
                scale.animateTo(maxScale, tween(duration / 2))
                scale.animateTo(minScale, tween(duration / 2))
            }
        } else {
            scale.animateTo(1f, AnimationSpecs.mediumSpring)
        }
    }
    
    Box(modifier = Modifier.scale(scale.value)) {
        content()
    }
}

/**
 * Composable that provides a shake animation effect
 */
@Composable
fun ShakeEffect(
    enabled: Boolean = false,
    strength: Float = 10f,
    content: @Composable () -> Unit
) {
    val offsetX = remember { Animatable(0f) }
    
    LaunchedEffect(enabled) {
        if (enabled) {
            repeat(3) {
                offsetX.animateTo(strength, tween(50))
                offsetX.animateTo(-strength, tween(50))
            }
            offsetX.animateTo(0f, tween(50))
        }
    }
    
    Box(
        modifier = Modifier.graphicsLayer {
            translationX = offsetX.value
        }
    ) {
        content()
    }
}