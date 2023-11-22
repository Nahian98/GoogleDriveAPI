package com.android.googledriveapi.imports.dropBox

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.android.googledriveapi.imports.dropBox.api.DropboxApiWrapper
import com.android.googledriveapi.imports.dropBox.api.DropboxAppConfig
import com.android.googledriveapi.imports.dropBox.api.DropboxCredentialUtil
import com.android.googledriveapi.imports.dropBox.api.DropboxOAuthUtil
import com.android.googledriveapi.imports.dropBox.api.GetCurrentAccountResult
import com.android.googledriveapi.model.Songs
import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.android.Auth
import com.dropbox.core.oauth.DbxCredential
import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.files.ListFolderResult
import kotlinx.coroutines.launch
import org.json.JSONObject

class DropBoxServiceHelper(private val activity: AppCompatActivity) {
    val dropboxCredentialUtil: DropboxCredentialUtil = DropboxCredentialUtil(activity.applicationContext)
    val dropboxAppConfig = DropboxAppConfig()
    val dropboxOAuthUtil: DropboxOAuthUtil = DropboxOAuthUtil(dropboxCredentialUtil, dropboxAppConfig)
    val dropboxApiWrapper: DropboxApiWrapper? = dropboxCredentialUtil.readCredentialLocally()
        ?.let { DropboxApiWrapper(it, dropboxAppConfig.clientIdentifier) }

    var fileList = mutableListOf<Songs>()
    val apiKey: String = "qorirrew2pvgo44"
    val clientIdentifier: String = "db-${apiKey}"


    fun listAllFilesAndFolders(credential: String?) {
        val dropboxClient: DbxClientV2 = DbxClientV2(
            DbxRequestConfig(clientIdentifier),
            credential
        )
        val files: ListFolderResult? = dropboxClient.files()?.listFolder("")

        if (files != null) {
            for (file in files.entries){
                // Converting metadata to jsonObject
                Log.d("_DFiles", "$file")
                val jsonObject = JSONObject(file.toString())
                val id = jsonObject.getString("path_lower")
                val tag = jsonObject.getString(".tag")
                val name = jsonObject.getString("name")

                // Detecting the fileType
                val fileType = if(tag == "folder"){
                    "folder"
                } else {
                    if (name.endsWith(".mp3")){
                        "music"
                    } else {
                        "others"
                    }
                }

                // Adding to the pojo class
                if (fileType == "folder"){
                    fileList.add(
                        Songs(
                            id = id,
                            fileType = fileType,
                            name = name,
                        )
                    )
                } else if (fileType == "music"){
                    fileList.add(
                        Songs(
                            id = id,
                            fileType = fileType,
                            name = name,
                        )
                    )
                }
            }
        }
    }

    fun listFilesFromFolders(credential: String?, id: String) {
        val dropboxClient: DbxClientV2 = DbxClientV2(
            DbxRequestConfig(clientIdentifier),
            credential
        )
        val files: ListFolderResult? = dropboxClient.files()?.listFolder(id)

        if (files != null) {
            fileList.clear()
            for (file in files.entries){
                // Converting metadata to jsonObject
                Log.d("_DFiles", "$file")
                val jsonObject = JSONObject(file.toString())
                val id = jsonObject.getString("path_lower")
                val tag = jsonObject.getString(".tag")
                val name = jsonObject.getString("name")

                // Detecting the fileType
                val fileType = if(tag == "folder"){
                    "folder"
                } else {
                    if (name.endsWith(".mp3")){
                        "music"
                    } else {
                        "others"
                    }
                }

                // Adding to the pojo class
                if (fileType == "folder"){
                    fileList.add(
                        Songs(
                            id = id,
                            fileType = fileType,
                            name = name,
                        )
                    )
                } else if (fileType == "music"){
                    fileList.add(
                        Songs(
                            id = id,
                            fileType = fileType,
                            name = name,
                        )
                    )
                }
            }
        }
    }

    fun startDropboxAuthorizationOAuth2(context: Context): DbxCredential? {
        Auth.startOAuth2Authentication(context, dropboxAppConfig.apiKey)
        return Auth.getDbxCredential()
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