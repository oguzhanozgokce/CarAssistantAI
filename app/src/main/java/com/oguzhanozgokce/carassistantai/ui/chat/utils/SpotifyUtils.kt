package com.oguzhanozgokce.carassistantai.ui.chat.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager

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
            sendBotMessage("Spotify app not installed")
        }
    }
}
