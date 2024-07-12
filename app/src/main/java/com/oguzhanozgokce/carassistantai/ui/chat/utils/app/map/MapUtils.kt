package com.oguzhanozgokce.carassistantai.ui.chat.utils.app.map


import android.net.Uri
import android.util.Log
import com.oguzhanozgokce.carassistantai.R
import com.oguzhanozgokce.carassistantai.common.Constant.GOOGLE_MAPS_PACKAGE_NAME
import com.oguzhanozgokce.carassistantai.common.Constant.GOOGLE_MAPS_WEB_URL
import com.oguzhanozgokce.carassistantai.ui.chat.utils.app.AppUtils
import com.oguzhanozgokce.carassistantai.ui.chat.view.ChatBotFragment

object MapUtils {
    fun openGoogleMapsForDestination(fragment: ChatBotFragment, destination: String) {
        Log.e(ChatBotFragment.TAG, "Destination: $destination")
        val encodedDestination = Uri.encode(destination)
        Log.e(ChatBotFragment.TAG, "Encoded Destination: $encodedDestination")
        val appUrl = "$GOOGLE_MAPS_WEB_URL$encodedDestination"

        AppUtils.openApp(
            context = fragment.requireContext(),
            packageName = GOOGLE_MAPS_PACKAGE_NAME,
            appUrl = appUrl,
            sendBotMessage = { errorMessage -> fragment.sendBotMessage(errorMessage) },
            errorMessageResId = R.string.google_maps_not_installed
        )
    }

    //val gmmIntentUri = Uri.parse("google.navigation:q=$encodedDestination")  // creates a route
    //val gmmIntentUri = Uri.parse("geo:0,0?q=$encodedDestination") //  shows location
    //val gmmIntentUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$encodedDestination") // directions on the web
}
