package com.oguzhanozgokce.carassistantai.ui.chat.utils

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import com.oguzhanozgokce.carassistantai.R
import com.oguzhanozgokce.carassistantai.data.model.json.Command
import com.oguzhanozgokce.carassistantai.ui.chat.view.ChatBotFragment
import java.util.Locale

class CommandProcessor(private val fragment: ChatBotFragment) {

    private val gson = Gson()
    companion object {
        private const val TAG = "CommandProcessor"
    }

    fun processCommand(response: String) {
        try {
            Log.d(TAG, "Received Response: $response")

            // Check if the response is a valid JSON
            val strippedResponse = response.trim().removeSurrounding("```json", "```").trim()

            val commandType = object : TypeToken<Command>() {}.type
            val command = gson.fromJson<Command>(strippedResponse, commandType)
            handleCommand(command)
            Log.d(TAG, "Command: $command")
        } catch (e: JsonSyntaxException) {
            // If it's not a valid JSON, treat it as a plain text response
            fragment.sendBotMessage(response)
        } catch (e: Exception) {
            fragment.hideLoadingAnimation()
            fragment.sendBotMessage("An error occurred: ${e.message}")
        } finally {
            fragment.hideLoadingAnimation()
        }
    }

    private fun handleCommand(command: Command) {
        when (command.type) {
            "open" -> {
                when (command.target.lowercase(Locale.getDefault())) {
                    "youtube" -> {
                        val query = command.parameters.query
                        if (!query.isNullOrEmpty()) {
                            Log.d(TAG, "YouTube query: $query")
                            fragment.searchAndOpenYouTube(query)
                        } else {
                            fragment.sendBotMessage(fragment.getString(R.string.no_query_specified_for_youtube))
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
                        fragment.sendBotMessage(fragment.getString(R.string.no_destination_specified))
                    }
                }
            }
            "search" -> {
                val query = command.parameters.query
                if (!query.isNullOrEmpty()) {
                    Log.d(TAG, "Search query: $query")
                    fragment.openGoogleSearch(query)
                } else {
                    fragment.sendBotMessage(fragment.getString(R.string.no_search_query_specified))
                }
            }
            "call" -> {
                val contactName = command.parameters.contactName
                Log.d(TAG, "Contact name: $contactName")
                if (!contactName.isNullOrEmpty()) {
                    fragment.findContactAndCall(contactName)
                } else {
                    fragment.sendBotMessage(fragment.getString(R.string.no_contact_specified))
                }
            }
            "message" -> {
                val contactName = command.parameters.contactName
                val message = command.parameters.message
                if (!contactName.isNullOrEmpty() && !message.isNullOrEmpty()) {
                    fragment.findContactAndSendMessage(contactName, message)
                } else {
                    fragment.sendBotMessage(fragment.getString(R.string.insufficient_message_info))
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
                        fragment.sendBotMessage("${fragment.getString(R.string.alarm_set_for)} $time")
                    } else {
                        fragment.sendBotMessage(fragment.getString(R.string.invalid_alarm_time_format))
                    }
                } else {
                    fragment.sendBotMessage(fragment.getString(R.string.could_not_understand_alarm_time))
                }
            }
            "unknown" -> {
                fragment.sendToGeminiAndProcessCommand(command.toString()) { response ->
                    processCommand(response)
                }
            }
            else -> {
                fragment.sendBotMessage(fragment.getString(R.string.command_not_recognized))
            }
        }
    }

}