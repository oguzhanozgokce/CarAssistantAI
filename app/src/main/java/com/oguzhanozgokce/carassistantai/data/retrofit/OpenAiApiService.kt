package com.oguzhanozgokce.carassistantai.data.retrofit



import com.oguzhanozgokce.carassistantai.data.model.chatgbt.ChatCompletionRequest
import com.oguzhanozgokce.carassistantai.data.model.chatgbt.ChatCompletionResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface OpenAiApiService {

    @Headers("Content-Type: application/json")
    @POST("chat/completions")
    suspend fun getChatCompletion(
        @Header("Authorization") authKey: String,
        @Body request: ChatCompletionRequest
    ): ChatCompletionResponse
}
