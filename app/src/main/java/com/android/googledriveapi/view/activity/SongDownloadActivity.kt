package com.android.googledriveapi.view.activity

import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.googledriveapi.R
import com.android.googledriveapi.databinding.ActivitySongDownloadBinding
import com.android.googledriveapi.imports.dropBox.DropBoxServiceHelper
import com.android.googledriveapi.imports.googleDrive.GoogleDriveServiceHelper
import com.android.googledriveapi.model.BoxFiles
import com.android.googledriveapi.model.BoxFolders
import com.android.googledriveapi.model.BoxItems
import com.android.googledriveapi.view.adapter.BoxAdapter
import com.android.googledriveapi.view.adapter.BoxItemAdapter
import com.android.googledriveapi.view.adapter.SongsAdapter
import com.box.sdk.BoxAPIConnection
import com.box.sdk.BoxFile
import com.box.sdk.BoxFolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors

class SongDownloadActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySongDownloadBinding
    private lateinit var googleDriveServiceHelper: GoogleDriveServiceHelper
    private lateinit var dropBoxServiceHelper: DropBoxServiceHelper
    private lateinit var signInOption: String
    private lateinit var adapter: SongsAdapter
    private lateinit var sharedPreferences: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySongDownloadBinding.inflate(layoutInflater)
        setContentView(binding.root)


        getExtra()
        initComponent()
        initListener()
    }

    override fun onStart() {
        super.onStart()
        sharedPreferences = getSharedPreferences(
            "dropbox-sample2",
            AppCompatActivity.MODE_PRIVATE
        )
        if (signInOption == "googleDrive"){
            googleDriveServiceHelper.requestGDriveSignIn()

            // Setting adapter data
            GlobalScope.launch {
                delay(3000)
                withContext(Dispatchers.Main){
                    adapter.setData(googleDriveServiceHelper.fileList)
                }
            }

        }
        else if(signInOption == "dropbox") {
            val sharedPrefValidity = sharedPreferences.getString("credential", null)
            if (sharedPrefValidity == null){
                val credential = dropBoxServiceHelper.dropboxOAuthUtil.startDropboxAuthorizationOAuth2(this@SongDownloadActivity)
                if (credential != null) {
                    sharedPreferences.edit().apply{
                        putString("credential", credential.accessToken)
                        putString("refresh", credential.refreshToken)
                    }.apply()
                }

            } else {
                Toast.makeText(this@SongDownloadActivity, "Already signed in", Toast.LENGTH_SHORT).show()
                dropBoxServiceHelper.fetchAccountInfo()

                GlobalScope.launch {
                    dropBoxServiceHelper.listAllFilesAndFolders(sharedPreferences.getString("credential", null))
                    delay(2000)
                    withContext(Dispatchers.Main){
                        adapter.setData(dropBoxServiceHelper.fileList)
                        Log.d("_DBOXList", "${dropBoxServiceHelper.fileList}")
                    }
                }

            }
        } else if (signInOption == "box") {
            showProgressBar(binding.progressBar, binding.rvSongs)
            val extras = intent.extras
            if (extras != null) {
                val code = extras.getString("code")
                if (code != null) {
                    if (code.isNotEmpty()) {
                        updateUI(code, binding.progressBar, binding.rvSongs)
                    }
                }
                // in case we don't get any code we should display an error
            }
        }
    }

    override fun onResume() {
        super.onResume()
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
    }

    private fun initListener() {
        adapter.onItemClick = {songs ->
            if (songs.fileType == "folder"){
                val fileId = songs.id
                Log.d("_FileID", "$fileId")
                if (fileId.startsWith("/")){
                    // Dropbox files
                    GlobalScope.launch {
                        dropBoxServiceHelper.listFilesFromFolders(sharedPreferences.getString("credential", null), fileId)
                        delay(2000)
                        withContext(Dispatchers.Main){
                            adapter.setData(dropBoxServiceHelper.fileList)
                        }
                    }

                } else {
                    GlobalScope.launch {
                        delay(3000)
                        val musicFileList = googleDriveServiceHelper.listOnlyFilesFromFolders(fileId)
                        Log.d("_MusicFiles", "$musicFileList")
                        withContext(Dispatchers.Main){
                            musicFileList?.let { adapter.setData(it) }
                        }
                    }
                }
            }
        }
    }

    private fun updateUI(code: String, progressBar: ProgressBar, recyclerView: RecyclerView) {
        val executor = Executors.newSingleThreadExecutor()
        val handler = Handler(Looper.getMainLooper())
        executor.execute {
            val items = getFolderItems(code)
            handler.post {
                recyclerView.adapter = BoxAdapter(items)
                showData(progressBar, recyclerView)
            }
        }
    }

    private fun showProgressBar(progressBar: ProgressBar, recyclerView: RecyclerView) {
        recyclerView.visibility = View.GONE
        progressBar.visibility = View.VISIBLE
    }

    private fun showData(progressBar: ProgressBar, recyclerView: RecyclerView) {
        progressBar.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
    }

    private fun getFolderItems(code: String): List<BoxItems> {
        val api = BoxAPIConnection(
            getString(R.string.box_client_id),
            getString(R.string.box_client_secret), code
        )
        val boxFolder = BoxFolder(api, "0")
            .getChildren("name", "modified_by")
        val items: MutableList<BoxItems> = ArrayList()
        for (itemInfo in boxFolder) {
            if (itemInfo is BoxFile.Info) {
                val fileInfo = itemInfo
                Log.d("__BoxFiles", fileInfo.json)
                if (fileInfo.name.endsWith(".mp3")) {
                    val file = BoxFiles(fileInfo.name, fileInfo.modifiedBy.name)
                    items.add(file)
                }
            } else if (itemInfo is BoxFolder.Info) {
                val folderInfo = itemInfo
                Log.d("__BoxFolders", folderInfo.json)
                val folder = BoxFolders(folderInfo.name, folderInfo.modifiedBy.name)
                items.add(folder)
            }
        }
        return items
    }


}

// File Responses

// ## GDrive
//{"id":"1EwlifMcsrI5SiX0L3ZqbJr9tTg39g8r8","kind":"drive#file","mimeType":"audio/mpeg","name":"over.mp3"}
//{"id":"1rQoPwJqjNrYInjvp6UKVVHQ595bdLKh0","kind":"drive#file","mimeType":"application/vnd.google-apps.folder","name":"My Apps APKs"}

// ## Dropbox
//{".tag":"folder","name":"Photos","id":"id:o5sYNE9L92UAAAAAAAAABw","path_lower":"/photos","path_display":"/Photos"}
//{".tag":"file","name":"cake.jpg","id":"id:o5sYNE9L92UAAAAAAAAABg","client_modified":"2023-08-30T05:53:34Z","server_modified":"2023-08-30T05:53:34Z","rev":"016041d8c5173cf000000010ad95f71","size":460189,"path_lower":"/cake.jpg","path_display":"/cake.jpg","is_downloadable":true,"content_hash":"8061cf878ec50dd7811c138094575803eff41b30f515e28e2b15fc09a83e5438"}

// ## Box
//{"type":"folder","id":"235927328965","etag":"0","name":"Demofolder 1","modified_by":{"type":"user","id":"30123196222","name":"Abdullah Al Nahian Kanon","login":"kanonabdullahalnahian@gmail.com"}}
//{"type":"file","id":"1368004600744","etag":"0","name":"music.jpg","modified_by":{"type":"user","id":"30123196222","name":"Abdullah Al Nahian Kanon","login":"kanonabdullahalnahian@gmail.com"}}
