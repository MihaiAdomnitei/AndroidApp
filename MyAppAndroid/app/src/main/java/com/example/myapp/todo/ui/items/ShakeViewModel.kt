package com.example.myapp.todo.ui.items

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.myapp.MyApplication
import com.example.myapp.core.TAG
import com.example.myapp.core.sensors.ShakeSensorManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class ShakeViewModel(
    private val shakeSensorManager: ShakeSensorManager
) : ViewModel() {

    private val _shakeEvents = MutableSharedFlow<Unit>()
    val shakeEvents: SharedFlow<Unit> = _shakeEvents.asSharedFlow()

    init {
        Log.d(TAG, "ShakeViewModel init")
        observeShakeEvents()
    }

    private fun observeShakeEvents() {
        viewModelScope.launch {
            shakeSensorManager.shakeEvents.collect {
                Log.d(TAG, "Shake event received in ViewModel")
                _shakeEvents.emit(Unit)
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MyApplication)
                ShakeViewModel(app.container.shakeSensorManager)
            }
        }
    }
}

