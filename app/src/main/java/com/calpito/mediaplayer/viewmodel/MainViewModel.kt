package com.calpito.mediaplayer.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calpito.mediaplayer.R
import com.calpito.mediaplayer.interfaces.RepositoryInterface
import com.calpito.mediaplayer.model.MusicPlayerData
import com.calpito.mediaplayer.model.Song
import com.calpito.mediaplayer.utility.HelperFunctions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(val repository: RepositoryInterface,  val context: Context): ViewModel() {

    init {
        //load songs that are in storage
        viewModelScope.launch {
            //todo we might want to show a loading spinner here while we are loading songs and display
            val songs = repository.getSongsFromAssets()
            repository.setSongList(songs)
        }

    }
    fun playCurrentSong(){
        repository.playCurrentSong()
    }

    fun stopMediaPlayer(){
        repository.stopMediaPlayer()
    }

    fun playNext(){
        repository.nextTrack()
    }

    fun nextPlayBackMode(){
        repository.nextPlayBackMode()
    }

    fun pause(){
        repository.pauseMediaPlayer()
    }

    fun setShuffleMode(){
        repository.setShuffleMode()
    }

    fun playPrevious(){
        repository.previousTrack()
    }

    fun setRepeatAllMode(){
        repository.setRepeatAllMode()
    }

    fun setRepeatOneMode(){
        repository.setRepeatOneMode()
    }


    /*return the music player state for UI*/
    fun getMusicPlayerStateFlow():StateFlow<MusicPlayerData>{
        val musicPlayerState = repository.getMusicPlayerState()
        return musicPlayerState
    }

    fun songSelected(song:Song){
        repository.setCurrentSong(song)
    }

}