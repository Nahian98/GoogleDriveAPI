package com.android.googledriveapi.imports.googleDrive
import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.android.googledriveapi.model.Songs
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import com.google.api.services.drive.model.FileList
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.Collections
import kotlin.concurrent.thread
class GoogleDriveServiceHelper(private val activity: AppCompatActivity) {

    private val TAG = "_GoogleDriveHelper"
    private val APP_NAME = "GDriveKanon"
    private lateinit var googleDriveService: Drive
    val fileList = mutableListOf<Songs>()

    private val signInLauncher =
        activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                handleSignInResult(result.data)
            } else {
                Log.d(TAG, "Unable to Sign In")
            }
        }

    fun requestGDriveSignIn() {
        Log.d(TAG, "Requesting sign-in")
        // Configure sign-in to request the user's ID, email address, and basic profile.
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestScopes(Scope(DriveScopes.DRIVE_METADATA_READONLY))
            .requestEmail()
            .build()

        // Build a GoogleSignInClient with the options specified by gso.
        val googleSignInClient = GoogleSignIn.getClient(activity, gso)

        signInLauncher.launch(googleSignInClient.signInIntent)
    }

    private fun handleSignInResult(result: Intent?) {
        GoogleSignIn.getSignedInAccountFromIntent(result)
            .addOnSuccessListener { googleAccount ->
                Log.d(TAG, "Signed In as ${googleAccount.email}")

                var credential: GoogleAccountCredential = GoogleAccountCredential.usingOAuth2(
                    activity, Collections.singleton(DriveScopes.DRIVE_METADATA_READONLY)
                )

                credential.selectedAccount = googleAccount.account
                thread {
                    Log.d(TAG, "AuthCode: ${googleAccount.serverAuthCode}")
                    Log.d(TAG, "AccessToken: ${credential.token}")
                }

                googleDriveService = Drive.Builder(
                    AndroidHttp.newCompatibleTransport(),
                    GsonFactory(),
                    credential
                ).setApplicationName(APP_NAME).build()

                GlobalScope.launch {
                    listAllFilesAndFolders()
                }

            }
            .addOnFailureListener {
                Log.e(TAG, "Unable to sign in.", it)
            }
    }

    fun listAllFilesAndFolders() {
        try {
            val result: FileList = googleDriveService.files().list()
                .setQ("mimeType = 'application/vnd.google-apps.folder' or mimeType = 'audio/mpeg'").execute()
//            val result2: FileList = googleDriveService.files().list()
//                .setQ("'1rQoPwJqjNrYInjvp6UKVVHQ595bdLKh0' in parents")
//                .execute()
            val files: MutableList<File> = result.files
            for (file in files) {
                Log.d("_Files", "$file")

                // Detecting the filetype [folder or music file]
                val fileType = if (file.mimeType == "application/vnd.google-apps.folder"){
                    "folder"
                } else {
                    "music"
                }

                // Adding the folders and music files into the pojo class
                fileList.add(
                    Songs(
                        id = file.id,
                        fileType = fileType,
                        name = file.name
                    )
                )

            }

        } catch (e: IOException) {
            // Handle API request error
            Log.e("_Files", "Error listing files", e)
        }
    }

    fun listOnlyFilesFromFolders(fileId: String): MutableList<Songs>? {
        try {
            val result: FileList = googleDriveService.files().list()
                .setQ("'$fileId' in parents")
                .execute()

            val files: MutableList<File> = result.files
            Log.d("_MusicFilesHelper", "$files")
            val fileList2 = mutableListOf<Songs>()
            for (file in files) {
                if (file.mimeType == "audio/mpeg") {
                    fileList2.clear()
                    fileList2.add(
                        Songs(
                            id = file.id,
                            fileType = "music",
                            name = file.name
                        )
                    )
                }
            }
            return fileList2
        } catch (e : IOException){
            e.printStackTrace()
        }
        return null
    }
}