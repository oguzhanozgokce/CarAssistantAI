package com.oguzhanozgokce.carassistantai.ui.chat

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.speech.RecognizerIntent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.oguzhanozgokce.carassistantai.R
import com.oguzhanozgokce.carassistantai.common.gone
import com.oguzhanozgokce.carassistantai.common.visible
import com.oguzhanozgokce.carassistantai.data.model.Message
import com.oguzhanozgokce.carassistantai.databinding.FragmentChatBotBinding
import com.oguzhanozgokce.carassistantai.ui.chat.adapter.MessageAdapter
import com.oguzhanozgokce.carassistantai.ui.chat.utils.CommandProcessor
import com.oguzhanozgokce.carassistantai.ui.chat.utils.ContactUtils
import com.oguzhanozgokce.carassistantai.ui.chat.utils.GoogleUtils
import com.oguzhanozgokce.carassistantai.ui.chat.utils.MapUtils
import com.oguzhanozgokce.carassistantai.ui.chat.utils.YouTubeUtils
import java.util.Locale

class ChatBotFragment : Fragment() {
    private var _binding: FragmentChatBotBinding? = null
    private val binding get() = _binding!!
    private val adapter by lazy { MessageAdapter() }
    private val commandProcessor by lazy { CommandProcessor(this) }


    companion object {
        private const val REQUEST_CODE_SPEECH_INPUT = 100
        const val REQUEST_CODE_CONTACT_PERMISSIONS = 101
        const val TAG = "ChatBotFragment"
    }

    val requestContactPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            // Handle the case where all permissions are granted
        } else {
            sendBotMessage("Gerekli izinler verilmedi.")
        }
    }

    private val speechRecognizerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == AppCompatActivity.RESULT_OK && result.data != null) {
            result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.let { results ->
                if (results.isNotEmpty()) {
                    binding.editTextMessage.setText(results[0])
                }
            }
        }
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
        binding.recyclerView.apply {
            adapter = this@ChatBotFragment.adapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun setupMicButton() {
        binding.buttonMic.setOnClickListener {
            startVoiceInput()
        }
    }


    private fun startVoiceInput() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Konuşun...")
        }
        try {
            speechRecognizerLauncher.launch(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupButtonSend() {
        binding.buttonSend.setOnClickListener {
            val message = binding.editTextMessage.text.toString()
            if (message.isNotEmpty()) {
                sendMessage(Message(message, false))
                binding.editTextMessage.text.clear()
                switchToChatMode()
                commandProcessor.processCommand(message)
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
            commandProcessor.processCommand(message)
        }
    }

    private fun sendMessage(message: Message) {
        adapter.addMessage(message)
        binding.recyclerView.scrollToPosition(adapter.itemCount - 1)
    }

    fun sendBotMessage(message: String) {
        val botMessage = Message(message, true)
        adapter.addMessage(botMessage)
        binding.recyclerView.scrollToPosition(adapter.itemCount - 1)
    }

    private fun switchToChatMode() {
        binding.questionLayout.gone()
        binding.recyclerView.visible()
    }

    fun openGoogleSearch(query: String) {
        GoogleUtils.openGoogleSearch(requireContext(), query) { errorMessage ->
            sendBotMessage(errorMessage)
        }
    }


    fun searchAndOpenYouTube(query: String) {
        YouTubeUtils.searchAndOpenYouTube(this, query)
    }

    fun openGoogleMapsForDestination(destination: String) {
        MapUtils.openGoogleMapsForDestination(this, destination)
    }

    fun findContactAndCall(contactName: String) {
        ContactUtils.findContactAndCall(this, contactName)
    }

    fun findContactAndSendMessage(contactName: String, message: String) {
        ContactUtils.findContactAndSendMessage(this, contactName, message)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
