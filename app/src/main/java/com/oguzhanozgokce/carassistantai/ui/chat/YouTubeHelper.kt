package com.oguzhanozgokce.carassistantai.ui.chat

import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.youtube.YouTube
import com.google.api.services.youtube.model.SearchListResponse
import com.google.api.services.youtube.model.SearchResult
import java.io.IOException

class YouTubeHelper(private val apiKey: String) {

    private val youtube: YouTube = YouTube.Builder(
        NetHttpTransport(),
        GsonFactory(),
        null
    ).setApplicationName("YouTubeDataAPI").build()

    fun searchVideos(query: String): List<SearchResult>? {
        return try {
            val search = youtube.search().list("id,snippet")
            search.key = apiKey
            search.q = query
            search.type = "video"
            search.maxResults = 1L

            val searchResponse: SearchListResponse = search.execute()
            searchResponse.items
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
}
