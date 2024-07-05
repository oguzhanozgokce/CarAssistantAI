package com.oguzhanozgokce.carassistantai.ui.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oguzhanozgokce.carassistantai.data.model.Message
import com.oguzhanozgokce.carassistantai.data.repo.OpenAiRepository
import kotlinx.coroutines.launch

class ChatBotViewModel : ViewModel() {

    private val repository = OpenAiRepository()

    private val _chatResponse = MutableLiveData<Result<String>>()
    val chatResponse: LiveData<Result<String>> get() = _chatResponse

    private val _messages = MutableLiveData<MutableList<Message>>(mutableListOf())
    val messages: LiveData<MutableList<Message>> = _messages

    private val _isChatMode = MutableLiveData<Boolean>(false)
    val isChatMode: LiveData<Boolean> = _isChatMode

    fun sendMessage(userMessage: String) {
        viewModelScope.launch {
            val result = repository.sendChatRequest(userMessage)
            _chatResponse.value = result
        }
    }

    fun addMessage(message: Message) {
        _messages.value?.add(message)
        _messages.value = _messages.value
        _isChatMode.value = true
    }

    fun setMessages(messages: List<Message>) {
        _messages.value = messages.toMutableList()
    }

    fun setChatMode(isChatMode: Boolean) {
        _isChatMode.value = isChatMode
    }
}