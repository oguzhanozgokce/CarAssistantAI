package com.oguzhanozgokce.carassistantai.ui.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oguzhanozgokce.carassistantai.data.repo.OpenAiRepository
import kotlinx.coroutines.launch

class ChatBotViewModel : ViewModel() {

    private val repository = OpenAiRepository()

    private val _chatResponse = MutableLiveData<Result<String>>()
    val chatResponse: LiveData<Result<String>> get() = _chatResponse

    fun sendMessage(userMessage: String) {
        viewModelScope.launch {
            val result = repository.sendChatRequest(userMessage)
            _chatResponse.value = result
        }
    }
}