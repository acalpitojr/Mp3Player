package com.calpito.mediaplayer.model

data class Song(
    val uuid:String,
    val title:String,
    val durationMs:Long,
    val artist:String,
    val rawId:Int,
    val assetsDirectory:String
)
