package com.example.myapp.core.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun NetworkStatusBar(isOnline: Boolean) {
    AnimatedVisibility(
        visible = !isOnline,
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Red.copy(alpha = 0.8f))
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "📵 No Internet Connection - Offline Mode",
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

