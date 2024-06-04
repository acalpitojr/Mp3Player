package com.calpito.mediaplayer

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import com.calpito.mediaplayer.interfaces.MusicPlayerInterface
import com.calpito.mediaplayer.model.MusicPlayerData
import com.calpito.mediaplayer.model.MusicPlayerState
import com.calpito.mediaplayer.model.PlaybackMode
import com.calpito.mediaplayer.model.Song
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

import java.util.Random
import javax.inject.Inject

class MusicPlayerImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : MusicPlayerInterface {
    private var mediaPlayer: MediaPlayer? = null

    //encapsulate all data so its easier to expose as a StateFlow
    private val musicPlayerData = MusicPlayerData()

    //StateFlow so app can react to music player data
    private val _state = MutableStateFlow(musicPlayerData.deepCopy())
    val state: StateFlow<MusicPlayerData> = _state

    override fun getMusicPlayerState(): StateFlow<MusicPlayerData> {
        var result = state
        return result
    }


    /*Load the list of songs the music player can play*/
    override fun setSongList(songs: List<Song>) {
        mediaPlayer?.release()
        musicPlayerData.apply {
            songList.clear()
            songList.addAll(songs)
            songList.forEachIndexed { index, song ->
                songIndexMap.put(song, index)
            }
            currentSong = null
        }
        updateStateFlow(musicPlayerData)
    }

    override fun getAllSongs(): List<Song> {
        val result = musicPlayerData.songList
        return result
    }

    //Make sure a song is loaded, and plays the song
    override fun playCurrent() {
        //choose first song if we do not have a song yet
        if (musicPlayerData.songList.isNotEmpty()) {

            //make sure we have a current song to play
            if (musicPlayerData.currentSong == null) {
                musicPlayerData.apply {
                    currentSong = songList.first()
                }
            }

            /*mark songs that we have started playing to the list of songs we have played*/
            musicPlayerData.currentSong?.let {
                musicPlayerData.playedSongs.add(it)
            }

            //different play behavior based on the music players state
            when (musicPlayerData.state) {
                //if we are paused, just play the current song again
                MusicPlayerState.PAUSED -> {
                    mediaPlayer?.start()
                    musicPlayerData.state = MusicPlayerState.PLAYING
                    updateStateFlow(musicPlayerData)
                }

                MusicPlayerState.PLAYING ->{
                    //already playing, play button does nothing
                }

                else -> {
                    try {//start playing from the beginning
                        val assetManager = context.assets
                        val assetFileDescriptor =
                            assetManager.openFd(musicPlayerData.currentSong?.assetsDirectory ?: "")
                        mediaPlayer?.release()
                        mediaPlayer = MediaPlayer().apply {
                            setDataSource(assetFileDescriptor.fileDescriptor, assetFileDescriptor.startOffset, assetFileDescriptor.length)

                            /*when music player is ready, start playing*/
                            setOnPreparedListener {
                                it.start()
                                musicPlayerData.state = MusicPlayerState.PLAYING
                                updateStateFlow(musicPlayerData)
                            }

                            /*when we are done playing a song*/
                            setOnCompletionListener {
                                musicPlayerData.state = MusicPlayerState.STOPPED
                                when (musicPlayerData.playbackMode) {
                                    PlaybackMode.REPEAT_ALL -> {
                                        //play next song.  if we are the last song, play the first song
                                        next()
                                    }

                                    PlaybackMode.SHUFFLE -> {
                                        //play a random song.
                                        //lets not play the same song 2 times in a row unless there is only 1 song in the list
                                        musicPlayerData.apply {
                                            currentSong = getRandomSong(musicPlayerData)
                                        }
                                        playCurrent()
                                    }

                                    PlaybackMode.REPEAT_ONE -> {
                                        //play the same song again
                                        playCurrent()
                                    }
                                }
                            }

                            /*prepare the mediaPlayer and call onPreparedListener when it is ready*/
                            prepareAsync()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        musicPlayerData.state = MusicPlayerState.IDLE
                        updateStateFlow(musicPlayerData)
                    }
                }
            }


        } else {
            //nothing to play!!
            musicPlayerData.state = MusicPlayerState.IDLE
            updateStateFlow(musicPlayerData)
        }

    }


    override fun stop() {
        mediaPlayer?.let {
            mediaPlayer?.stop()
            musicPlayerData.state = MusicPlayerState.STOPPED
            updateStateFlow(musicPlayerData)
        }
    }

    /*play the next song in our list of songs.  If we played the last song in our list, play the first song*/
    override fun next() {
        musicPlayerData.state = MusicPlayerState.IDLE //idle as we change to the next track

        //play the next song in the list of songs
        when (musicPlayerData.playbackMode) {
            //shuffle mode we will choose a random song.
            PlaybackMode.SHUFFLE -> {
                //play a random song.
                //lets not play the same song 2 times in a row unless there is only 1 song in the list
                musicPlayerData.apply {
                    currentSong = getRandomSong(musicPlayerData)
                }


            }

            //Any other mode we have logic to choose the next track in the list
            else -> {
                musicPlayerData.apply {


                    when (this.songList.size) {
                        0 -> {
                            //there are no songs to play
                            currentSong = null
                            state = MusicPlayerState.IDLE
                        }

                        1 -> {
                            //play only song
                            currentSong = songList.first()
                        }

                        else -> {
                            //play the next song by incrementing the songs index
                            if (currentSong == null) { //current song isnt set, choose the first song
                                currentSong = songList.first()
                            } else {
                                var currentIndex = songIndexMap[currentSong]
                                currentIndex?.let {
                                    var nextIndex = it + 1
                                    //loop back to the beginning if we have reached the end
                                    if (nextIndex > songList.lastIndex) {
                                        nextIndex = 0
                                        playedSongs.clear()
                                    }
                                    currentSong = songList[nextIndex]
                                }

                            }

                        }
                    }
                }
            }
        }



        updateStateFlow(musicPlayerData)
        playCurrent()
    }

    override fun pause() {

        mediaPlayer?.let {
            when (musicPlayerData.state) {
                MusicPlayerState.IDLE -> {
                    //pause does nothing
                }

                MusicPlayerState.PLAYING -> {
                    it.pause()
                    musicPlayerData.state = MusicPlayerState.PAUSED
                    updateStateFlow(musicPlayerData)
                }

                MusicPlayerState.PAUSED -> {
                    //cant pause if alreay paused
                }

                MusicPlayerState.STOPPED -> {
                    //pause does nothing if we are stopped
                }
            }

        }

    }

    /*change play back mode. Clear the played songs list*/
    fun setPlayBackMode(mode: PlaybackMode) {
        musicPlayerData.apply {
            playedSongs.clear()
            playbackMode = mode
        }

        updateStateFlow(musicPlayerData)
    }

    /*update StateFlow for any collectors*/
    private fun updateStateFlow(musicPlayerData: MusicPlayerData) {
        val deepCopy = musicPlayerData.deepCopy()
        _state.value = deepCopy
    }


    /*will return a random songResId, ensuring that every song is played before a song can be played again.*/
    private fun getRandomSong(musicPlayerData: MusicPlayerData): Song? {
        var result: Song? = null //default

        when {
            //we can shuffle 2 or more songs
            musicPlayerData.songList.size > 1 -> {
                if (musicPlayerData.playedSongs.size == musicPlayerData.songList.size) {
                    //all songs have been played, clear the list
                    musicPlayerData.playedSongs.clear()
                }


                //filter out songs that we can play, songs that are not the current song, and have not been played
                val unplayedSongs = musicPlayerData.songList.filter { song ->
                    (song != musicPlayerData.currentSong
                            && musicPlayerData.playedSongs.isNotEmpty()
                            && !musicPlayerData.playedSongs.contains(song))
                            || (song != musicPlayerData.currentSong && musicPlayerData.playedSongs.isEmpty())
                }

                //choose a random song from this list
                result = unplayedSongs.random()

            }

            //only 1 song, just return that song
            musicPlayerData.songList.size == 1 -> {
                result = musicPlayerData.songList[0]
            }

            //no songs to return
            else -> {
                result = null
            }

        }

        return result
    }

    override fun nextPlayBackMode() {
        musicPlayerData.playbackMode =
            when (musicPlayerData.playbackMode) {
                PlaybackMode.SHUFFLE -> {
                    PlaybackMode.REPEAT_ALL
                }

                PlaybackMode.REPEAT_ALL -> {
                    PlaybackMode.REPEAT_ONE
                }

                PlaybackMode.REPEAT_ONE -> {
                    PlaybackMode.SHUFFLE
                }
            }

        updateStateFlow(musicPlayerData)
    }

    override fun setCurrentSong(song: Song) {
        //play the next song in the list of songs
        //first verify that we have this song in the list
        val songIndex = musicPlayerData.songIndexMap[song]
        if (songIndex != null) {
            musicPlayerData.apply {
                state = MusicPlayerState.IDLE //idle as we change to the next track
                currentSong = song
            }
            updateStateFlow(musicPlayerData)
            playCurrent()
        } else {
            //song doesnt exist, we cannot play it
        }
    }

    override fun setShuffleMode() {
        musicPlayerData.playbackMode = PlaybackMode.SHUFFLE
        updateStateFlow(musicPlayerData)
    }

    /*Previous Track will traverse the list of songs in order, it will not keep track of songs that were played, and instead will be used
    * to navigate to the song prior to the current song.
    *
    * The alternative is we keep track of all songs we have played in order, but what logic would be use to ensure this list isnt infinite,
    * possibly a linked list?*/
    override fun previousTrack() {
        //play the next song in the list of songs
        musicPlayerData.apply {
            state = MusicPlayerState.IDLE //idle as we change to the next track

            when (this.songList.size) {
                0 -> {
                    //there are no songs to play
                    currentSong = null
                    state = MusicPlayerState.IDLE
                }

                1 -> {
                    //play only song
                    currentSong = songList.first()
                }

                else -> {
                    //play the next song by decrementing the songs index
                    if (currentSong == null) { //current song isnt set, choose the first song
                        currentSong = songList.first()
                    } else {
                        var currentIndex = songIndexMap[currentSong]
                        currentIndex?.let {
                            var nextIndex = it - 1
                            //loop back to the beginning if we have reached the end
                            if (nextIndex < 0) {
                                nextIndex = songList.lastIndex
                                playedSongs.clear()
                            }
                            currentSong = songList[nextIndex]
                        }

                    }

                }
            }
        }

        updateStateFlow(musicPlayerData)
        playCurrent()
    }

    override fun setRepeatAllMode() {
        musicPlayerData.playbackMode = PlaybackMode.REPEAT_ALL
        updateStateFlow(musicPlayerData)
    }

    override fun setRepeatOneMode() {
        musicPlayerData.playbackMode = PlaybackMode.REPEAT_ONE
        updateStateFlow(musicPlayerData)
    }


}
