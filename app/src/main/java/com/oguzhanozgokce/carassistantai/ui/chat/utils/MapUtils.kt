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
        val gmmIntentUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$encodedDestination")
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

    //val gmmIntentUri = Uri.parse("google.navigation:q=$encodedDestination")  // rota oluşturur
    //val gmmIntentUri = Uri.parse("geo:0,0?q=$encodedDestination") // konum gösterir
    //val gmmIntentUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$encodedDestination") // web de yol tarifi
}
