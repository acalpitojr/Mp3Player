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
//todo inject media player with hilt
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
        //todo call the pause functionof the media player
        musicPlayer.pause()
    }

    override fun nextTrack() {
        //todo call the next function of the media player
        musicPlayer.next()
    }

    override fun getAllSongs(): List<Song> {
        //todo get all the songs that the media player has
        var result = musicPlayer.getAllSongs()
        return result
    }

    override fun setSongList(songs: List<Song>) {
        //todo call the set list of songs of the media player
        musicPlayer.setSongList(songs)
    }

    override fun setPlaybackMode(mode: PlaybackMode) {
        //todo call the set playback mode function of the media player
    }

    /*returns the state of the musicPlayer */
    override fun getMusicPlayerState(): StateFlow<MusicPlayerData> {
        val result = musicPlayer.getMusicPlayerState()
        return result
    }

    //todo get songs from assets that way we can pull their actual file data
    override fun getSongsFromStorage(): List<Song> {
        var result = arrayListOf<Song>()

        //get songs wherever they are stored in memory
        //lets create the songs here given the res raw list
        result.add(
            Song(
                uuid = UUID.randomUUID().toString(),
                title = "clair de lune",
                durationMs = 999,
                artist = "artist",
                rawId = R.raw.clair_de_lune,
                assetsDirectory = ""
            )
        )
        result.add(
            Song(
                uuid = UUID.randomUUID().toString(),
                title = "jazz de luxe",
                durationMs = 999,
                artist = "artist",
                rawId = R.raw.jazz_de_luxe,
                assetsDirectory = ""
            )
        )
        result.add(
            Song(
                uuid = UUID.randomUUID().toString(),
                title = "la paloma",
                durationMs = 999,
                artist = "artist",
                rawId = R.raw.la_paloma,
                assetsDirectory = ""
            )
        )

        return result
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