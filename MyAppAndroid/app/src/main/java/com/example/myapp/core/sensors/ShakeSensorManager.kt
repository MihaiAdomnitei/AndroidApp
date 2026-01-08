package com.example.myapp.core.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import com.example.myapp.core.TAG
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.math.sqrt

class ShakeSensorManager(private val context: Context) {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    companion object {
        private const val SHAKE_THRESHOLD = 12.0f
        private const val SHAKE_TIME_INTERVAL = 500L
    }

    private var lastShakeTime = 0L

    val shakeEvents: Flow<Unit> = callbackFlow {
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.let {
                    val x = it.values[0]
                    val y = it.values[1]
                    val z = it.values[2]

                    // Calculate acceleration magnitude
                    val acceleration = sqrt((x * x + y * y + z * z).toDouble()).toFloat()

                    val currentTime = System.currentTimeMillis()

                    if (acceleration > SHAKE_THRESHOLD) {
                        if (currentTime - lastShakeTime > SHAKE_TIME_INTERVAL) {
                            lastShakeTime = currentTime
                            Log.d(TAG, "🔔 Shake detected! Acceleration: $acceleration")
                            trySend(Unit)
                        }
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                Log.d(TAG, "Sensor accuracy changed: $accuracy")
            }
        }

        Log.d(TAG, "Registering accelerometer listener")
        sensorManager.registerListener(
            listener,
            accelerometer,
            SensorManager.SENSOR_DELAY_NORMAL
        )

        awaitClose {
            Log.d(TAG, "Unregistering accelerometer listener")
            sensorManager.unregisterListener(listener)
        }
    }

    fun isAccelerometerAvailable(): Boolean {
        return accelerometer != null
    }
}

