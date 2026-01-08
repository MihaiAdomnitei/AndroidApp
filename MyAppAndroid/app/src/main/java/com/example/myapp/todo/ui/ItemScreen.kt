package com.example.myapp.todo.ui

import android.Manifest
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.myapp.R
import com.example.myapp.todo.ui.item.ItemViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemScreen(itemId: String?, onClose: () -> Unit) {
    val context = LocalContext.current
    val itemViewModel = viewModel<ItemViewModel>(factory = ItemViewModel.Factory(itemId))
    val itemUiState = itemViewModel.uiState

    var title by remember(itemUiState.item.title) { mutableStateOf(itemUiState.item.title) }
    var price by remember(itemUiState.item.price) { mutableStateOf(itemUiState.item.price.toString()) }
    var date by remember(itemUiState.item.date) { mutableStateOf(itemUiState.item.date) }
    var sold by remember(itemUiState.item.sold) { mutableStateOf(itemUiState.item.sold) }

    // Camera state
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }

    // Animation states
    var isVisible by remember { mutableStateOf(false) }
    val saveButtonScale by animateFloatAsState(
        targetValue = if (itemUiState.isSaving) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "saveButtonScale"
    )

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            photoUri = tempPhotoUri
            Log.d("ItemScreen", "Photo captured: $photoUri")
        }
    }

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Create temp file and launch camera
            val photoFile = File(context.cacheDir, "photo_${System.currentTimeMillis()}.jpg")
            tempPhotoUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                photoFile
            )
            tempPhotoUri?.let { cameraLauncher.launch(it) }
        }
    }

    // Trigger enter animation
    LaunchedEffect(Unit) {
        isVisible = true
    }

    Log.d("ItemScreen", "recompose, itemId = $itemId")

    LaunchedEffect(itemUiState.savingCompleted) {
        Log.d("ItemScreen", "Saving completed")
        if (itemUiState.savingCompleted) {
            onClose()
        }
    }

    // Animated content
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(300)) + slideInVertically(
            initialOffsetY = { it / 4 },
            animationSpec = tween(300)
        ),
        exit = fadeOut() + slideOutVertically()
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(text = stringResource(id = if (itemId == null) R.string.new_item else R.string.edit_item))
                    },
                    navigationIcon = {
                        IconButton(onClick = onClose) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                if (itemUiState.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                    return@Scaffold
                }

                if (itemUiState.loadingError != null) {
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + expandVertically()
                    ) {
                        Text(
                            text = "Failed to load item: ${itemUiState.loadingError.message}",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    return@Scaffold
                }

                // Photo section with animation (Camera feature)
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + scaleIn()
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clickable {
                                permissionLauncher.launch(Manifest.permission.CAMERA)
                            },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            if (photoUri != null) {
                                AsyncImage(
                                    model = photoUri,
                                    contentDescription = "Product photo",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(12.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                // Overlay to retake
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(8.dp)
                                        .background(
                                            Color.Black.copy(alpha = 0.6f),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(8.dp)
                                ) {
                                    Text("📷", style = MaterialTheme.typography.titleLarge)
                                }
                            } else {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text("📷", style = MaterialTheme.typography.displayMedium)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "Tap to take photo",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Animated text fields
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(tween(300, delayMillis = 100)) + slideInHorizontally()
                ) {
                    TextField(
                        label = { Text("Title") },
                        value = title,
                        onValueChange = { title = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(tween(300, delayMillis = 150)) + slideInHorizontally()
                ) {
                    TextField(
                        label = { Text("Price") },
                        value = price,
                        onValueChange = { price = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        leadingIcon = { Text("$") }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(tween(300, delayMillis = 200)) + slideInHorizontally()
                ) {
                    TextField(
                        label = { Text("Date") },
                        value = date,
                        onValueChange = { date = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Animated checkbox
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(tween(300, delayMillis = 250)) + slideInHorizontally()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { sold = !sold }
                            .padding(8.dp)
                    ) {
                        Checkbox(
                            checked = sold,
                            onCheckedChange = { sold = it }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        AnimatedContent(
                            targetState = sold,
                            transitionSpec = {
                                fadeIn() + scaleIn() togetherWith fadeOut() + scaleOut()
                            },
                            label = "soldText"
                        ) { isSold ->
                            Text(
                                text = if (isSold) "🔴 Marked as Sold" else "🟢 Available for Sale",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Animated save button
                Button(
                    onClick = {
                        Log.d("ItemScreen", "save item...")
                        itemViewModel.saveOrUpdateItem(title, price.toIntOrNull() ?: 0, date, sold)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .scale(saveButtonScale),
                    enabled = !itemUiState.isSaving
                ) {
                    AnimatedContent(
                        targetState = itemUiState.isSaving,
                        transitionSpec = {
                            fadeIn() togetherWith fadeOut()
                        },
                        label = "saveButtonContent"
                    ) { isSaving ->
                        if (isSaving) {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Saving...")
                            }
                        } else {
                            Text("💾 Save Item")
                        }
                    }
                }

                // Animated error message
                AnimatedVisibility(
                    visible = itemUiState.savingError != null,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = "❌ Failed to save: ${itemUiState.savingError?.message}",
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }
    }
}
