package com.oguzhanozgokce.carassistantai.data.repo

import android.util.Log
import com.oguzhanozgokce.carassistantai.common.Constant.CHAT_GBT_MODEL
import com.oguzhanozgokce.carassistantai.common.Constant.OPENAI_API_KEY
import com.oguzhanozgokce.carassistantai.data.model.chatgbt.ChatCompletionRequest
import com.oguzhanozgokce.carassistantai.data.model.chatgbt.ChatMessage
import com.oguzhanozgokce.carassistantai.data.retrofit.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext



class OpenAiRepository {

    suspend fun sendChatRequest(userMessage: String): Result<String> {
        return try {
            val request = ChatCompletionRequest(
                model = CHAT_GBT_MODEL,
                messages = listOf(
                    ChatMessage(role = "user", content = userMessage)
                ),
                temperature = 0.7
            )
            val response = withContext(Dispatchers.IO) {
                RetrofitClient.apiService.getChatCompletion(
                    authKey = "Bearer $OPENAI_API_KEY",
                    request = request
                )
            }
            if (response.choices.isNotEmpty()) {
                Result.success(response.choices.first().message.content)
            } else {
                Result.failure(Exception("No response"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
