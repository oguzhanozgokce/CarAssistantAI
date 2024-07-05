package com.oguzhanozgokce.carassistantai.ui.chat

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.oguzhanozgokce.carassistantai.R
import com.oguzhanozgokce.carassistantai.common.gone
import com.oguzhanozgokce.carassistantai.common.setupCardView
import com.oguzhanozgokce.carassistantai.common.visible
import com.oguzhanozgokce.carassistantai.data.model.Message
import com.oguzhanozgokce.carassistantai.databinding.FragmentChatBotBinding
import com.oguzhanozgokce.carassistantai.ui.chat.adapter.MessageAdapter
import com.oguzhanozgokce.carassistantai.ui.chat.helper.SpeechRecognizerHelper
import com.oguzhanozgokce.carassistantai.ui.chat.utils.alarm.AlarmUtils
import com.oguzhanozgokce.carassistantai.ui.chat.utils.CameraUtils
import com.oguzhanozgokce.carassistantai.ui.chat.utils.CommandProcessor
import com.oguzhanozgokce.carassistantai.ui.chat.utils.ContactUtils
import com.oguzhanozgokce.carassistantai.ui.chat.utils.GoogleUtils
import com.oguzhanozgokce.carassistantai.ui.chat.utils.InstagramUtils
import com.oguzhanozgokce.carassistantai.ui.chat.utils.MapUtils
import com.oguzhanozgokce.carassistantai.ui.chat.utils.PhotoUtils
import com.oguzhanozgokce.carassistantai.ui.chat.utils.SpotifyUtils
import com.oguzhanozgokce.carassistantai.ui.chat.utils.YouTubeUtils

class ChatBotFragment : Fragment() {
    private var _binding: FragmentChatBotBinding? = null
    private val binding get() = _binding!!
    private val adapter by lazy { MessageAdapter() }
    private val commandProcessor by lazy { CommandProcessor(this) }
    private val viewModel: ChatBotViewModel by viewModels()
    private lateinit var speechRecognizerHelper: SpeechRecognizerHelper


    companion object {
        const val TAG = "ChatBotFragment"
    }

    val requestContactPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            // Handle the case where all permissions are granted
        } else {
            sendBotMessage("Necessary authorisations were not granted")
        }
    }

    private val requestStoragePermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Actions to be taken when authorisation is granted
        } else {
            sendBotMessage("The necessary permits were not granted.")
        }
    }



    fun requestStoragePermission() {
        val readPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            android.Manifest.permission.READ_MEDIA_IMAGES
        } else {
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        }
        requestStoragePermissionsLauncher.launch(readPermission)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBotBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupButtonSend()
        setupMicButton()
        setupCardViews()

        speechRecognizerHelper = SpeechRecognizerHelper(this) { userMessage ->
            sendMessage(Message(userMessage, false))
            switchToChatMode()
            commandProcessor.processCommand(userMessage)
        }


        // Observe the chat response
        viewModel.chatResponse.observe(viewLifecycleOwner) { result ->
            result.fold(
                onSuccess = { response ->
                    sendBotMessage(response)
                },
                onFailure = { error ->
                    sendBotMessage(error.message ?: "An error occurred")
                }
            )
        }

    }

    private fun setupRecyclerView() {
        binding.recyclerView.apply {
            adapter = this@ChatBotFragment.adapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun setupMicButton() {
        binding.buttonMic.setOnClickListener {
            speechRecognizerHelper.startVoiceInput()
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
        binding.cardView1.setupCardView(R.id.textViewQuestion1) { message ->
            handleCardViewClick(message)
        }
        binding.cardView2.setupCardView(R.id.textViewQuestion2) { message ->
            handleCardViewClick(message)
        }
        binding.cardView3.setupCardView(R.id.textViewQuestion3) { message ->
            handleCardViewClick(message)
        }
    }

    private fun handleCardViewClick(message: String) {
        sendMessage(Message(message, false))
        switchToChatMode()
        commandProcessor.processCommand(message)
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

    fun getPhotosByDateRange(startDate: Long, endDate: Long) {
        PhotoUtils.getPhotosByDateRange(this, startDate, endDate)
    }

    fun openGooglePhotos() {
        PhotoUtils.openGooglePhotos(this)
    }

    fun openSpotify() {
        SpotifyUtils.openSpotify(requireContext()) { errorMessage ->
            sendBotMessage(errorMessage)
        }
    }

    fun searchSpotify(query: String) {
        SpotifyUtils.searchSpotify(requireContext(), query) { errorMessage ->
            sendBotMessage(errorMessage)
        }
    }

    fun openInstagram() {
        InstagramUtils.openInstagram(requireContext()) { errorMessage ->
            sendBotMessage(errorMessage)
        }
    }
    fun openInstagramProfile(username: String) {
        InstagramUtils.openInstagramProfile(requireContext(), username) { errorMessage ->
            sendBotMessage(errorMessage)
        }
    }

    fun openCamera() {
        CameraUtils.openCamera(this)
    }

    fun sendOpenAiRequest(userMessage: String) {
        viewModel.sendMessage(userMessage)
    }

    fun setAlarm(hour: Int, minute: Int, message: String) {
        AlarmUtils.setAlarm(requireContext(), hour, minute, message)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}