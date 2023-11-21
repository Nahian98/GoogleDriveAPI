package com.android.googledriveapi.view.activity
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.android.googledriveapi.databinding.ActivityMainBinding
import kotlin.concurrent.thread
import android.Manifest
import android.content.Intent
import android.os.CountDownTimer
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.android.googledriveapi.downloadManager.AndroidDownloader
import com.android.googledriveapi.imports.dropBox.DropBoxServiceHelper
import com.android.googledriveapi.imports.googleDrive.GoogleDriveServiceHelper
import com.box.sdk.BoxAPIConnection
import com.box.sdk.BoxConfig
import com.dropbox.core.oauth.DbxCredential


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

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
        val api = BoxAPIConnection("81bgvgblrktpkrpqja8eogjpp95tbudl", "1n7FjU9cVi9Rruh4ydhza02EU4nokxOL",)
        requestPermissionLaunch.launch(requestedPermissions)
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
            startActivity(Intent(this@MainActivity, SongDownloadActivity::class.java).apply {
                putExtra("singInOption", "googleDrive")
            })
        }

        binding.mcDropBox.setOnClickListener {
            startActivity(Intent(this@MainActivity, SongDownloadActivity::class.java).apply {
                putExtra("singInOption", "dropbox")
            })
        }

        //            dropBoxServiceHelper.dropboxOAuthUtil.startDropboxAuthorization2PKCE(this@MainActivity)
//            dropBoxServiceHelper.dropboxOAuthUtil.onResume()
//            dropBoxServiceHelper.dropboxCredentialUtil.readCredentialLocally()
//            thread {
//                dropBoxServiceHelper.listFiles()
//            }

        binding.mcBox.setOnClickListener {
//            val session = BoxSession(this@MainActivity)
//            session.authenticate(this@MainActivity)
//            val boxFolderApi = BoxApiFolder(session)
//            val boxFileApi = BoxApiFile(session)
//
//            object : CountDownTimer(5000,1000){
//                override fun onTick(millisUntilFinished: Long) {
//
//                }
//
//                override fun onFinish() {
//                    thread {
//                        try {
//                            val folderItems = boxFolderApi.getItemsRequest(BoxConstants.ROOT_FOLDER_ID).send()
//                            for (boxItem in folderItems){
//                                Log.d("__BoxFolders", "$boxItem")
//                            }
//                        } catch (e: BoxException) {
//                            e.printStackTrace()
//                        }
//                    }
//                }
//            }.start()

            startActivity(Intent(this@MainActivity, BoxActivity::class.java))

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

    private fun getRootFolder() {

    }

}