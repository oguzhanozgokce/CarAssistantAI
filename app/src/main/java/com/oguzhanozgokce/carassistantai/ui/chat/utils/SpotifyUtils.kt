package com.oguzhanozgokce.carassistantai.ui.chat.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.oguzhanozgokce.carassistantai.data.model.Message

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
                sendBotMessage("Spotify uygulaması açılırken bir hata oluştu.")
            }
        } catch (e: PackageManager.NameNotFoundException) {
            sendBotMessage("Spotify uygulaması yüklü değil.")
        }
    }
}
