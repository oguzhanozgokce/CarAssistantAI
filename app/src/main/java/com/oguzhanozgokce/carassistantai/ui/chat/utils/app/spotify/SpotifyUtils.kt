package com.oguzhanozgokce.carassistantai.ui.chat.utils.app.spotify

import android.content.Context
import com.oguzhanozgokce.carassistantai.R
import com.oguzhanozgokce.carassistantai.common.Constant.SPOTIFY_PACKAGE_NAME
import com.oguzhanozgokce.carassistantai.common.Constant.SPOTIFY_WEB_URL
import com.oguzhanozgokce.carassistantai.ui.chat.utils.app.AppUtils

object SpotifyUtils {
    fun openSpotify(context: Context, sendBotMessage: (String) -> Unit) {
        AppUtils.openApp(context, SPOTIFY_PACKAGE_NAME, SPOTIFY_WEB_URL, sendBotMessage, R.string.error_opening_spotify_app)
    }
}
