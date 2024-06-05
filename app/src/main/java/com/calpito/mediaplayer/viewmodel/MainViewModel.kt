package com.calpito.mediaplayer.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calpito.mediaplayer.R
import com.calpito.mediaplayer.interfaces.RepositoryInterface
import com.calpito.mediaplayer.model.MusicPlayerData
import com.calpito.mediaplayer.model.OneTimeEvent
import com.calpito.mediaplayer.model.Resource
import com.calpito.mediaplayer.model.Song
import com.calpito.mediaplayer.utility.HelperFunctions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(val repository: RepositoryInterface,  val context: Context): ViewModel() {
    //Shared flow for handing 1 time events in the ui
    private val _oneTimeEventSharedFlow= MutableSharedFlow<OneTimeEvent>()
    val oneTimeEventSharedFlow: SharedFlow<OneTimeEvent> = _oneTimeEventSharedFlow

    private val _uiState = MutableStateFlow<Resource<MusicPlayerData>>(Resource.Success(
        MusicPlayerData()
    ))
    val uiState:StateFlow<Resource<MusicPlayerData>> = _uiState

    init {

        //Collect the state from the music player
        viewModelScope.launch {
            //collect any errors from the music player
            repository.getMusicPlayerState().collect{ musicPlayerData ->
                //pass the error message to the UI
                _uiState.value = Resource.Success(musicPlayerData)
            }
        }

        //Collect any errors from the music player
        viewModelScope.launch {
            //collect any errors from the music player
            repository.getMusicPlayerErrorFLow().collect{errorString ->
                Log.d("MyViewModel", "Error collected: $errorString")
                //pass the error message to the UI
                _oneTimeEventSharedFlow.emit(OneTimeEvent.ToastEvent(errorString))
            }
        }


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



    fun songSelected(song:Song){
        repository.setCurrentSong(song)
    }

}