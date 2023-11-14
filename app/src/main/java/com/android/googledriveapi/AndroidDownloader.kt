package com.android.googledriveapi

import android.app.DownloadManager
import android.content.Context
import android.os.Environment
import androidx.core.net.toUri

class AndroidDownloader(private val context: Context) : Downloader {

    private val downloadManager = context.getSystemService(DownloadManager::class.java)
    override fun donwloadFile(url: String): Long {
        val request = DownloadManager.Request(url.toUri())
            .setMimeType("audio/mpeg")
            .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setTitle("Offline Music")
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "happy-day-113985.mp3")

        return downloadManager.enqueue(request)
    }

}