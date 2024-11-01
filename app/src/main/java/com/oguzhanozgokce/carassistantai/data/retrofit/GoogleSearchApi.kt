package com.oguzhanozgokce.carassistantai.data.retrofit

import com.oguzhanozgokce.carassistantai.data.model.search.SearchResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface GoogleSearchApi {
    @GET("customsearch/v1")
    fun search(
        @Query("q") query: String,
        @Query("cx") cx: String,
        @Query("key") apiKey: String
    ): Call<SearchResponse>
}