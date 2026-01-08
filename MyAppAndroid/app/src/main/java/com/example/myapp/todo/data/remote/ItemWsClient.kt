package com.example.myapp.todo.data.remote

import android.util.Log
import com.example.myapp.core.TAG
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

class ItemWsClient(private val okHttpClient: OkHttpClient) {
    private var webSocket: WebSocket? = null
    private var token: String? = null

    fun authorize(token: String) {
        this.token = token
    }

    fun openSocket(
        onEvent: (ItemEvent?) -> Unit,
        onClosed: () -> Unit,
        onFailure: () -> Unit
    ) {
        val request = Request.Builder()
            .url("ws://10.0.2.2:3000")
            .apply {
                token?.let { addHeader("Authorization", "Bearer $it") }
            }
            .build()

        webSocket = okHttpClient.newWebSocket(
            request,
            object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    Log.d(TAG, "WebSocket opened")
                }

                override fun onMessage(webSocket: WebSocket, text: String) {
                    Log.d(TAG, "WebSocket message: $text")
                    try {
                        val event = Gson().fromJson(text, ItemEvent::class.java)
                        onEvent(event)
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to parse WebSocket message", e)
                        onEvent(null)
                    }
                }

                override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                    Log.d(TAG, "WebSocket closing: $code $reason")
                    webSocket.close(1000, null)
                    onClosed()
                }

                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                    Log.d(TAG, "WebSocket closed: $code $reason")
                    onClosed()
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    Log.e(TAG, "WebSocket failure", t)
                    onFailure()
                }
            }
        )
    }

    fun closeSocket() {
        Log.d(TAG, "closeSocket")
        webSocket?.close(1000, "Client closing")
        webSocket = null
    }
}

