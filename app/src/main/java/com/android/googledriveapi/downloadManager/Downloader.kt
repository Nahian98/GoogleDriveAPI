package com.android.googledriveapi.downloadManager

interface Downloader {

    fun donwloadFile(url: String): Long
}