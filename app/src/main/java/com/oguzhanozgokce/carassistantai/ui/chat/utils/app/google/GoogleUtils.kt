package com.oguzhanozgokce.carassistantai.ui.chat.utils.app.google

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
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

    fun openLink(context: Context, url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "No app found to open the link", Toast.LENGTH_SHORT).show()
        }
    }
}
