package com.example.myapp.todo.data

import android.util.Log
import com.example.myapp.core.TAG
import com.example.myapp.core.data.remote.Api
import com.example.myapp.todo.data.local.ItemDao
import com.example.myapp.todo.data.remote.ItemEvent
import com.example.myapp.todo.data.remote.ItemService
import com.example.myapp.todo.data.remote.ItemWsClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext

class ItemRepository(
    private val itemService: ItemService,
    private val itemWsClient: ItemWsClient,
    private val itemDao: ItemDao
) {
    val itemStream by lazy { itemDao.getAll() }

    init {
        Log.d(TAG, "init")
    }

    private fun getBearerToken(): String {
        val token = Api.tokenInterceptor.token
        Log.d(TAG, "getBearerToken - token: $token")
        return "Bearer ${token ?: ""}"
    }

    suspend fun refresh() {
        Log.d(TAG, "refresh started")
        try {
            val items = itemService.find(authorization = getBearerToken())
            itemDao.deleteAll()
            items.forEach { itemDao.insert(it) }
            Log.d(TAG, "refresh succeeded")
        } catch (e: Exception) {
            Log.w(TAG, "refresh failed", e)
        }
    }

    suspend fun openWsClient() {
        Log.d(TAG, "openWsClient")
        withContext(Dispatchers.IO) {
            getItemEvents().collect {
                Log.d(TAG, "Item event collected $it")
                if (it.isSuccess) {
                    val itemEvent = it.getOrNull();
                    when (itemEvent?.type) {
                        "created" -> handleItemCreated(itemEvent.payload)
                        "updated" -> handleItemUpdated(itemEvent.payload)
                        "deleted" -> handleItemDeleted(itemEvent.payload)
                    }
                }
            }
        }
    }

    suspend fun closeWsClient() {
        Log.d(TAG, "closeWsClient")
        withContext(Dispatchers.IO) {
            itemWsClient.closeSocket()
        }
    }

    suspend fun getItemEvents(): Flow<kotlin.Result<ItemEvent>> = callbackFlow {
        Log.d(TAG, "getItemEvents started")
        itemWsClient.openSocket(
            onEvent = {
                Log.d(TAG, "onEvent $it")
                if (it != null) {
                    trySend(kotlin.Result.success(it))
                }
            },
            onClosed = { close() },
            onFailure = { close() });
        awaitClose { itemWsClient.closeSocket() }
    }

    suspend fun update(item: Item): Item {
        Log.d(TAG, "update $item...")
        // Salvează local întâi (offline-first)
        val localItem = item.copy(isSynced = false)
        itemDao.update(localItem)
        Log.d(TAG, "update saved locally")

        // Încearcă să sincronizeze cu serverul
        return try {
            val updatedItem = itemService.update(
                itemId = item._id,
                item = item,
                authorization = getBearerToken()
            )
            // Marchează ca sincronizat
            val syncedItem = updatedItem.copy(isSynced = true)
            itemDao.update(syncedItem)
            Log.d(TAG, "update synced with server")
            syncedItem
        } catch (e: Exception) {
            Log.w(TAG, "update failed to sync, saved locally", e)
            localItem
        }
    }

    suspend fun save(item: Item): Item {
        Log.d(TAG, "save $item...")
        // Generează ID local și salvează în Room (offline-first)
        val localItem = item.copy(
            _id = if (item._id.isEmpty()) java.util.UUID.randomUUID().toString() else item._id,
            isSynced = false
        )
        itemDao.insert(localItem)
        Log.d(TAG, "save saved locally with id: ${localItem._id}")

        // Încearcă să sincronizeze cu serverul
        return try {
            val authHeader = getBearerToken()
            val createdItem = itemService.create(item = localItem, authorization = authHeader)
            // Șterge itemul local și inserează cel de pe server (poate avea alt ID)
            itemDao.deleteById(localItem._id)
            val syncedItem = createdItem.copy(isSynced = true)
            itemDao.insert(syncedItem)
            Log.d(TAG, "save synced with server, new id: ${syncedItem._id}")
            syncedItem
        } catch (e: Exception) {
            Log.w(TAG, "save failed to sync, saved locally only", e)
            localItem
        }
    }

    private suspend fun handleItemDeleted(item: Item) {
        Log.d(TAG, "handleItemDeleted - todo $item")
    }

    private suspend fun handleItemUpdated(item: Item) {
        Log.d(TAG, "handleItemUpdated...")
        itemDao.update(item)
    }

    private suspend fun handleItemCreated(item: Item) {
        Log.d(TAG, "handleItemCreated...")
        itemDao.insert(item)
    }

    suspend fun deleteAll() {
        itemDao.deleteAll()
    }

    fun setToken(token: String) {
        itemWsClient.authorize(token)
    }

    // Sincronizează itemele salvate offline când redevii online
    suspend fun syncPendingItems() {
        Log.d(TAG, "syncPendingItems started")
        try {
            val unsyncedItems = itemDao.getUnsyncedItems()
            Log.d(TAG, "Found ${unsyncedItems.size} unsynced items")

            for (item in unsyncedItems) {
                try {
                    val authHeader = getBearerToken()
                    // Încearcă să creeze pe server
                    val createdItem = itemService.create(item = item, authorization = authHeader)
                    // Șterge versiunea locală și salvează versiunea de pe server
                    itemDao.deleteById(item._id)
                    val syncedItem = createdItem.copy(isSynced = true)
                    itemDao.insert(syncedItem)
                    Log.d(TAG, "Synced item: ${item._id} -> ${syncedItem._id}")
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to sync item ${item._id}", e)
                }
            }
            Log.d(TAG, "syncPendingItems completed")
        } catch (e: Exception) {
            Log.w(TAG, "syncPendingItems failed", e)
        }
    }
}