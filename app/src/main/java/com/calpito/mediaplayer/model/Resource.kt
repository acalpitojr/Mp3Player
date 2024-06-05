package com.calpito.mediaplayer.model

/*class for representing 3 states, success, error, and loading*/
sealed class Resource<T> {
    class Success<T>(val data:T):Resource<T>()
    class Error<T>(val exception:Exception):Resource<T>()
    class Loading<T>:Resource<T>()
}