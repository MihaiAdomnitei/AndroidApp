package com.example.myapp.core.ui.animations

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

// Animated card that scales and fades in when appearing
@Composable
fun AnimatedItemCard(
    modifier: Modifier = Modifier,
    index: Int,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(
            animationSpec = tween(
                durationMillis = 300,
                delayMillis = index * 50
            )
        ) + slideInVertically(
            animationSpec = tween(
                durationMillis = 300,
                delayMillis = index * 50
            ),
            initialOffsetY = { it / 2 }
        ) + scaleIn(
            animationSpec = tween(
                durationMillis = 300,
                delayMillis = index * 50
            ),
            initialScale = 0.8f
        ),
        exit = fadeOut() + slideOutVertically() + scaleOut()
    ) {
        content()
    }
}

// Pulsating animation for buttons
@Composable
fun PulsingButton(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Box(
        modifier = modifier.scale(scale)
    ) {
        content()
    }
}

// Shake animation for error states
@Composable
fun ShakeAnimation(
    trigger: Boolean,
    content: @Composable () -> Unit
) {
    val shakeOffset by animateFloatAsState(
        targetValue = if (trigger) 10f else 0f,
        animationSpec = if (trigger) {
            repeatable(
                iterations = 5,
                animation = tween(50, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            )
        } else {
            tween(0)
        },
        label = "shake"
    )

    Box(
        modifier = Modifier.graphicsLayer { translationX = shakeOffset }
    ) {
        content()
    }
}

// Rotating refresh indicator
@Composable
fun RotatingRefreshIndicator(
    isRefreshing: Boolean,
    modifier: Modifier = Modifier
) {
    val rotation by animateFloatAsState(
        targetValue = if (isRefreshing) 360f else 0f,
        animationSpec = if (isRefreshing) {
            infiniteRepeatable(
                animation = tween(1000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        } else {
            tween(0)
        },
        label = "rotation"
    )

    Box(
        modifier = modifier.graphicsLayer { rotationZ = rotation }
    )
}

// Animated counter for prices
@Composable
fun AnimatedCounter(
    count: Int,
    modifier: Modifier = Modifier
) {
    var oldCount by remember { mutableStateOf(count) }

    SideEffect {
        oldCount = count
    }

    Row(modifier = modifier) {
        val countString = count.toString()
        val oldCountString = oldCount.toString()

        for (i in countString.indices) {
            val oldChar = oldCountString.getOrNull(i)
            val newChar = countString[i]
            val char = if (oldChar == newChar) oldChar else newChar

            AnimatedContent(
                targetState = char,
                transitionSpec = {
                    slideInVertically { it } + fadeIn() togetherWith
                            slideOutVertically { -it } + fadeOut()
                },
                label = "counter"
            ) { c ->
                Text(
                    text = c.toString(),
                    style = MaterialTheme.typography.headlineMedium,
                    softWrap = false
                )
            }
        }
    }
}

// Bounce animation for FAB
@Composable
fun BouncingFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "bounce"
    )

    Box(
        modifier = modifier
            .scale(scale)
    ) {
        content()
    }
}

// Shimmer loading effect
@Composable
fun ShimmerLoadingItem(
    modifier: Modifier = Modifier
) {
    val shimmerColors = listOf(
        Color.LightGray.copy(alpha = 0.6f),
        Color.LightGray.copy(alpha = 0.2f),
        Color.LightGray.copy(alpha = 0.6f)
    )

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp)
            .padding(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.LightGray.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(20.dp)
                    .background(
                        Color.LightGray.copy(alpha = 0.5f),
                        RoundedCornerShape(4.dp)
                    )
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.4f)
                    .height(16.dp)
                    .background(
                        Color.LightGray.copy(alpha = 0.5f),
                        RoundedCornerShape(4.dp)
                    )
            )
        }
    }
}

