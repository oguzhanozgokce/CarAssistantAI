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
                command.contains("ara", true) -> {
                    val query = command.substringBefore("ara").trim()
                    Log.d(TAG, "Search query: $query")
                    if (query.isNotEmpty()) {
                        fragment.openGoogleSearch(query)
                    } else {
                        fragment.sendBotMessage("Aramak istediğiniz yeri belirtmediniz.")
                    }
                }
                else -> {
                    fragment.sendBotMessage("Üzgünüm, komutunuzu anlayamadım.")
                }
            }
        }, 1000) // 1 saniye gecikme
    }
}