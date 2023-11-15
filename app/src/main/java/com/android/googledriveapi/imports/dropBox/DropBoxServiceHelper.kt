package com.android.googledriveapi.imports.dropBox

import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.android.googledriveapi.imports.dropBox.api.DropboxApiWrapper
import com.android.googledriveapi.imports.dropBox.api.DropboxAppConfig
import com.android.googledriveapi.imports.dropBox.api.DropboxCredentialUtil
import com.android.googledriveapi.imports.dropBox.api.DropboxOAuthUtil
import com.android.googledriveapi.imports.dropBox.api.GetCurrentAccountResult
import com.dropbox.core.v2.files.ListFolderResult
import kotlinx.coroutines.launch

class DropBoxServiceHelper(private val activity: AppCompatActivity) {
    val dropboxCredentialUtil: DropboxCredentialUtil = DropboxCredentialUtil(activity.applicationContext)
    val dropboxAppConfig = DropboxAppConfig()
    val dropboxOAuthUtil: DropboxOAuthUtil = DropboxOAuthUtil(dropboxCredentialUtil, dropboxAppConfig)
    val dropboxApiWrapper: DropboxApiWrapper? = dropboxCredentialUtil.readCredentialLocally()
        ?.let { DropboxApiWrapper(it, dropboxAppConfig.clientIdentifier) }

    fun listFiles(){
        val files: ListFolderResult? = dropboxApiWrapper?.dropboxClient?.files()?.listFolder("")
        if (files != null) {
            for (file in files.entries){
                Log.d("_DFiles", "$file")
            }
        }
    }

    fun fetchAccountInfo() {
        activity.lifecycleScope.launch {
            when (val accountResult = dropboxApiWrapper?.getCurrentAccount()) {
                is GetCurrentAccountResult.Error -> {
                    Log.e(
                        javaClass.name,
                        "Failed to get account details.",
                        accountResult.e
                    )
                    Toast.makeText(
                        activity.applicationContext,
                        "Error getting account info!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                is GetCurrentAccountResult.Success -> {
                    val account = accountResult.account
                    Log.d("__DropboxAccount", "Email: ${account.email}  DisplayName: ${account.name.displayName} AccountType: ${account.accountType.name}")
                }

                else -> {}
            }
        }
    }

}