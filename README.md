TODO: Move the media player to a foreground service so that we can play the music when our app is not in the foreground
TODO: In the playlist screen, add search functionality so the user can find a song

Previous Functionality: The previous button will select the song that is before the current song in our playlist.  It will not play from a list of songs that we have played in order.  When we reach the first track, previous will navigate to the end of our playlist.

Stop Button: Added a stop button.  This is different from the pause functionality where after pressing stop, if we play the same song again, the song will start from the begining.

Shuffle Functionality: Shuffle logic - In shuffle mode, when we play the next track, the player will choose a random track that is not the current track, and not a track from a list of songs we have played.  When we have played all the songs, the list of songs we have played is cleared.  This is so we play a random song until we are done playing all the songs.
                                        
