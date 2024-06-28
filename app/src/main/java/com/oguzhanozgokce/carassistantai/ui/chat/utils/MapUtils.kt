package com.oguzhanozgokce.carassistantai.ui.chat.utils

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.fragment.app.Fragment
import com.oguzhanozgokce.carassistantai.ui.chat.ChatBotFragment

object MapUtils {

    fun openGoogleMapsForDestination(fragment: Fragment, destination: String) {
        Log.e(ChatBotFragment.TAG, "Destination: $destination")
        val encodedDestination = Uri.encode(destination)
        Log.e(ChatBotFragment.TAG, "Encoded Destination: $encodedDestination")
        val gmmIntentUri = Uri.parse("google.navigation:q=$encodedDestination")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
            setPackage("com.google.android.apps.maps")
        }
        if (mapIntent.resolveActivity(fragment.requireActivity().packageManager) != null) {
            fragment.startActivity(mapIntent)
        } else {
            val webIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$encodedDestination")
            )
            fragment.startActivity(webIntent)
        }
    }

    fun openGoogleMapsForSearch(fragment: Fragment, query: String) {
        Log.e(ChatBotFragment.TAG, "Search query: $query")
        val encodedQuery = Uri.encode(query)
        Log.e(ChatBotFragment.TAG, "Encoded Search Query: $encodedQuery")
        val gmmIntentUri = Uri.parse("geo:0,0?q=$encodedQuery")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
            setPackage("com.google.android.apps.maps")
        }
        if (mapIntent.resolveActivity(fragment.requireActivity().packageManager) != null) {
            fragment.startActivity(mapIntent)
        } else {
            val webIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://www.google.com/maps/search/?api=1&query=$encodedQuery")
            )
            fragment.startActivity(webIntent)
        }
    }
}
