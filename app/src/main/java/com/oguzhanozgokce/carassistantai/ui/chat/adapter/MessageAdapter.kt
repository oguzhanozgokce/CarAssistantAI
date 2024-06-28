package com.oguzhanozgokce.carassistantai.ui.chat.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.oguzhanozgokce.carassistantai.data.model.Message
import com.oguzhanozgokce.carassistantai.databinding.ItemBotMessageBinding
import com.oguzhanozgokce.carassistantai.databinding.ItemMessageBinding


class MessageAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val messages: MutableList<Message> = mutableListOf()

    companion object {
        private const val VIEW_TYPE_USER = 1
        private const val VIEW_TYPE_BOT = 2
    }

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].isBotMessage) VIEW_TYPE_BOT else VIEW_TYPE_USER
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == VIEW_TYPE_USER) {
            val binding = ItemMessageBinding.inflate(inflater, parent, false)
            UserMessageViewHolder(binding)
        } else {
            val binding = ItemBotMessageBinding.inflate(inflater, parent, false)
            BotMessageViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        if (holder is UserMessageViewHolder) {
            holder.bind(message)
        } else if (holder is BotMessageViewHolder) {
            holder.bind(message)
        }
    }

    override fun getItemCount(): Int = messages.size

    fun addMessage(message: Message) {
        messages.add(message)
        notifyItemInserted(messages.size - 1)
    }

    inner class UserMessageViewHolder(private val binding: ItemMessageBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(message: Message) {
            binding.textUserMessage.text = message.content
        }
    }

    inner class BotMessageViewHolder(private val binding: ItemBotMessageBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(message: Message) {
            binding.textBotMessage.text = message.content
        }
    }
}
