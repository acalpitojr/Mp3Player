package com.calpito.mediaplayer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.calpito.mediaplayer.databinding.ActivityMainBinding
import com.calpito.mediaplayer.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val mainViewModel: MainViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initView()
        setObservers()


    }

    private fun initView(){
        binding?.buttonPlay?.setOnClickListener {
            mainViewModel.playCurrentSong()
        }

        binding?.buttonStop?.setOnClickListener {
            mainViewModel.stopMediaPlayer()
        }

        binding?.buttonNext?.setOnClickListener {
            mainViewModel.playNext()
        }
        binding?.buttonStop?.setOnClickListener {
            mainViewModel.stopMediaPlayer()
        }
        binding?.buttonPause?.setOnClickListener {
            mainViewModel.pause()
        }
        binding?.buttonPlaybackMode?.setOnClickListener {
            mainViewModel.nextPlayBackMode()
        }
        /* MusicPlayerImpl.setSongIdList(listOf(R.raw.clair_de_lune, R.raw.jazz_de_luxe))
       MusicPlayerImpl.playCurrent(this)
*//*

        */
    }

    private fun setObservers() {
        //now lets collect our ui states so we can update our ui as required
        lifecycleScope.launch {
            // This will make sure the block is cancelled and restarted according to the lifecycle changes
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainViewModel.getMusicPlayerStateFlow().collect {
                    Log.d("MEDIA_PLAYER_UPDATE", it.toString())
                    binding.tvCurrentSong.text = it.currentSong?.title?:""
                    binding.tvPlaybackMode.text = it.playbackMode.name

                }
            }
        }
    }

}
