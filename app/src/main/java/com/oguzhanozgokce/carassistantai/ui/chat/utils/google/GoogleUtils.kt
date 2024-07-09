package com.oguzhanozgokce.carassistantai.ui.chat.utils.google

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log

object GoogleUtils {
    private const val TAG = "GoogleUtils"

    fun openGoogleSearch(context: Context, query: String, onFailure: (String) -> Unit) {
        val encodedQuery = Uri.encode(query)
        val googleSearchUri = Uri.parse("https://www.google.com/search?q=$encodedQuery")
        val webIntent = Intent(Intent.ACTION_VIEW, googleSearchUri)

        Log.d(TAG, "Google Search URI: $googleSearchUri")
        Log.d(TAG, "Web Intent: $webIntent")

        try {
            context.startActivity(webIntent)
            Log.d(TAG, "Web intent started successfully")
        } catch (e: Exception) {
            onFailure("Google Search application not found")
            Log.e(TAG, "Google Search application not found", e)

            // Alternatively, you can use a dialogue to show a warning message to the user.
            val fallbackUri = Uri.parse("https://www.google.com/search?q=$encodedQuery")
            val fallbackIntent = Intent(Intent.ACTION_VIEW, fallbackUri)
            context.startActivity(fallbackIntent)
        }
    }
}