package com.calpito.mediaplayer.model


data class MusicPlayerData(
    var playbackMode: PlaybackMode = PlaybackMode.REPEAT_ALL,
    var currentSong: Song? = null,
    val songList: MutableList<Song> = mutableListOf<Song>(),
    val songIndexMap: HashMap<Song, Int> = hashMapOf(),
    val playedSongs: MutableSet<Song> = mutableSetOf<Song>(),
    var state: MusicPlayerState = MusicPlayerState.IDLE
) {
    fun deepCopy(): MusicPlayerData {
        return MusicPlayerData(
            playbackMode = this.playbackMode,
            currentSong = this.currentSong,
            songList = this.songList.toMutableList(),
            songIndexMap = HashMap(this.songIndexMap),
            playedSongs = this.playedSongs.toMutableSet(),
            state = this.state
        )
    }
}
