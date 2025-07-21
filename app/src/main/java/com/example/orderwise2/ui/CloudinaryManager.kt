package com.example.orderwise2.ui

import android.content.Context
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import java.io.File
import java.io.InputStream

object CloudinaryManager {

    private var isInitialized = false

    // IMPORTANT: Move these to a secure location like local.properties
    private const val CLOUD_NAME = "dru1scnhi"
    private const val API_KEY = "682947453939451"
    private const val API_SECRET = "s0ywrXwJRMIeFExdEvuyHoPoeos"

    fun init(context: Context) {
        if (isInitialized) return
        val config = mapOf(
            "cloud_name" to CLOUD_NAME,
            "api_key" to API_KEY,
            "api_secret" to API_SECRET
        )
        MediaManager.init(context, config)
        isInitialized = true
    }

    fun uploadImage(
        inputStream: InputStream,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val bytes = inputStream.readBytes()
        MediaManager.get().upload(bytes)
            .callback(object : UploadCallback {
                override fun onStart(requestId: String) {
                    // Upload started
                }

                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                    // Upload in progress
                }

                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    val url = resultData["secure_url"] as? String
                    if (url != null) {
                        onSuccess(url)
                    } else {
                        onError("Upload succeeded but URL was not found.")
                    }
                }

                override fun onError(requestId: String, error: ErrorInfo) {
                    onError("Upload failed: ${error.description}")
                }

                override fun onReschedule(requestId: String, error: ErrorInfo) {
                    // Upload rescheduled
                }
            }).dispatch()
    }
} 