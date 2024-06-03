package com.calpito.mediaplayer.ui.activities

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.calpito.mediaplayer.databinding.ActivityMainNavBinding
import com.calpito.mediaplayer.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainNavActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainNavBinding
    private val mainViewModel by viewModels<MainViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainNavBinding.inflate(layoutInflater)
        setContentView(binding.root) //this content already contains the fragment manager, so it will kick off the fragment automatically
    }
}