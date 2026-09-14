package com.example.mall_android

import com.example.mall_android.model.Notification
import com.google.gson.Gson
import okhttp3.*
import java.io.BufferedReader
import java.io.IOException
import java.util.concurrent.TimeUnit

class NotificationManager(
    private val apiClient: ApiClient,
    private val getToken: () -> String?,
    private val onNotification: (Notification) -> Unit
) {
    companion object {
        private const val RECONNECT_DELAY_MS = 30_000L
    }

    private val gson = Gson()
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.SECONDS)
        .pingInterval(25, TimeUnit.SECONDS)
        .build()

    @Volatile
    private var running = false
    private var workerThread: Thread? = null
    @Volatile
    private var currentCall: Call? = null

    fun start() {
        if (workerThread?.isAlive == true) return
        running = true
        workerThread = Thread {
            while (running) {
                connect()
            }
        }
        workerThread?.start()
    }

    fun stop() {
        running = false
        currentCall?.cancel()
        workerThread?.interrupt()
        workerThread?.join(1000)
        workerThread = null
    }

    private fun connect() {
        if (!running) return

        val token = getToken()
        if (token.isNullOrBlank()) {
            sleepQuietly(RECONNECT_DELAY_MS)
            return
        }

        apiClient.sseTicket().fold(
            onSuccess = { ticket ->
                if (ticket.isBlank()) {
                    sleepQuietly(RECONNECT_DELAY_MS)
                } else {
                    connectToStream(ticket)
                }
            },
            onFailure = { _ ->
                sleepQuietly(RECONNECT_DELAY_MS)
            }
        )
    }

    private fun connectToStream(ticket: String) {
        if (!running) return

        val url = apiClient.sseStreamUrl(ticket)
        val request = Request.Builder()
            .url(url)
            .get()
            .header("Accept", "text/event-stream")
            .header("Cache-Control", "no-cache")
            .build()

        val call = client.newCall(request)
        currentCall = call

        try {
            call.execute().use { response ->
                if (response.isSuccessful) {
                    readEvents(response.body)
                } else {
                    sleepQuietly(RECONNECT_DELAY_MS)
                }
            }
        } catch (e: IOException) {
            if (running) sleepQuietly(RECONNECT_DELAY_MS)
        }
    }

    private fun readEvents(body: ResponseBody?) {
        if (body == null) {
            sleepQuietly(RECONNECT_DELAY_MS)
            return
        }

        val reader = BufferedReader(body.charStream())
        var eventType = ""
        var dataBuffer = ""

        try {
            while (running) {
                val line = reader.readLine() ?: break

                when {
                    line.startsWith("event:") -> {
                        eventType = line.removePrefix("event:").trim()
                    }
                    line.startsWith("data:") -> {
                        val value = line.removePrefix("data:").trim()
                        if (dataBuffer.isEmpty()) dataBuffer = value else dataBuffer += "\n" + value
                    }
                    line.isEmpty() -> {
                        if (eventType == "notification" && dataBuffer.isNotEmpty()) {
                            try {
                                val notification = gson.fromJson(dataBuffer, Notification::class.java)
                                onNotification(notification)
                            } catch (e: Exception) {
                                // Ignore malformed notification payloads
                            }
                        }
                        eventType = ""
                        dataBuffer = ""
                    }
                }
            }
        } catch (e: Exception) {
            // Connection dropped or stream ended
        } finally {
            try { reader.close() } catch (e: Exception) { }
        }

        if (running) sleepQuietly(RECONNECT_DELAY_MS)
    }

    private fun sleepQuietly(ms: Long) {
        try {
            Thread.sleep(ms)
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
        }
    }
}
