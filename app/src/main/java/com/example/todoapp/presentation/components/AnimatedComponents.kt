package com.example.todoapp.presentation.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

/**
 * Animated visibility wrapper with slide and fade animations
 */
@Composable
fun AnimatedSlideInOut(
    visible: Boolean,
    modifier: Modifier = Modifier,
    enter: EnterTransition = slideInVertically(
        initialOffsetY = { -it },
        animationSpec = tween(300, easing = EaseOutCubic)
    ) + fadeIn(animationSpec = tween(300)),
    exit: ExitTransition = slideOutVertically(
        targetOffsetY = { -it },
        animationSpec = tween(300, easing = EaseInCubic)
    ) + fadeOut(animationSpec = tween(300)),
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = enter,
        exit = exit,
        content = content
    )
}

/**
 * Bouncy scale animation for button presses
 */
@Composable
fun BouncyPressAnimation(
    pressed: Boolean,
    modifier: Modifier = Modifier,
    scaleDown: Float = 0.95f,
    content: @Composable () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (pressed) scaleDown else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "bouncy_scale"
    )
    
    Box(
        modifier = modifier.scale(scale)
    ) {
        content()
    }
}

/**
 * Animated counter with number rolling effect
 */
@Composable
fun AnimatedCounter(
    count: Int,
    modifier: Modifier = Modifier,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.headlineMedium
) {
    var oldCount by remember { mutableIntStateOf(count) }
    val animatedCount by animateIntAsState(
        targetValue = count,
        animationSpec = tween(
            durationMillis = 500,
            easing = EaseOutCubic
        ),
        label = "counter_animation"
    )
    
    LaunchedEffect(count) {
        oldCount = count
    }
    
    Text(
        text = animatedCount.toString(),
        style = style,
        modifier = modifier
    )
}

/**
 * Animated progress bar with smooth transitions
 */
@Composable
fun AnimatedProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary,
    trackColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.surfaceVariant,
    strokeWidth: androidx.compose.ui.unit.Dp = 8.dp
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(
            durationMillis = 800,
            easing = EaseOutCubic
        ),
        label = "progress_animation"
    )
    
    LinearProgressIndicator(
        progress = { animatedProgress },
        modifier = modifier,
        color = color,
        trackColor = trackColor,
        strokeCap = androidx.compose.ui.graphics.StrokeCap.Round,
    )
}

/**
 * Staggered animation for list items
 */
@Composable
fun StaggeredAnimation(
    visible: Boolean,
    index: Int,
    modifier: Modifier = Modifier,
    delayMillis: Int = 50,
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(
                durationMillis = 300,
                delayMillis = index * delayMillis,
                easing = EaseOutCubic
            )
        ) + fadeIn(
            animationSpec = tween(
                durationMillis = 300,
                delayMillis = index * delayMillis
            )
        ),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(
                durationMillis = 200,
                easing = EaseInCubic
            )
        ) + fadeOut(
            animationSpec = tween(durationMillis = 200)
        ),
        content = content
    )
}

/**
 * Shake animation for error states
 */
@Composable
fun ShakeAnimation(
    trigger: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    val shakeOffset = remember { Animatable(0f) }
    
    LaunchedEffect(trigger) {
        if (trigger) {
            val shakeDistance = with(density) { 8.dp.toPx() }
            repeat(3) {
                shakeOffset.animateTo(
                    targetValue = shakeDistance,
                    animationSpec = tween(50)
                )
                shakeOffset.animateTo(
                    targetValue = -shakeDistance,
                    animationSpec = tween(50)
                )
            }
            shakeOffset.animateTo(
                targetValue = 0f,
                animationSpec = tween(50)
            )
        }
    }
    
    Box(
        modifier = modifier.graphicsLayer {
            translationX = shakeOffset.value
        }
    ) {
        content()
    }
}

/**
 * Pulsing animation for attention-grabbing elements
 */
@Composable
fun PulseAnimation(
    enabled: Boolean,
    modifier: Modifier = Modifier,
    minScale: Float = 0.95f,
    maxScale: Float = 1.05f,
    durationMillis: Int = 1000,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = minScale,
        targetValue = maxScale,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    
    Box(
        modifier = modifier.scale(if (enabled) scale else 1f)
    ) {
        content()
    }
}

/**
 * Slide up animation for bottom sheets and dialogs
 */
@Composable
fun SlideUpAnimation(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(
                durationMillis = 300,
                easing = EaseOutCubic
            )
        ) + fadeIn(
            animationSpec = tween(300)
        ),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(
                durationMillis = 250,
                easing = EaseInCubic
            )
        ) + fadeOut(
            animationSpec = tween(250)
        ),
        content = content
    )
}

/**
 * Cross-fade animation for content switching
 */
@Composable
fun CrossFadeAnimation(
    targetState: Any,
    modifier: Modifier = Modifier,
    animationSpec: FiniteAnimationSpec<Float> = tween(300),
    content: @Composable (Any) -> Unit
) {
    Crossfade(
        targetState = targetState,
        modifier = modifier,
        animationSpec = animationSpec,
        label = "crossfade"
    ) { state ->
        content(state)
    }
}