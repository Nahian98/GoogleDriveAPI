package com.android.googledriveapi
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.android.googledriveapi.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var googleDriveServiceHelper: GoogleDriveServiceHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        googleDriveServiceHelper = GoogleDriveServiceHelper(this@MainActivity)
        initListener()
    }

    private fun initListener() {
        binding.mcGDrive.setOnClickListener {
            googleDriveServiceHelper.requestGDriveSignIn()
        }
    }
}