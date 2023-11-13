package com.android.googledriveapi

import androidx.appcompat.app.AppCompatActivity
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.UserRecoverableAuthException
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.FileList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var credentials: GoogleAccountCredential
    private lateinit var driveService: Drive
    private lateinit var gso: GoogleSignInOptions
    private lateinit var signInLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Configure sign-in to request the user's ID, email address, and basic profile.
        gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()

        // Build a GoogleSignInClient with the options specified by gso.
        val googleSignInClient = GoogleSignIn.getClient(this, gso)

        signInLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val data: Intent? = result.data
                    data?.let {
                        try {
                            // Attempt to handle the result and obtain credentials
                            handleSignInResult(GoogleSignIn.getSignedInAccountFromIntent(it))
                        } catch (e: UserRecoverableAuthIOException) {
                            // Handle the case where additional user consent is needed
                            val consentIntent = e.intent
                            signInLauncher.launch(consentIntent)
                        }
                    }
                }
            }

        // Attempt to sign in the user.
        val signInIntent = googleSignInClient.signInIntent
        signInLauncher.launch(signInIntent)
    }

    private fun handleSignInResult(task: Task<GoogleSignInAccount>) {
        try {
            val account = task.getResult(ApiException::class.java)!!
            credentials = GoogleAccountCredential.usingOAuth2(
                applicationContext, setOf("https://www.googleapis.com/auth/drive")
            )
            credentials.selectedAccount = account.account
            driveService = Drive.Builder(
                AndroidHttp.newCompatibleTransport(),
                GsonFactory(),
                credentials
            )
                .setApplicationName("GDriveKanon")
                .build()
            listFiles()
        } catch (e: ApiException) {
            if (e.statusCode == GoogleSignInStatusCodes.SIGN_IN_REQUIRED) {
                // The user needs to grant additional consent. Launch the intent.
                val signInIntent = GoogleSignIn.getClient(this, gso).signInIntent
                signInLauncher.launch(signInIntent)
            } else {
                // Handle other sign-in failures
                Log.e("SignInError", "Error signing in: ${e.message}")
            }
        } catch (e: UserRecoverableAuthException) {
            // Launch the intent for user consent
            signInLauncher.launch(e.intent)
        }
    }


    private fun listFiles() {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val result: FileList = driveService.files().list().execute()
                val files = result.files
                for (file in files) {
                    Log.d("_Files", "$file")
                }
            } catch (e: IOException) {
                // Handle API request error
                Log.e("_Files", "Error listing files", e)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            // User granted consent, proceed with Drive API operations
            listFiles()
        } else {
            // User denied consent, handle accordingly
            Log.e("SignInError", "User denied consent.")
        }
    }

}