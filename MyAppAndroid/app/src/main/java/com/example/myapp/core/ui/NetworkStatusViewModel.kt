package com.example.myapp.core.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.myapp.MyApplication
import com.example.myapp.core.TAG
import com.example.myapp.core.util.NetworkMonitor
import com.example.myapp.core.util.NotificationHelper
import com.example.myapp.core.work.WorkManagerScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NetworkStatusViewModel(
    private val networkMonitor: NetworkMonitor,
    private val notificationHelper: NotificationHelper,
    private val app: MyApplication
) : ViewModel() {

    val isOnline: StateFlow<Boolean> = networkMonitor.isOnline
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = networkMonitor.isNetworkAvailable()
        )

    init {
        Log.d(TAG, "NetworkStatusViewModel init")
        observeNetworkChanges()
    }

    private fun observeNetworkChanges() {
        viewModelScope.launch {
            var wasOffline = !networkMonitor.isNetworkAvailable()

            networkMonitor.isOnline.collect { isOnline ->
                Log.d(TAG, "Network status changed: isOnline = $isOnline")

                if (isOnline && wasOffline) {
                    // Network restored - trigger sync and show notification
                    Log.d(TAG, "Network restored, triggering sync...")
                    notificationHelper.showNetworkRestoredNotification()
                    WorkManagerScheduler.triggerImmediateSync(app)
                } else if (!isOnline) {
                    // Network lost - show offline notification
                    Log.d(TAG, "Network lost, showing offline notification...")
                    notificationHelper.showOfflineNotification()
                }

                wasOffline = !isOnline
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MyApplication)
                NetworkStatusViewModel(
                    app.container.networkMonitor,
                    app.container.notificationHelper,
                    app
                )
            }
        }
    }
}

