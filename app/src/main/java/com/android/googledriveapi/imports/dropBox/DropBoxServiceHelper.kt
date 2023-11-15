package com.android.googledriveapi.imports.dropBox

import androidx.appcompat.app.AppCompatActivity
import com.android.googledriveapi.imports.dropBox.api.DropboxApiWrapper
import com.android.googledriveapi.imports.dropBox.api.DropboxAppConfig
import com.android.googledriveapi.imports.dropBox.api.DropboxCredentialUtil
import com.android.googledriveapi.imports.dropBox.api.DropboxOAuthUtil

class DropBoxServiceHelper(private val activity: AppCompatActivity) {
    val dropboxCredentialUtil: DropboxCredentialUtil = DropboxCredentialUtil(activity.applicationContext)
    val dropboxAppConfig = DropboxAppConfig()
    val dropboxOAuthUtil: DropboxOAuthUtil = DropboxOAuthUtil(dropboxCredentialUtil, dropboxAppConfig)
    val dropboxApiWrapper: DropboxApiWrapper? = dropboxCredentialUtil.readCredentialLocally()
        ?.let { DropboxApiWrapper(it, dropboxAppConfig.clientIdentifier) }

}