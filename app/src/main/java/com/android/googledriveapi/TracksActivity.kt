package com.android.googledriveapi

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.android.googledriveapi.databinding.ActivityTracksBinding

class TracksActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTracksBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTracksBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }
}