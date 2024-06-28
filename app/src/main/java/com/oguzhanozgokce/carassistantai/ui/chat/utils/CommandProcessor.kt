package com.oguzhanozgokce.carassistantai.ui.chat.utils

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.oguzhanozgokce.carassistantai.ui.chat.ChatBotFragment
import com.oguzhanozgokce.carassistantai.ui.chat.ChatBotFragment.Companion.TAG

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
                else -> {
                    fragment.sendBotMessage("Sorry, I don't understand your command.")
                }
            }
        }, 1000) // 1 saniye gecikme
    }
}