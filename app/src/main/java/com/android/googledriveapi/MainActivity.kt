package com.android.googledriveapi
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.android.googledriveapi.databinding.ActivityMainBinding
import kotlin.concurrent.thread
import android.Manifest
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var googleDriveServiceHelper: GoogleDriveServiceHelper

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
        initListener()

        binding.ivGDrive.setImageDrawable(R.drawable.)
    }

    private fun onPermissionDenied() {
        Toast.makeText(this@MainActivity,"Lack of permissions, please grant permissions first", Toast.LENGTH_SHORT).show()
    }

    private fun initListener() {
        binding.mcGDrive.setOnClickListener {
            googleDriveServiceHelper.requestGDriveSignIn()
        }

        binding.btnDownload.setOnClickListener {
            val downloader = AndroidDownloader(this@MainActivity)
            val link = "https://drive.google.com/file/d/1F-vfvhw6B9UNNjqlnoK-2PBhJG2wIU_Z/view?usp=drive_link"
            thread {
                downloader.donwloadFile(link)
            }
        }
    }

//    https://drive.google.com/file/d/1ExoyEY1bscOQLuSy5dEqbmrIuKSelkvv/view?usp=drive_link
//    https://drive.google.com/file/d/1vsZmseeNBtzCsyW7PciWMT5ZraplNuOt/view?usp=drive_link
//    https://drive.google.com/file/d/1F-vfvhw6B9UNNjqlnoK-2PBhJG2wIU_Z/view?usp=drive_link
//    https://drive.google.com/file/d/14XomAYktsmffUkccBYwRDTHLLVqJGVyU/view?usp=drive_link
}