package com.android.googledriveapi

interface Downloader {

    fun donwloadFile(url: String): Long
}