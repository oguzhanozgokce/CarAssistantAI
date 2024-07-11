package com.oguzhanozgokce.carassistantai.ui.chat.utils

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.oguzhanozgokce.carassistantai.data.model.json.Command
import com.oguzhanozgokce.carassistantai.data.model.json.Parameters
import com.oguzhanozgokce.carassistantai.ui.chat.view.ChatBotFragment
import com.oguzhanozgokce.carassistantai.ui.chat.view.ChatBotFragment.Companion.TAG
import java.util.Locale

class CommandProcessor(private val fragment: ChatBotFragment) {

    private val gson = Gson()

    fun processCommand(userInput: String) {
        fragment.showLoadingAnimation()
        Log.d(TAG, "Received command: $userInput")

        val commandJson = convertToCommandJson(userInput)
        val commandType = object : TypeToken<Command>() {}.type
        val command = gson.fromJson<Command>(commandJson, commandType)
        Log.d(TAG, "Command JSON: $commandJson")

        Handler(Looper.getMainLooper()).postDelayed({
            try {
                handleCommand(command, userInput)
            } finally {
                fragment.hideLoadingAnimation()
            }
        }, 1000) // 1 saniye gecikme
    }

    private fun convertToCommandJson(userInput: String): String {
        val lowerCaseInput = userInput.lowercase(Locale.getDefault())
        return when {
            lowerCaseInput.contains("youtube") && lowerCaseInput.contains("aç") -> {
                val query = userInput.substringAfter("YouTube", "").substringBefore("aç").trim()
                createCommandJson("open", "YouTube", "search", Parameters(query = query))
            }
            lowerCaseInput.contains("götür") -> {
                val destination = userInput.substringBefore("götür", "").trim()
                createCommandJson("navigate", "GoogleMaps", "route", Parameters(destination = destination))
            }
            lowerCaseInput.contains("ara") && lowerCaseInput.contains("rehberden") -> {
                val contactName = userInput.substringAfter("Rehberden", "").substringBefore("ara").trim()
                createCommandJson("call", "Contacts", "call", Parameters(contactName = contactName))
            }
            lowerCaseInput.contains("mesaj") -> {
                val parts = userInput.split(" ")
                if (parts.size >= 4) {
                    val contactName = parts[1]
                    val message = parts.subList(2, parts.size).joinToString(" ")
                    createCommandJson("message", "Contacts", "send", Parameters(contactName = contactName, message = message))
                } else {
                    createCommandJson("error", "", "", Parameters())
                }
            }
            lowerCaseInput.contains("fotoğraflar") -> {
                createCommandJson("open", "Gallery", "open", Parameters())
            }
            lowerCaseInput.contains("alarm") -> {
                val time = userInput.substringBefore("alarm", "").trim()
                createCommandJson("alarm", "Clock", "set", Parameters(time = time))
            }
            lowerCaseInput.contains("kamera") -> {
                createCommandJson("open", "Camera", "open", Parameters())
            }
            lowerCaseInput.contains("spotify") -> {
                createCommandJson("open", "Spotify", "open", Parameters())
            }
            lowerCaseInput.contains("Instagram") -> {
                createCommandJson("open", "Instagram", "open", Parameters())
            }
            lowerCaseInput.contains("saat") -> {
                createCommandJson("open", "Clock", "open", Parameters())
            }
            lowerCaseInput.contains("search") -> {
                val query = userInput.substringBefore("search").trim()
                createCommandJson("search", "Google", "search", Parameters(query = query))
            }
            else -> createCommandJson("unknown", "Gemini", "respond", Parameters())
        }
    }

    private fun createCommandJson(type: String, target: String, action: String, parameters: Parameters): String {
        val command = Command(type, target, action, parameters)
        return gson.toJson(command)
    }

    private fun handleCommand(command: Command, originalUserInput: String) {
        when (command.type) {
            "open" -> {
                when (command.target.lowercase(Locale.getDefault())) {
                    "youtube" -> {
                        val query = command.parameters.query
                        if (!query.isNullOrEmpty()) {
                            Log.d(TAG, "YouTube query: $query")
                            fragment.searchAndOpenYouTube(query)
                        } else {
                            fragment.sendBotMessage("No query specified for YouTube search.")
                        }
                    }
                    "gallery" -> fragment.openGooglePhotos()
                    "spotify" -> fragment.openSpotify()
                    "instagram" -> fragment.openInstagram()
                    "camera" -> fragment.openCamera()
                    "clock" -> fragment.openClockApp()
                }
            }
            "navigate" -> {
                if (command.target.equals("googlemaps", ignoreCase = true)) {
                    val destination = command.parameters.destination
                    if (!destination.isNullOrEmpty()) {
                        Log.d(TAG, "Destination after processing: $destination")
                        fragment.openGoogleMapsForDestination(destination)
                    } else {
                        fragment.sendBotMessage("You didn't specify your destination.")
                    }
                }
            }
            "search" -> {
                val query = command.parameters.query
                if (!query.isNullOrEmpty()) {
                    Log.d(TAG, "Search query: $query")
                    fragment.openGoogleSearch(query)
                } else {
                    fragment.sendBotMessage("You didn't specify where you wanted to call.")
                }
            }
            "call" -> {
                val contactName = command.parameters.contactName
                if (!contactName.isNullOrEmpty()) {
                    fragment.findContactAndCall(contactName)
                } else {
                    fragment.sendBotMessage("You didn't specify who you wanted to call.")
                }
            }
            "message" -> {
                val contactName = command.parameters.contactName
                val message = command.parameters.message
                if (!contactName.isNullOrEmpty() && !message.isNullOrEmpty()) {
                    fragment.findContactAndSendMessage(contactName, message)
                } else {
                    fragment.sendBotMessage("You didn't give me enough information to send a message.")
                }
            }
            "alarm" -> {
                val time = command.parameters.time
                if (!time.isNullOrEmpty()) {
                    val parts = time.split(":")
                    if (parts.size == 2) {
                        val hour = parts[0].toInt()
                        val minute = parts[1].toInt()
                        fragment.setAlarm(hour, minute, "Time to wake up!")
                        fragment.sendBotMessage("Alarm set for $time")
                    } else {
                        fragment.sendBotMessage("Invalid time format for alarm.")
                    }
                } else {
                    fragment.sendBotMessage("Could not understand the alarm time.")
                }
            }
            "instagramProfile" -> {
                val username = command.parameters.username
                if (!username.isNullOrEmpty()) {
                    Log.d(TAG, "Instagram profile: $username")
                    fragment.openInstagramProfile(username)
                } else {
                    fragment.sendBotMessage("You didn't specify the Instagram profile.")
                }
            }
            "unknown" -> {
                fragment.sendGeminiResponse(originalUserInput)
            }
            else -> {
                fragment.sendBotMessage("Command not recognized.")
            }
        }
    }
}
