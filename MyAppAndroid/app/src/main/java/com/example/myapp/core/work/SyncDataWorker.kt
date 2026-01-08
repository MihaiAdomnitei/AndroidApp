package com.example.myapp.core.work

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.myapp.MyApplication
import com.example.myapp.core.TAG
import com.example.myapp.core.util.NotificationHelper

class SyncDataWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        Log.d(TAG, "SyncDataWorker: Starting sync...")

        val notificationHelper = NotificationHelper(context)

        return try {
            val app = applicationContext as MyApplication
            val itemRepository = app.container.itemRepository

            // Show syncing notification
            notificationHelper.showSyncNotification("Syncing data with server...")

            // Refresh data from server and save to local database
            itemRepository.refresh()

            Log.d(TAG, "SyncDataWorker: Sync completed successfully")
            notificationHelper.showSyncNotification("Data synced successfully!")

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "SyncDataWorker: Sync failed", e)
            notificationHelper.showSyncNotification("Sync failed: ${e.message}")

            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }

    companion object {
        const val WORK_NAME = "sync_data_worker"
    }
}

