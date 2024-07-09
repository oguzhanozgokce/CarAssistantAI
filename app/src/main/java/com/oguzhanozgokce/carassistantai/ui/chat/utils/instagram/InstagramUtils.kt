package com.oguzhanozgokce.carassistantai.ui.chat.utils.instagram

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri

object InstagramUtils {

    private const val INSTAGRAM_PACKAGE_NAME = "com.instagram.android"
    private const val TAG = "InstagramUtils"

    fun openInstagram(context: Context, sendBotMessage: (String) -> Unit) {
        val pm = context.packageManager
        try {
            // Check if Instagram is installed
            pm.getPackageInfo(INSTAGRAM_PACKAGE_NAME, PackageManager.GET_ACTIVITIES)

            // Create an intent to launch Instagram
            val intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                `package` = INSTAGRAM_PACKAGE_NAME
            }

            if (intent.resolveActivity(pm) != null) {
                context.startActivity(intent)
            } else {
                sendBotMessage("An error occurred while opening the Instagram app.")
            }
        } catch (e: PackageManager.NameNotFoundException) {
            openInstagramWeb(context, sendBotMessage, null)
        } catch (e: Exception) {
            sendBotMessage("An unexpected error occurred while opening the Instagram app")
        }
    }

    private fun openInstagramWeb(context: Context, sendBotMessage: (String) -> Unit, url: String?) {
        try {
            val uri = url ?: "https://www.instagram.com"
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
            context.startActivity(webIntent)
        } catch (e: Exception) {
            sendBotMessage("An unexpected error occurred while opening the Instagram web app")
        }
    }

    fun openInstagramProfile(context: Context, username: String, sendBotMessage: (String) -> Unit) {
        try {
            // Try to open the profile in the Instagram app
            val uri = Uri.parse("http://instagram.com/_u/$username")
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                `package` = INSTAGRAM_PACKAGE_NAME
            }
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            } else {
                // If the app is not installed, open in web browser
                openInstagramWeb(context, sendBotMessage, "https://instagram.com/$username")
            }
        } catch (e: Exception) {
            sendBotMessage("An unexpected error occurred while opening the Instagram profile")
        }
    }


}