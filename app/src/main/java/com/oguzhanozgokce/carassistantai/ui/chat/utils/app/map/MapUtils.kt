package com.oguzhanozgokce.carassistantai.ui.chat.utils.app.map


import android.content.Intent
import android.net.Uri
import android.util.Log
import com.oguzhanozgokce.carassistantai.R
import com.oguzhanozgokce.carassistantai.common.Constant.GOOGLE_MAPS_GEO_URL
import com.oguzhanozgokce.carassistantai.common.Constant.GOOGLE_MAPS_PACKAGE_NAME
import com.oguzhanozgokce.carassistantai.common.Constant.GOOGLE_MAPS_WEB_URL
import com.oguzhanozgokce.carassistantai.ui.chat.view.ChatBotFragment

object MapUtils {
    fun openGoogleMapsForDestination(fragment: ChatBotFragment, destination: String) {
        Log.e(ChatBotFragment.TAG, "Destination: $destination")
        val encodedDestination = Uri.encode(destination)
        Log.e(ChatBotFragment.TAG, "Encoded Destination: $encodedDestination")
        val gmmIntentUri = Uri.parse("$GOOGLE_MAPS_WEB_URL$encodedDestination")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
            setPackage(GOOGLE_MAPS_PACKAGE_NAME)
        }
        if (mapIntent.resolveActivity(fragment.requireContext().packageManager) != null) {
            fragment.startActivity(mapIntent)
        } else {
            fragment.sendBotMessage(fragment.getString(R.string.google_maps_not_installed))
        }
    }

    fun showPlacesOnGoogleMaps(fragment: ChatBotFragment, placeType: String) {
        val encodedPlaceType = Uri.encode(placeType)
        val gmmIntentUri = Uri.parse("$GOOGLE_MAPS_GEO_URL$encodedPlaceType")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
            setPackage(GOOGLE_MAPS_PACKAGE_NAME)
        }
        if (mapIntent.resolveActivity(fragment.requireContext().packageManager) != null) {
            fragment.startActivity(mapIntent)
        } else {
            fragment.sendBotMessage(fragment.getString(R.string.google_maps_not_installed))
        }
    }
}

//val gmmIntentUri = Uri.parse("google.navigation:q=$encodedDestination")  // creates a route
//val gmmIntentUri = Uri.parse("geo:0,0?q=$encodedDestination") //  shows location
//val gmmIntentUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$encodedDestination") // directions on the web
