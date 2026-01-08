package com.example.myapp.todo.ui.item

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.myapp.MyApplication
import com.example.myapp.core.TAG
import com.example.myapp.core.util.NotificationHelper
import com.example.myapp.todo.data.Item
import com.example.myapp.todo.data.ItemRepository
import kotlinx.coroutines.launch

data class ItemUiState(
    val itemId: String? = null,
    val item: Item = Item(),
    val isSaving: Boolean = false,
    val savingError: Throwable? = null,
    val savingCompleted: Boolean = false,
    val isLoading: Boolean = false,
    val loadingError: Throwable? = null
)

class ItemViewModel(
    private val itemId: String?,
    private val itemRepository: ItemRepository,
    private val notificationHelper: NotificationHelper
) : ViewModel() {

    var uiState: ItemUiState by mutableStateOf(ItemUiState(isLoading = true))
        private set

    init {
        Log.d(TAG, "init")
        if (itemId != null) {
            loadItem()
        } else {
            uiState = uiState.copy(item = Item(), isLoading = false)
        }
    }

    private fun loadItem() {
        viewModelScope.launch {
            itemRepository.itemStream.collect { items ->
                val item = items.find { it._id == itemId }
                if (item != null) {
                    uiState = uiState.copy(item = item, isLoading = false)
                }
            }
        }
    }

    fun saveOrUpdateItem(title: String, price: Int, date: String, sold: Boolean) {
        viewModelScope.launch {
            Log.d(TAG, "saveOrUpdateItem...")
            try {
                uiState = uiState.copy(isSaving = true, savingError = null)
                val item = uiState.item.copy(title = title, price = price, date = date, sold = sold)
                if (itemId == null) {
                    itemRepository.save(item)
                    // Show notification for new item (System Service - Notifications)
                    notificationHelper.showNewItemNotification(title)
                } else {
                    itemRepository.update(item)
                }
                Log.d(TAG, "saveOrUpdateItem succeeded")
                uiState = uiState.copy(isSaving = false, savingCompleted = true)
            } catch (e: Exception) {
                Log.d(TAG, "saveOrUpdateItem failed", e)
                uiState = uiState.copy(isSaving = false, savingError = e)
            }
        }
    }

    companion object {
        fun Factory(itemId: String?): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app =
                    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MyApplication)
                ItemViewModel(
                    itemId,
                    app.container.itemRepository,
                    app.container.notificationHelper
                )
            }
        }
    }
}
