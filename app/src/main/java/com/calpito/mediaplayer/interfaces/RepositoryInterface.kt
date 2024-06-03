package com.calpito.mediaplayer.interfaces

import android.content.Context
import com.calpito.mediaplayer.model.MusicPlayerData
import com.calpito.mediaplayer.model.MusicPlayerState
import com.calpito.mediaplayer.model.PlaybackMode
import com.calpito.mediaplayer.model.Song
import kotlinx.coroutines.flow.StateFlow

interface RepositoryInterface {
    fun playCurrentSong()
    fun stopMediaPlayer()
    fun pauseMediaPlayer()
    fun nextTrack()
    fun getAllSongs(): List<Song>
    fun setSongList(songs: List<Song>)
    fun setPlaybackMode(mode: PlaybackMode)

    fun getMusicPlayerState(): StateFlow<MusicPlayerData>

    fun getSongsFromStorage(): List<Song>

    suspend fun getSongsFromAssets(): List<Song>

    fun nextPlayBackMode()

    fun setCurrentSong(song: Song)

    fun setShuffleMode()


    fun previousTrack()


    fun setRepeatAllMode()


    fun setRepeatOneMode()

    suspend fun getMp3sFromAssets():List<String>
}