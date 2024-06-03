package com.calpito.mediaplayer.interfaces

import android.content.Context
import com.calpito.mediaplayer.model.MusicPlayerData
import com.calpito.mediaplayer.model.Song
import kotlinx.coroutines.flow.StateFlow

interface MusicPlayerInterface {
    fun playCurrent()
    fun stop()
    fun next()
    fun pause()

    fun getAllSongs(): List<Song>

    fun getMusicPlayerState(): StateFlow<MusicPlayerData>

    fun setSongList(songs: List<Song>)

    fun nextPlayBackMode()

    fun setCurrentSong(song: Song)

    fun setShuffleMode()

    fun previousTrack()
    fun setRepeatAllMode()

    fun setRepeatOneMode()
}