package com.oguzhanozgokce.carassistantai.ui.chat.utils

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.oguzhanozgokce.carassistantai.ui.chat.ChatBotFragment
import com.oguzhanozgokce.carassistantai.ui.chat.ChatBotFragment.Companion.TAG
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

class CommandProcessor(private val fragment: ChatBotFragment) {

    fun processCommand(command: String) {
        fragment.sendBotMessage("Yes sir, your request is being fulfilled...")
        Log.d(TAG, "Received command: $command")

        Handler(Looper.getMainLooper()).postDelayed({
            when {
                command.contains("YouTube", true) && command.contains("ac", true) -> {
                    val query = command.substringAfter("YouTube").substringBefore(" ac").trim()
                    Log.d(TAG, "YouTube query: $query")
                    fragment.searchAndOpenYouTube(query)
                }

                command.contains("go", true) -> {
                    val destination = command.substringBefore("go").trim()
                    Log.d(TAG, "Destination after processing: $destination")
                    if (destination.isNotEmpty()) {
                        fragment.openGoogleMapsForDestination(destination)
                    } else {
                        fragment.sendBotMessage("You didn't specify your destination.")
                    }
                }
                command.contains("search", true) -> {   // search , query
                    val query = command.substringBefore("search").trim()
                    Log.d(TAG, "Search query: $query")
                    if (query.isNotEmpty()) {
                        fragment.openGoogleSearch(query)
                    } else {
                        fragment.sendBotMessage("You didn't specify where you wanted to call.")
                    }
                }
                command.startsWith("Rehberden") && command.contains("ara", true) -> {       // Rehberden , name , ara
                    val contactName = command.substringAfter("Rehberden").substringBefore("ara").trim()
                    if (contactName.isNotEmpty()) {
                        ContactUtils.findContactAndCall(fragment, contactName)
                    } else {
                        fragment.sendBotMessage("You didn't specify who you wanted to call.")
                    }
                }
                command.contains("mesaj", true) -> {  // message , name , message_body
                    val parts = command.split(" ")
                    if (parts.size >= 4) {
                        val contactName = parts[1]
                        val message = parts.subList(2, parts.size).joinToString(" ")
                        fragment.findContactAndSendMessage(contactName, message)
                    } else {
                        fragment.sendBotMessage("Mesaj göndermek için yeterli bilgi vermediniz.")
                    }

                }
                command.contains("fotoğraflar", true) -> {
                    val dateRegex = """\d{1,2} \w+ \d{4}""".toRegex()
                    val matchResult = dateRegex.find(command)
                    if (matchResult != null) {
                        val dateStr = matchResult.value
                        val sdf = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
                        val startDate = sdf.parse(dateStr)?.time ?: 0L
                        val endDate = startDate + TimeUnit.DAYS.toMillis(1) - 1

                        fragment.getPhotosByDateRange(startDate, endDate)
                    } else {
                        fragment.sendBotMessage("Belirtilen tarihe göre fotoğraflar bulunamadı.")
                    }
                }
                command.contains("Photo", true) -> {
                    fragment.openGooglePhotos()
                }

                command.contains("Spotify", true) -> {
                    fragment.openSpotify()
                }

                command.contains("Kamera", true) -> {
                    fragment.openCamera()
                }
                else -> {
                    fragment.sendOpenAiRequest(command)
                }
            }
        }, 1000) // 1 saniye gecikme
    }
}