package com.example.myapp.core.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.myapp.MainActivity
import com.example.myapp.R
import com.example.myapp.core.TAG

class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "myapp_channel"
        const val CHANNEL_NAME = "MyApp Notifications"
        const val SYNC_NOTIFICATION_ID = 1
        const val OFFLINE_NOTIFICATION_ID = 2
        const val NEW_ITEM_NOTIFICATION_ID = 3
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = "Notifications for MyApp"
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            Log.d(TAG, "Notification channel created")
        }
    }

    fun showSyncNotification(message: String) {
        showNotification(SYNC_NOTIFICATION_ID, "Sync Status", message)
    }

    fun showOfflineNotification() {
        showNotification(
            OFFLINE_NOTIFICATION_ID,
            "Offline Mode",
            "You are currently offline. Changes will be synced when connection is restored."
        )
    }

    fun showNewItemNotification(itemTitle: String) {
        showNotification(
            NEW_ITEM_NOTIFICATION_ID,
            "New Item Added",
            "Item '$itemTitle' was added successfully!"
        )
    }

    fun showNetworkRestoredNotification() {
        showNotification(
            OFFLINE_NOTIFICATION_ID,
            "Back Online",
            "Network connection restored. Syncing data..."
        )
    }

    private fun showNotification(notificationId: Int, title: String, message: String) {
        // Check permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                Log.w(TAG, "Notification permission not granted")
                return
            }
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
            Log.d(TAG, "Notification shown: $title - $message")
        } catch (e: SecurityException) {
            Log.e(TAG, "Failed to show notification", e)
        }
    }

    fun cancelNotification(notificationId: Int) {
        NotificationManagerCompat.from(context).cancel(notificationId)
    }
}

