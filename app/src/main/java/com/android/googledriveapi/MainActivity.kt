package com.android.googledriveapi
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.android.googledriveapi.databinding.ActivityMainBinding
import kotlin.concurrent.thread
import android.Manifest
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.android.googledriveapi.downloadManager.AndroidDownloader
import com.android.googledriveapi.imports.dropBox.DropBoxServiceHelper
import com.android.googledriveapi.imports.googleDrive.GoogleDriveServiceHelper
import com.dropbox.core.oauth.DbxCredential


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var googleDriveServiceHelper: GoogleDriveServiceHelper
    private lateinit var dropBoxServiceHelper: DropBoxServiceHelper

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestPermissionLaunch.launch(requestedPermissions)

        googleDriveServiceHelper = GoogleDriveServiceHelper(this@MainActivity)
        dropBoxServiceHelper = DropBoxServiceHelper(this@MainActivity)

        initListener()
    }

//    override fun onResume() {
//        super.onResume()
//        dropBoxServiceHelper.dropboxOAuthUtil.onResume()
//    }

    private fun onPermissionDenied() {
        Toast.makeText(this@MainActivity,"Lack of permissions, please grant permissions first", Toast.LENGTH_SHORT).show()
    }

    private fun initListener() {
        binding.mcGDrive.setOnClickListener {
            googleDriveServiceHelper.requestGDriveSignIn()
        }

        binding.mcDropBox.setOnClickListener {
            if (!dropBoxServiceHelper.dropboxCredentialUtil.isAuthenticated()){
                val authCredential: DbxCredential? = dropBoxServiceHelper.dropboxOAuthUtil.startDropboxAuthorizationOAuth2(this@MainActivity)
                if (authCredential != null) {
                    dropBoxServiceHelper.dropboxCredentialUtil.storeCredentialLocally(authCredential)
                }
            } else {
                Toast.makeText(this@MainActivity, "Already signed in", Toast.LENGTH_SHORT).show()
                dropBoxServiceHelper.fetchAccountInfo()
                thread {
                    dropBoxServiceHelper.listFiles()
                }
            }
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

}