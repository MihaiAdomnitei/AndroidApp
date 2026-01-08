package com.example.myapp.core

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.preferencesDataStore
import com.example.myapp.MyAppDatabase
import com.example.myapp.auth.data.AuthRepository
import com.example.myapp.auth.data.remote.AuthDataSource
import com.example.myapp.core.data.UserPreferencesRepository
import com.example.myapp.core.data.remote.Api
import com.example.myapp.core.sensors.ShakeSensorManager
import com.example.myapp.core.util.NetworkMonitor
import com.example.myapp.core.util.NotificationHelper
import com.example.myapp.todo.data.ItemRepository
import com.example.myapp.todo.data.remote.ItemService
import com.example.myapp.todo.data.remote.ItemWsClient

val Context.userPreferencesDataStore by preferencesDataStore(
    name = "user_preferences"
)

class AppContainer(val context: Context) {
    init {
        Log.d(TAG, "init")
    }

    private val itemService: ItemService = Api.retrofit.create(ItemService::class.java)
    private val itemWsClient: ItemWsClient = ItemWsClient(Api.okHttpClient)
    private val authDataSource: AuthDataSource = AuthDataSource()

    private val database: MyAppDatabase by lazy { MyAppDatabase.getDatabase(context) }

    val itemRepository: ItemRepository by lazy {
        ItemRepository(itemService, itemWsClient, database.itemDao())
    }

    val authRepository: AuthRepository by lazy {
        AuthRepository(authDataSource)
    }

    val userPreferencesRepository: UserPreferencesRepository by lazy {
        UserPreferencesRepository(context.userPreferencesDataStore)
    }

    // Network Monitor (System Service)
    val networkMonitor: NetworkMonitor by lazy {
        NetworkMonitor(context)
    }

    // Notification Helper (System Service)
    val notificationHelper: NotificationHelper by lazy {
        NotificationHelper(context)
    }

    // Shake Sensor Manager (Sensors)
    val shakeSensorManager: ShakeSensorManager by lazy {
        ShakeSensorManager(context)
    }
}
