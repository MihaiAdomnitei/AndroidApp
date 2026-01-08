package com.example.myapp.core.work

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.myapp.core.TAG
import java.util.concurrent.TimeUnit

object WorkManagerScheduler {

    /**
     * Schedule periodic sync every 15 minutes (minimum interval for WorkManager)
     */
    fun schedulePeriodicSync(context: Context) {
        Log.d(TAG, "Scheduling periodic sync...")

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = PeriodicWorkRequestBuilder<SyncDataWorker>(
            15, TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            SyncDataWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )

        Log.d(TAG, "Periodic sync scheduled")
    }

    /**
     * Trigger immediate sync when network becomes available
     */
    fun triggerImmediateSync(context: Context) {
        Log.d(TAG, "Triggering immediate sync...")

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = OneTimeWorkRequestBuilder<SyncDataWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueue(syncRequest)

        Log.d(TAG, "Immediate sync triggered")
    }

    /**
     * Cancel all scheduled sync work
     */
    fun cancelSync(context: Context) {
        Log.d(TAG, "Cancelling sync work...")
        WorkManager.getInstance(context).cancelUniqueWork(SyncDataWorker.WORK_NAME)
    }
}

