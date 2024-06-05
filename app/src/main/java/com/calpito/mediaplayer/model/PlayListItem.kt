package com.calpito.mediaplayer.model

/*different items we can display in our playlist*/
sealed class PlayListItem {
    class SongItem(val song: Song):PlayListItem()
    class CurrentSongItem(val song:Song):PlayListItem()
}