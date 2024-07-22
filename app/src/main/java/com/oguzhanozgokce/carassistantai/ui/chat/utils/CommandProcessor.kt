package com.oguzhanozgokce.carassistantai.ui.chat.utils

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import com.oguzhanozgokce.carassistantai.R
import com.oguzhanozgokce.carassistantai.data.model.json.Command
import com.oguzhanozgokce.carassistantai.ui.chat.utils.alarm.AlarmUtils.setAlarm
import com.oguzhanozgokce.carassistantai.ui.chat.utils.app.google.SearchGoogle
import com.oguzhanozgokce.carassistantai.ui.chat.view.ChatBotFragment
import java.util.Locale

class CommandProcessor(private val fragment: ChatBotFragment) {

    private val gson = Gson()

    companion object {
        private const val TAG = "CommandProcessor"
    }

    private val searchGoogle = SearchGoogle(fragment.requireContext())

    fun processCommand(response: String) {
        try {
            Log.d(TAG, "${fragment.getString(R.string.received_response)}: $response")
            val strippedResponse = response.trim().removeSurrounding("```json", "```").trim()
            // Is your response a valid JSON?
            if (isJson(strippedResponse)) {
                val commandType = object : TypeToken<Command>() {}.type
                val command = gson.fromJson<Command>(strippedResponse, commandType)
                handleCommand(command)
                Log.d(TAG, "Command: $command")
            } else {
                // If not JSON, write as string
                fragment.sendBotMessage(response)
            }
        } catch (e: JsonSyntaxException) {
            // If JSON parsing fails, evaluate to string
            fragment.sendBotMessage(response)
        } catch (e: Exception) {
            fragment.hideLoadingAnimation()
            fragment.sendBotMessage("An error occurred: ${e.message}")
        } finally {
            fragment.hideLoadingAnimation()
        }
    }

    private fun isJson(response: String): Boolean {
        return try {
            gson.fromJson(response, Any::class.java)
            true
        } catch (e: JsonSyntaxException) {
            false
        }
    }

    private fun handleCommand(command: Command) {
        when (command.type.lowercase(Locale.getDefault())) {
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
                    //"spotify" -> fragment.openSpotify()
                    "instagram" -> fragment.openInstagram()
                    "twitter" -> fragment.openTwitter()
                    "camera" -> fragment.openCamera()
                    "clock" -> fragment.openClockApp()
                    "whatsapp" -> fragment.openWhatsApp()
                    "keep" -> fragment.openKeep()
                    "stopwatch" -> fragment.startStopwatch()
                    "timer" -> {
                        val seconds = command.parameters.seconds
                        val message = command.parameters.message ?: "Timer"
                        if (seconds != null) {
                            fragment.startTimer(seconds, message)
                        } else {
                            fragment.sendBotMessage(fragment.getString(R.string.no_timer_duration_specified))
                        }
                    }

                    "save_note" -> {
                        val noteContent = command.parameters.noteContent
                        if (!noteContent.isNullOrEmpty()) {
                            fragment.sendNoteKeep(noteContent)
                        } else {
                            fragment.sendBotMessage(fragment.getString(R.string.no_note_content_specified))
                        }
                    }

                    "mail" -> {
                        val email = command.parameters.contactName
                        val subject = command.parameters.query
                        val body = command.parameters.message
                        if (!email.isNullOrEmpty() && !subject.isNullOrEmpty() && !body.isNullOrEmpty()) {
                            fragment.openMailApp(email, subject, body)
                        } else {
                            fragment.sendBotMessage(fragment.getString(R.string.invalid_command))
                        }
                    }
                    "google_search" -> {
                        val query = command.parameters.query
                        if (!query.isNullOrEmpty()) {
                            Log.d("CommandProcessor", "Querying Google for: $query")
                            searchGoogle.search(query) { firstLink ->
                                Log.d("CommandProcessor", "First link: $firstLink")
                                if (firstLink != null) {
                                    fragment.openLink(firstLink)
                                } else {
                                    fragment.sendBotMessage(fragment.getString(R.string.no_results_found))
                                }
                            }
                        } else {
                            fragment.sendBotMessage(fragment.getString(R.string.no_query_specified))
                        }
                    }
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

            "tweet" -> {
                if (command.target.lowercase(Locale.getDefault()) == "twitter" &&
                    command.action.lowercase(Locale.getDefault()) == "create_tweet"
                ) {

                    val message = command.parameters.message
                    if (!message.isNullOrEmpty()) {
                        fragment.tweet(message)
                    } else {
                        fragment.sendBotMessage(fragment.getString(R.string.no_message_specified))
                    }
                } else {
                    fragment.sendBotMessage(fragment.getString(R.string.invalid_command))
                }
            }
            /*
            "search" -> {
                val query = command.parameters.query
                if (!query.isNullOrEmpty()) {
                    Log.d(TAG, "Search query: $query")
                    fragment.openGoogleSearch(query)
                } else {
                    fragment.sendBotMessage(fragment.getString(R.string.no_search_query_specified))
                }
            }
            */
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
                val message = command.parameters.message ?: "Alarm"
                if (!time.isNullOrEmpty()) {
                    val parts = time.split(":")
                    if (parts.size == 2) {
                        val hour = parts[0].toInt()
                        val minute = parts[1].toInt()
                        fragment.setAlarm(hour, minute, message)
                        fragment.sendBotMessage("${fragment.getString(R.string.alarm_set_for)} $time")
                    } else {
                        fragment.sendBotMessage(fragment.getString(R.string.invalid_alarm_time_format))
                    }
                } else {
                    fragment.sendBotMessage(fragment.getString(R.string.could_not_understand_alarm_time))
                }
            }

            "show" -> {
                if (command.target.equals("googlemaps", ignoreCase = true)) {
                    val placeType = command.parameters.placeType
                    if (!placeType.isNullOrEmpty()) {
                        Log.d(TAG, "Place type: $placeType")
                        fragment.showPlacesOnGoogleMaps(placeType)
                    } else {
                        fragment.sendBotMessage(fragment.getString(R.string.no_place_type_specified))
                    }
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
