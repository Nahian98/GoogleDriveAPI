package com.android.googledriveapi
import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
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
import java.io.IOException
import java.util.Collections
import kotlin.concurrent.thread
class GoogleDriveServiceHelper(val activity: AppCompatActivity) {

    private val TAG = "_GoogleDriveHelper"
    private val APP_NAME = "GDriveKanon"
    private var fileList = mutableListOf<File>()

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

                val googleDriveService = Drive.Builder(
                    AndroidHttp.newCompatibleTransport(),
                    GsonFactory(),
                    credential
                ).setApplicationName(APP_NAME).build()

                thread {
                    listFiles(googleDriveService)
                }
            }
            .addOnFailureListener {
                Log.e(TAG, "Unable to sign in.", it)
            }
    }

    private fun listFiles(googleDriveService: Drive) {
        try {
            val result: FileList = googleDriveService.files().list().execute()
            val files: MutableList<File> = result.files
            for (file in files) {
                Log.d("_Files", "$file")
                fileList.add(file)
            }
        } catch (e: IOException) {
            // Handle API request error
            Log.e("_Files", "Error listing files", e)
        }
    }
}