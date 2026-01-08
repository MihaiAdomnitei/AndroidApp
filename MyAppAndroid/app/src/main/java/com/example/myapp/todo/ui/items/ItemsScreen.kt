package com.example.myapp.todo.ui.items

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapp.R
import com.example.myapp.core.ui.NetworkStatusBar
import com.example.myapp.core.ui.NetworkStatusViewModel
import com.example.myapp.core.ui.animations.AnimatedItemCard
import com.example.myapp.core.ui.animations.ShimmerLoadingItem
import com.example.myapp.todo.data.Item
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemsScreen(
    onItemClick: (id: String) -> Unit,
    onAddItem: () -> Unit,
    onLogout: () -> Unit
) {
    Log.d("ItemsScreen", "recompose")
    val itemsViewModel = viewModel<ItemsViewModel>(factory = ItemsViewModel.Factory)
    val itemsUiState by itemsViewModel.uiState.collectAsStateWithLifecycle(initialValue = emptyList())

    // Network Status (System Service)
    val networkStatusViewModel = viewModel<NetworkStatusViewModel>(factory = NetworkStatusViewModel.Factory)
    val isOnline by networkStatusViewModel.isOnline.collectAsStateWithLifecycle()

    // Shake to refresh (Sensor)
    val shakeViewModel = viewModel<ShakeViewModel>(factory = ShakeViewModel.Factory)
    var isRefreshing by remember { mutableStateOf(false) }

    // Coroutine scope for refresh
    val scope = rememberCoroutineScope()

    // Listen for shake events to refresh
    LaunchedEffect(Unit) {
        shakeViewModel.shakeEvents.collectLatest {
            Log.d("ItemsScreen", "Shake detected! Refreshing...")
            isRefreshing = true
            itemsViewModel.loadItems()
            delay(1000)
            isRefreshing = false
        }
    }

    // FAB animation
    val fabScale by animateFloatAsState(
        targetValue = if (isRefreshing) 0.8f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "fabScale"
    )

    // Refresh icon rotation animation
    val infiniteTransition = rememberInfiniteTransition(label = "refresh")
    val refreshRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "refreshRotation"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(id = R.string.items))
                },
                actions = {
                    // Refresh button with rotation animation
                    IconButton(
                        onClick = {
                            scope.launch {
                                isRefreshing = true
                                itemsViewModel.loadItems()
                                delay(1000)
                                isRefreshing = false
                            }
                        }
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            modifier = if (isRefreshing) {
                                Modifier.graphicsLayer { rotationZ = refreshRotation }
                            } else {
                                Modifier
                            }
                        )
                    }
                    TextButton(onClick = onLogout) {
                        Text(text = "Logout")
                    }
                }
            )
        },
        floatingActionButton = {
            // Animated FAB
            FloatingActionButton(
                onClick = { onAddItem() },
                modifier = Modifier.scale(fabScale)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Network Status Bar (System Service - shows offline status)
            NetworkStatusBar(isOnline = isOnline)

            // Shake to refresh hint
            AnimatedVisibility(
                visible = isRefreshing,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (itemsUiState.isEmpty() && !isRefreshing) {
                // Show shimmer loading placeholders
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(5) {
                        ShimmerLoadingItem()
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    itemsIndexed(itemsUiState) { index, item ->
                        // Animated item cards
                        AnimatedItemCard(index = index) {
                            ItemDetail(item = item, onItemClick = onItemClick)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ItemDetail(item: Item, onItemClick: (id: String) -> Unit) {
    // Scale animation on press
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "cardScale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .scale(scale)
            .clickable {
                isPressed = true
                onItemClick(item._id)
            }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = item.title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))

            // Animated price display
            Row {
                Text(text = "Price: $", style = MaterialTheme.typography.bodyMedium)
                AnimatedContent(
                    targetState = item.price,
                    transitionSpec = {
                        slideInVertically { it } + fadeIn() togetherWith
                            slideOutVertically { -it } + fadeOut()
                    },
                    label = "priceAnimation"
                ) { price ->
                    Text(text = price.toString(), style = MaterialTheme.typography.bodyMedium)
                }
            }

            Text(text = "Date: ${item.date}", style = MaterialTheme.typography.bodySmall)

            // Animated status badge
            AnimatedContent(
                targetState = item.sold,
                transitionSpec = {
                    scaleIn() + fadeIn() togetherWith scaleOut() + fadeOut()
                },
                label = "soldAnimation"
            ) { sold ->
                Text(
                    text = if (sold) "🔴 Sold" else "🟢 Available",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
