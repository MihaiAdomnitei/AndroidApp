package com.example.myapp.core.camera

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.example.myapp.core.TAG
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.Executor

class CameraManager(private val context: Context) {

    private var imageCapture: ImageCapture? = null
    private var cameraProvider: ProcessCameraProvider? = null

    fun startCamera(
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView,
        onReady: () -> Unit
    ) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider?.unbindAll()
                cameraProvider?.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )
                Log.d(TAG, "Camera started successfully")
                onReady()
            } catch (e: Exception) {
                Log.e(TAG, "Camera start failed", e)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    fun takePhoto(
        executor: Executor,
        onPhotoCaptured: (Uri) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val imageCapture = imageCapture ?: return

        val photoFile = File(
            context.cacheDir,
            SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US)
                .format(System.currentTimeMillis()) + ".jpg"
        )

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            executor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    Log.d(TAG, "Photo captured: $savedUri")
                    onPhotoCaptured(savedUri)
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed", exception)
                    onError(exception)
                }
            }
        )
    }

    fun shutdown() {
        cameraProvider?.unbindAll()
    }
}

