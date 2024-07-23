package com.oguzhanozgokce.carassistantai.ui.chat.utils

interface CommandHandler {
    fun openLink(url: String)
    fun searchAndOpenYouTube(query: String)
    fun openGoogleMapsForDestination(destination: String)
    fun showPlacesOnGoogleMaps(placeType: String)
    fun findContactAndCall(contactName: String)
    fun findContactAndSendMessage(contactName: String, message: String)
    fun openGooglePhotos()
    fun openCamera()
    fun setAlarm(hour: Int, minute: Int, message: String)
    fun startStopwatch()
    fun startTimer(seconds: Int, message: String)
    fun openClockApp()
    fun sendNoteKeep(noteContent: String)
    fun openMailApp(email: String, subject: String, body: String)
    fun openWhatsApp()
    fun tweet(message: String)
}
