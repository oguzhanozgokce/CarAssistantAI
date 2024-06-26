package com.oguzhanozgokce.carassistantai.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.oguzhanozgokce.carassistantai.R
import com.oguzhanozgokce.carassistantai.databinding.FragmentChatBotBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatBotFragment : Fragment() {
    private var _binding: FragmentChatBotBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: MessageAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentChatBotBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = MessageAdapter()
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(context)

        binding.buttonSend.setOnClickListener {
            val message = binding.editTextMessage.text.toString()
            if (message.isNotEmpty()) {
                sendMessage(message)
                binding.editTextMessage.text.clear()
                switchToChatMode()
                processCommand(message)
            }
        }

        setupCardView(binding.cardView1, R.id.textViewQuestion1)
        setupCardView(binding.cardView2, R.id.textViewQuestion2)
        setupCardView(binding.cardView3, R.id.textViewQuestion3)
    }

    private fun setupCardView(cardView: CardView, textViewId: Int) {
        cardView.setOnClickListener {
            val message = cardView.findViewById<TextView>(textViewId).text.toString()
            sendMessage(message)
            switchToChatMode()
            processCommand(message)
        }
    }


    private fun sendMessage(message: String) {
        adapter.addMessage(Message(message))
        binding.recyclerView.scrollToPosition(adapter.itemCount - 1)
    }

    private fun switchToChatMode() {
        binding.questionLayout.visibility = View.GONE
        binding.recyclerView.visibility = View.VISIBLE
    }

    private fun processCommand(command: String) {
        when {
            command.contains("YouTube", true) && command.contains("ac", true) -> {
                val query = command.substringAfter("YouTube").substringBefore(" ac").trim()
                searchAndOpenYouTube(query)
            }
            // Diğer komutları burada işleyebilirsiniz
        }
    }

    private fun searchAndOpenYouTube(query: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val videoId = getFirstVideoId(query)
            if (videoId != null) {
                withContext(Dispatchers.Main) {
                    openYouTubeWithVideoId(videoId)
                }
            }
        }
    }

    private fun getFirstVideoId(query: String): String? {
        val youtubeHelper = YouTubeHelper("AIzaSyCmDkEwxItRh7m-mSucXSVc8Cxt3Saq4CA")
        val results = youtubeHelper.searchVideos(query)
        return results?.firstOrNull()?.id?.videoId
    }

    private fun openYouTubeWithVideoId(videoId: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=$videoId"))
        intent.setPackage("com.google.android.youtube")
        if (intent.resolveActivity(requireActivity().packageManager) != null) {
            startActivity(intent)
        } else {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=$videoId"))
            startActivity(webIntent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
