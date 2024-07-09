package com.oguzhanozgokce.carassistantai.ui.chat.utils.spotify

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log

object SpotifyUtils {

    fun openSpotify(context: Context, sendBotMessage: (String) -> Unit) {
        val pm = context.packageManager
        try {
            // Check if Spotify is installed
            pm.getPackageInfo("com.spotify.music", PackageManager.GET_ACTIVITIES)

            // Create an intent to launch Spotify
            val intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                `package` = "com.spotify.music"
            }

            if (intent.resolveActivity(pm) != null) {
                context.startActivity(intent)
            } else {
                sendBotMessage("An error occurred while opening the Spotify app.")
            }
        } catch (e: PackageManager.NameNotFoundException) {
            openSpotifyWeb(context, sendBotMessage)
        } catch (e: Exception) {
            sendBotMessage("An unexpected error occurred")
        }
    }

    private fun openSpotifyWeb(context: Context, sendBotMessage: (String) -> Unit) {
        try {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://open.spotify.com"))
            context.startActivity(webIntent)
        } catch (e: Exception) {
            sendBotMessage("An unexpected error occurred while opening the Spotify web app")
        }
    }

    fun searchSpotify(context: Context, query: String, sendBotMessage: (String) -> Unit) {
        try {
            // Create an intent to search Spotify
            val formattedQuery = query.replace(" ", "%20")
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://open.spotify.com/search/$formattedQuery")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }

            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            } else {
                sendBotMessage("An error occurred while searching in the Spotify app.")
                openSpotifyWeb(context, sendBotMessage)
            }
        } catch (e: Exception) {
            Log.e("SpotifyUtils", "Unexpected error", e)
        }
    }
}
