package com.oguzhanozgokce.carassistantai.ui.chat.utils.app.google

import android.content.Context
import android.util.Log
import com.oguzhanozgokce.carassistantai.BuildConfig
import com.oguzhanozgokce.carassistantai.data.retrofit.GoogleSearchApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SearchGoogle(private val context: Context) {

    private val cx = "6762663cb67374c2a"

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://www.googleapis.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val api = retrofit.create(GoogleSearchApi::class.java)

    fun search(query: String, callback: (String?) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d("SearchGoogle", "Searching for: $query")
                val response = api.search(query, cx, BuildConfig.GOOGLE_API_KEY).execute()
                if (response.isSuccessful) {
                    val firstLink = response.body()?.items?.firstOrNull()?.link
                    Log.d("SearchGoogle", "First link found: $firstLink")
                    withContext(Dispatchers.Main) {
                        callback(firstLink)
                    }
                } else {
                    Log.e("SearchGoogle", "Response not successful: ${response.errorBody()?.string()}")
                    withContext(Dispatchers.Main) {
                        callback(null)
                    }
                }
            } catch (e: Exception) {
                Log.e("SearchGoogle", "Error during search", e)
                withContext(Dispatchers.Main) {
                    callback(null)
                }
            }
        }
    }
}