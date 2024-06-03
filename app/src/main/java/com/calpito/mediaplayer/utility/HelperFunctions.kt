package com.calpito.mediaplayer.utility

import android.content.Context
import android.media.MediaMetadataRetriever
import com.calpito.mediaplayer.model.Song
import java.io.File
import java.io.IOException
import java.util.UUID

object HelperFunctions {
    fun convertMillisToMMSS(millis: Long): String {
        val minutes = millis / 1000 / 60
        val seconds = (millis / 1000 % 60)
        return String.format("%02d:%02d", minutes, seconds)
    }


}