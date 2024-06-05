package com.calpito.mediaplayer.ui.fragments

import android.content.Context
import android.health.connect.datatypes.units.Length
import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.calpito.mediaplayer.R
import com.calpito.mediaplayer.databinding.FragmentPlayBinding
import com.calpito.mediaplayer.model.OneTimeEvent
import com.calpito.mediaplayer.model.Resource

import com.calpito.mediaplayer.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [DetailsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@AndroidEntryPoint
class PlayFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private val mainViewModel by activityViewModels<MainViewModel>()
    private lateinit var binding: FragmentPlayBinding

    private var uiObserver: Job? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }



    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {



        // Inflate the layout for this fragment
        binding = FragmentPlayBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //init the view
        initView()
        setObservers()
    }


    private fun initView(){

        binding?.buttonShuffle?.setOnClickListener {
            mainViewModel.setShuffleMode()
        }

        binding?.buttonPrevious?.setOnClickListener {
            mainViewModel.playPrevious()
        }

        binding?.buttonPlay?.setOnClickListener {
            mainViewModel.playCurrentSong()
        }

        binding?.buttonPause?.setOnClickListener {
            mainViewModel.pause()
        }

        binding?.buttonNext?.setOnClickListener {
            mainViewModel.playNext()
        }

        //lets add stop button, this is standard for music players.
        binding?.buttonStop?.setOnClickListener {
            mainViewModel.stopMediaPlayer()
        }

        binding?.buttonRepeatAll?.setOnClickListener {
            mainViewModel.setRepeatAllMode()
        }

        binding?.buttonRepeatOne?.setOnClickListener {
            mainViewModel.setRepeatOneMode()
        }






        /* MusicPlayerImpl.setSongIdList(listOf(R.raw.clair_de_lune, R.raw.jazz_de_luxe))
       MusicPlayerImpl.playCurrent(this)

        */
        binding?.buttonPlaylist?.setOnClickListener {

            //navigate as defined in the nav graph
            findNavController().navigate(R.id.action_playfragment_to_playlistfragement)

        }
    }

    private fun setObservers() {

        uiObserver?.cancel() //make sure we dont have multiple observers
        //now lets collect our ui states so we can update our ui as required
        uiObserver = lifecycleScope.launch {
            // This will make sure the block is cancelled and restarted according to the lifecycle changes
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {

                //COLLECT UI STATE
                launch {
                    mainViewModel.uiState.collect {
                        when(it){
                            is Resource.Error -> {
                                //todo show some error ui
                            }
                            is Resource.Loading -> {
                                //todo show loading ui}
                            }
                            is Resource.Success -> {
                                val musicPlayerData = it.data
                                binding.tvCurrentSong.text = "${musicPlayerData.songIndexMap[musicPlayerData.currentSong]?:"?"} - ${ musicPlayerData.currentSong?.title ?: "" }"
                                binding.tvArtist.text = musicPlayerData.currentSong?.artist?:""
                                binding.tvState.text = "state: ${musicPlayerData.state.name ?: ""}"
                                binding.tvPlaybackMode.text = "playback mode: ${ musicPlayerData.playbackMode.name }"
                            }
                        }
                    }
                }

                //COLLECT ANY ONE TIME EVENTS
                launch {
                    mainViewModel.oneTimeEventSharedFlow.collect{
                        when(it){
                            is OneTimeEvent.ToastEvent->{
                                //show toast with this error
                                Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }
    }

}