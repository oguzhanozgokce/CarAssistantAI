package com.oguzhanozgokce.carassistantai.ui.chat.utils.app.google

import android.content.Context
import android.util.Log
import com.oguzhanozgokce.carassistantai.BuildConfig
import com.oguzhanozgokce.carassistantai.common.Constant.GOOGLE_SEARCH_API_URL
import com.oguzhanozgokce.carassistantai.data.retrofit.GoogleSearchApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SearchGoogle(private val context: Context) {

    private val retrofit = Retrofit.Builder()
        .baseUrl(GOOGLE_SEARCH_API_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val api = retrofit.create(GoogleSearchApi::class.java)

    fun search(query: String, callback: (String?) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = api.search(query, BuildConfig.CX, BuildConfig.GOOGLE_API_KEY).execute()
                if (response.isSuccessful) {
                    val firstLink = response.body()?.items?.firstOrNull()?.link
                    withContext(Dispatchers.Main) {
                        callback(firstLink)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        callback(null)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    callback(null)
                }
            }
        }
    }
}