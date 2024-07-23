package com.oguzhanozgokce.carassistantai.ui.chat.view

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.oguzhanozgokce.carassistantai.data.model.message.Message

class ChatBotViewModel : ViewModel() {
    private val _messages = MutableLiveData<MutableList<Message>>(mutableListOf())
    val messages: LiveData<MutableList<Message>> = _messages

    private val _isChatMode = MutableLiveData<Boolean>(false)
    val isChatMode: LiveData<Boolean> = _isChatMode

    fun addMessage(message: Message) {
        _messages.value?.let {
            it.add(message)
            _messages.value = it
        }
        _isChatMode.value = true
    }

    fun setChatMode(isChatMode: Boolean) {
        _isChatMode.value = isChatMode
    }

    fun removeLoadingMessage() {
        _messages.value = _messages.value?.filterNot { it.isLoading }?.toMutableList()
        _messages.value = _messages.value
    }
}
