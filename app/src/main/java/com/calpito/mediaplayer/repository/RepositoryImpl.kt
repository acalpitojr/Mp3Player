package com.calpito.mediaplayer.repository

import android.content.Context
import android.media.MediaMetadataRetriever
import android.provider.SyncStateContract
import com.calpito.mediaplayer.Constants
import com.calpito.mediaplayer.R
import com.calpito.mediaplayer.interfaces.MusicPlayerInterface
import com.calpito.mediaplayer.interfaces.RepositoryInterface
import com.calpito.mediaplayer.model.MusicPlayerData
import com.calpito.mediaplayer.model.PlaybackMode
import com.calpito.mediaplayer.model.Song
import com.calpito.mediaplayer.utility.HelperFunctions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.util.UUID
import javax.inject.Inject

//todo in order to make songs play in the background, create a media player service
class RepositoryImpl @Inject constructor(
    val context: Context,
    val musicPlayer: MusicPlayerInterface,
) : RepositoryInterface {



    override fun playCurrentSong() {
        musicPlayer.playCurrent()
    }

    override fun stopMediaPlayer() {
        musicPlayer.stop()
    }

    override fun pauseMediaPlayer() {
        musicPlayer.pause()
    }

    override fun nextTrack() {
        musicPlayer.next()
    }

    override fun getAllSongs(): List<Song> {
        var result = musicPlayer.getAllSongs()
        return result
    }

    override fun setSongList(songs: List<Song>) {
        musicPlayer.setSongList(songs)
    }

    override fun setPlaybackMode(mode: PlaybackMode) {
        //todo method to specifically set playback mode
    }

    /*returns the state of the musicPlayer */
    override fun getMusicPlayerState(): StateFlow<MusicPlayerData> {
        val result = musicPlayer.getMusicPlayerState()
        return result
    }

    /*todo other method of grabbing songs*/
    override fun getSongsFromStorage(): List<Song> {
        return emptyList()
    }


    override fun nextPlayBackMode() {
        musicPlayer.nextPlayBackMode()
    }

    override fun setCurrentSong(song: Song) {
        musicPlayer.setCurrentSong(song)
    }

    override fun setShuffleMode() {
        musicPlayer.setShuffleMode()
    }

    override fun previousTrack() {
        musicPlayer.previousTrack()
    }

    override fun setRepeatAllMode() {
        musicPlayer.setRepeatAllMode()
    }

    override fun setRepeatOneMode() {
        musicPlayer.setRepeatOneMode()
    }

    /*Pulls a list of all mp3s from the given assets directory, and returns their paths.
    * Used so we can open actual mp3s for mediaplyer*/
    override suspend fun getMp3sFromAssets(): List<String> {
        val assetManager = context.assets
        val mp3Files = mutableListOf<String>()

        try {
            // List all files in the 'songs' directory
            val files = assetManager.list(Constants.SONGS_DIRECTORY)
            if (files != null) {
                for (file in files) {
                    // Check if the file has a .mp3 extension
                    if (file.endsWith(".mp3")) {
                        mp3Files.add("${Constants.SONGS_PATH}$file")
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            mp3Files.clear()
        }

        return mp3Files
    }

    /*Creates Songs from mp3 files stored in assets*/
    override suspend fun getSongsFromAssets(): List<Song> {
        var result = arrayListOf<Song>()

        withContext(Dispatchers.IO) {
            try {
                val mp3s = getMp3sFromAssets()
                for (mp3 in mp3s) {
                    val song = songFromMp3File(context, mp3)
                    song?.let {
                        result.add(song)
                    }
                }
            } catch (e: Exception) {
                result.clear()
            }
        }

        return result
    }

    /*Grabs the given mp3 file, and creates a song object with it.*/
    private fun songFromMp3File(context: Context, filePath: String): Song? {
        var result: Song? = null

        // Check if the file has a .mp3 extension
        if (filePath.endsWith(".mp3")) {
            //create a song
            val assetManager = context.assets
            val metadataRetriever = MediaMetadataRetriever()
            try {
                val fd = assetManager.openFd(filePath)
                metadataRetriever.setDataSource(fd.fileDescriptor, fd.startOffset, fd.length)
                val title =
                    metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                        ?: ""
                val artist =
                    metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
                        ?: ""
                val album =
                    metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
                        ?: ""
                val durationMsString =
                    metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                        ?: "0"
                val durationMs = durationMsString?.toLongOrNull() ?: 0L


                println("MP3 File: $filePath")
                println("Title: ${title ?: "Unknown"}")
                println("Artist: ${artist ?: "Unknown"}")
                println("Album: ${album ?: "Unknown"}")
                val song = Song(
                    uuid = UUID.randomUUID().toString(),
                    title = title,
                    durationMs = durationMs,
                    artist = artist,
                    rawId = 0,
                    assetsDirectory = filePath
                )
                result = song
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                metadataRetriever.release()
            }


        }

        return result
    }

}