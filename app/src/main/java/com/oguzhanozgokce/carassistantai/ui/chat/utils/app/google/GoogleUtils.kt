package com.oguzhanozgokce.carassistantai.ui.chat.utils.app.google

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.oguzhanozgokce.carassistantai.R
import com.oguzhanozgokce.carassistantai.common.Constant.GOOGLE_SEARCH_URL
import com.oguzhanozgokce.carassistantai.ui.chat.utils.app.AppUtils

object GoogleUtils {
    private const val TAG = "GoogleUtils"
    fun openGoogleSearch(context: Context, query: String, onFailure: (String) -> Unit) {
        val encodedQuery = Uri.encode(query)
        val googleSearchUrl = "$GOOGLE_SEARCH_URL$encodedQuery"
        Log.d(TAG, context.getString(R.string.google_search_uri, googleSearchUrl))
        AppUtils.openWebUrl(context, googleSearchUrl, onFailure, R.string.google_search_app_not_found)
    }
}


/**
 * // Alternatively, you can use a dialogue to show a warning message to the user.
 *             val fallbackUri = Uri.parse("https://www.google.com/search?q=$encodedQuery")
 *             val fallbackIntent = Intent(Intent.ACTION_VIEW, fallbackUri)
 *             context.startActivity(fallbackIntent)
 */