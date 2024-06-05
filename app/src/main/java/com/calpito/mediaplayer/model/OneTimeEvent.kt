package com.calpito.mediaplayer.model

/*One time events that can be used for things such as showing toast messages in the ui*/
sealed class OneTimeEvent {
    class ToastEvent(val message:String):OneTimeEvent()
}