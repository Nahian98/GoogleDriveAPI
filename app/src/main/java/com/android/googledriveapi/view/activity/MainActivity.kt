package com.android.googledriveapi.view.activity
import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import com.android.googledriveapi.R
import com.android.googledriveapi.databinding.ActivityMainBinding
import com.android.googledriveapi.downloadManager.AndroidDownloader
import com.box.sdk.BoxAPIConnection
import java.net.URI
import java.util.UUID
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var state: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initComponent()
        initListener()
    }

    private fun initComponent() {
        requestPermissionLaunch.launch(requestedPermissions)
    }


    private fun onPermissionDenied() {
        Toast.makeText(this@MainActivity,"Lack of permissions, please grant permissions first", Toast.LENGTH_SHORT).show()
    }

    private fun initListener() {
        binding.mcGDrive.setOnClickListener {
            startActivity(Intent(this@MainActivity, SongDownloadActivity::class.java).apply {
                putExtra("singInOption", "googleDrive")
            })
        }

        binding.mcDropBox.setOnClickListener {
            startActivity(Intent(this@MainActivity, SongDownloadActivity::class.java).apply {
                putExtra("singInOption", "dropbox")
            })

        }


        binding.mcBox.setOnClickListener {
            launchBoxTab()
        }

        binding.btnDownload.setOnClickListener {
            val downloader = AndroidDownloader(this@MainActivity)
            // https://drive.google.com/uc?id=<FILE_ID>&export=download
            val link = "https://drive.google.com/uc?id=1F-vfvhw6B9UNNjqlnoK-2PBhJG2wIU_Z&export=download "
            thread {
                downloader.donwloadFile(link)
            }
        }
    }
    private fun launchBoxTab() {
        val scopes = listOf("root_readonly")
        //        android:host="boxinandroid"
//        android:scheme="oauth"
        val redirectUri = URI.create("oauth://boxinandroid")
        state = UUID.randomUUID().toString()
        val authURL = BoxAPIConnection.getAuthorizationURL(
            getString(R.string.box_client_id),
            redirectUri, state, scopes
        )
        CustomTabsIntent.Builder().build().launchUrl(this, Uri.parse(authURL.toString()))
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        savedInstanceState.putString("State", state)
        super.onSaveInstanceState(savedInstanceState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        state = savedInstanceState.getString("State")
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent != null) {
            val callback = intent.data
            val callbackState = callback!!.getQueryParameter("state")
            if (callbackState != state) {
                Toast.makeText(this@MainActivity, "State validation failed!", Toast.LENGTH_LONG)
                    .show()
                return
            }
            val code = callback.getQueryParameter("code")
            if (code != null) {
                val itemsIntent = Intent(this@MainActivity, SongDownloadActivity::class.java)
                itemsIntent.putExtra("code", code)
                itemsIntent.putExtra("singInOption", "box")
                this@MainActivity.startActivity(itemsIntent)
            } else {
                Toast.makeText(this@MainActivity, "Authorization code not found in callback.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private val requestedPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.CHANGE_NETWORK_STATE,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.NEARBY_WIFI_DEVICES,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_MEDIA_AUDIO,
        )
    } else {
        arrayOf(
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.CHANGE_NETWORK_STATE,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
        )
    }

    private val requestPermissionLaunch = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { it ->
        if (it.all { it.value }) {
            Toast.makeText(this@MainActivity,"All permissions obtained", Toast.LENGTH_SHORT).show()
        } else {
            onPermissionDenied()
        }
    }

}