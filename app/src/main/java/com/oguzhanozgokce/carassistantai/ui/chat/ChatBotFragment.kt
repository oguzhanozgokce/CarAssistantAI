package com.oguzhanozgokce.carassistantai.ui.chat

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognizerIntent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.oguzhanozgokce.carassistantai.R
import com.oguzhanozgokce.carassistantai.common.Constant.YouTube_API_KEY
import com.oguzhanozgokce.carassistantai.common.gone
import com.oguzhanozgokce.carassistantai.common.visible
import com.oguzhanozgokce.carassistantai.databinding.FragmentChatBotBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class ChatBotFragment : Fragment() {
    private var _binding: FragmentChatBotBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: MessageAdapter

    companion object {
        private const val REQUEST_CODE_SPEECH_INPUT = 100
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentChatBotBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupButtonSend()
        setupMicButton()
        setupCardViews()
    }

    private fun setupRecyclerView() {
        adapter = MessageAdapter()
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
    }

    private fun setupMicButton() {
        binding.buttonMic.setOnClickListener {
            startVoiceInput()
        }
    }

    private fun startVoiceInput() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Konuşun...")
        try {
            startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SPEECH_INPUT && resultCode == AppCompatActivity.RESULT_OK && data != null) {
            val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (result != null && result.isNotEmpty()) {
                binding.editTextMessage.setText(result[0])
            }
        }
    }

    private fun setupButtonSend() {
        binding.buttonSend.setOnClickListener {
            val message = binding.editTextMessage.text.toString()
            if (message.isNotEmpty()) {
                sendMessage(Message(message, false))
                binding.editTextMessage.text.clear()
                switchToChatMode()
                processCommand(message)
            }
        }
    }

    private fun setupCardViews() {
        setupCardView(binding.cardView1, R.id.textViewQuestion1)
        setupCardView(binding.cardView2, R.id.textViewQuestion2)
        setupCardView(binding.cardView3, R.id.textViewQuestion3)
    }

    private fun setupCardView(cardView: CardView, textViewId: Int) {
        cardView.setOnClickListener {
            val message = cardView.findViewById<TextView>(textViewId).text.toString()
            sendMessage(Message(message, false))
            switchToChatMode()
            processCommand(message)
        }
    }

    private fun sendMessage(message: Message) {
        adapter.addMessage(message)
        binding.recyclerView.scrollToPosition(adapter.itemCount - 1)
    }

    private fun sendBotMessage(message: String) {
        val botMessage = Message(message, true)
        adapter.addMessage(botMessage)
        binding.recyclerView.scrollToPosition(adapter.itemCount - 1)
    }


    private fun switchToChatMode() {
        binding.questionLayout.gone()
        binding.recyclerView.visible()
    }

    private fun processCommand(command: String) {
        sendBotMessage("Tamamdır efendim, isteğiniz yerine getiriliyor...")
        Handler(Looper.getMainLooper()).postDelayed({
            when {
                command.contains("YouTube", true) && command.contains("ac", true) -> {
                    val query = command.substringAfter("YouTube").substringBefore(" ac").trim()
                    searchAndOpenYouTube(query)
                }
                // Diğer komutları burada işleyebilirsiniz
            }
        }, 1000) // 1 saniye gecikme
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
        val youtubeHelper = YouTubeHelper(YouTube_API_KEY)
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
