package com.oguzhanozgokce.carassistantai.ui.chat.utils

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.oguzhanozgokce.carassistantai.ui.chat.ChatBotFragment
import com.oguzhanozgokce.carassistantai.ui.chat.ChatBotFragment.Companion.TAG
import com.oguzhanozgokce.carassistantai.ui.chat.utils.alarm.AlarmUtils
import com.oguzhanozgokce.carassistantai.ui.chat.utils.alarm.TimeUtils
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

class CommandProcessor(private val fragment: ChatBotFragment) {

    fun processCommand(command: String) {
        fragment.sendBotMessage("Yes sir, your request is being fulfilled...")
        Log.d(TAG, "Received command: $command")

        Handler(Looper.getMainLooper()).postDelayed({
            when {
                command.contains("YouTube", true) && Regex("\\b(aç|ac)\\b", RegexOption.IGNORE_CASE).containsMatchIn(command) -> {
                    val regex = Regex("\\b(aç|ac)\\b", RegexOption.IGNORE_CASE)
                    val query = command.substringAfter("YouTube").split(regex).first().trim()
                    Log.d(TAG, "YouTube query: $query")
                    fragment.searchAndOpenYouTube(query)
                }

                Regex("\\b(go|götür|yol tarifi)\\b", RegexOption.IGNORE_CASE).containsMatchIn(command) -> {
                    val regex = Regex("\\b(go|götür|yol tarifi)\\b", RegexOption.IGNORE_CASE)
                    val destination = command.split(regex).first().trim()
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
                        fragment.findContactAndCall(contactName)
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
                        fragment.sendBotMessage("You didn't give me enough information to send a message.")
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
                        fragment.sendBotMessage("Photos were not found according to the specified date.")
                    }
                }
                command.contains("Photo", true) -> {
                    fragment.openGooglePhotos()
                }

                command.contains("Spotify", true) -> {
                    fragment.openSpotify()
                }

                command.contains("Spotify", true) && command.contains("ara", true) -> {
                    val query = command.substringAfter("ara").trim()
                    fragment.searchSpotify(query)
                }

                command.contains("Kamera", true) -> {
                    fragment.openCamera()
                }
                command.contains("instagram", true) -> {
                    fragment.openInstagram()
                }
                command.contains("Instagram", true) && command.contains("profile", true) -> {
                    val parts = command.split("profile", ignoreCase = true, limit = 2)
                    if (parts.size > 1) {
                        val username = parts[1].trim()
                        Log.d(TAG, "Instagram profile: $username")
                        fragment.openInstagramProfile(username)
                    } else {
                        fragment.sendBotMessage("You didn't specify the Instagram profile.")
                    }
                }
                command.contains("alarm", true) -> {
                    val time = TimeUtils.extractTimeFromCommand(command)
                    if (time != null) {
                        fragment.setAlarm(time.first, time.second, "Time to wake up!")
                        fragment.sendBotMessage("Alarm set for ${time.first}:${time.second}")
                    } else {
                        fragment.sendBotMessage("Could not understand the alarm time.")
                    }
                }
                command.contains("Saat", true) -> {
                    AlarmUtils.showAlarms(fragment.requireContext())
                }
                else -> {
                    fragment.sendOpenAiRequest(command)
                }
            }
        }, 1000) // 1 saniye gecikme
    }
}