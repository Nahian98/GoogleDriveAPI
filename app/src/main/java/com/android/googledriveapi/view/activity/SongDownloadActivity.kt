package com.android.googledriveapi.view.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.googledriveapi.R
import com.android.googledriveapi.databinding.ActivitySongDownloadBinding
import com.android.googledriveapi.imports.dropBox.DropBoxServiceHelper
import com.android.googledriveapi.imports.googleDrive.GoogleDriveServiceHelper
import com.android.googledriveapi.view.adapter.SongsAdapter
import kotlin.concurrent.thread
import kotlin.math.sign

class SongDownloadActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySongDownloadBinding
    private lateinit var googleDriveServiceHelper: GoogleDriveServiceHelper
    private lateinit var dropBoxServiceHelper: DropBoxServiceHelper
    private lateinit var signInOption: String
    private lateinit var adapter: SongsAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySongDownloadBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getExtra()
        initComponent()
        initListener()
    }

    private fun getExtra() {
        signInOption = intent.getStringExtra("singInOption").toString()
    }

    private fun initComponent() {
        adapter = SongsAdapter(this@SongDownloadActivity, arrayListOf())
        binding.rvSongs.adapter = adapter
        val linearLayoutManager = LinearLayoutManager(this@SongDownloadActivity)
        binding.rvSongs.layoutManager = linearLayoutManager
        googleDriveServiceHelper = GoogleDriveServiceHelper(this@SongDownloadActivity)
        dropBoxServiceHelper = DropBoxServiceHelper(this@SongDownloadActivity)
        if (signInOption == "googleDrive"){
            googleDriveServiceHelper.requestGDriveSignIn()
            Log.d("_SongDownloadActivity", "${googleDriveServiceHelper.fileList}")

            object : CountDownTimer(5000,1000){
                override fun onTick(millisUntilFinished: Long) {

                }

                override fun onFinish() {
                    adapter.setData(googleDriveServiceHelper.fileList)
                }
            }.start()
        } else if(signInOption == "dropbox") {
            if (!dropBoxServiceHelper.dropboxCredentialUtil.isAuthenticated()){
                val credential = dropBoxServiceHelper.dropboxOAuthUtil.startDropboxAuthorization2PKCE(this@SongDownloadActivity)
//                dropBoxServiceHelper.dropboxOAuthUtil.onResume()
                if (credential != null) {
                    dropBoxServiceHelper.dropboxCredentialUtil.storeCredentialLocally(credential)
                }
            } else {
                Toast.makeText(this@SongDownloadActivity, "Already signed in", Toast.LENGTH_SHORT).show()
                dropBoxServiceHelper.fetchAccountInfo()

                object : CountDownTimer(5000,1000){
                    override fun onTick(millisUntilFinished: Long) {

                    }

                    override fun onFinish() {
                        thread {
                            dropBoxServiceHelper.listFiles()
                            adapter.setData(dropBoxServiceHelper.fileList)
                        }

                    }
                }.start()
            }
        }
    }

    private fun initListener() {

    }
}