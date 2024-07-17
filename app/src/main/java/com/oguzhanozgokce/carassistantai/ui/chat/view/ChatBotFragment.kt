package com.oguzhanozgokce.carassistantai.ui.chat.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.oguzhanozgokce.carassistantai.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.asTextOrNull
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import com.oguzhanozgokce.carassistantai.R
import com.oguzhanozgokce.carassistantai.common.Constant.GEMINI_MODEL_NAME
import com.oguzhanozgokce.carassistantai.common.gone
import com.oguzhanozgokce.carassistantai.common.visible
import com.oguzhanozgokce.carassistantai.data.model.message.Message
import com.oguzhanozgokce.carassistantai.databinding.FragmentChatBotBinding
import com.oguzhanozgokce.carassistantai.ui.chat.adapter.MessageAdapter
import com.oguzhanozgokce.carassistantai.ui.chat.helper.SpeechRecognizerHelper
import com.oguzhanozgokce.carassistantai.ui.chat.utils.alarm.AlarmUtils
import com.oguzhanozgokce.carassistantai.ui.chat.utils.camera.CameraUtils
import com.oguzhanozgokce.carassistantai.ui.chat.utils.CommandProcessor
import com.oguzhanozgokce.carassistantai.ui.chat.utils.contact.ContactUtils
import com.oguzhanozgokce.carassistantai.ui.chat.utils.app.google.GoogleUtils
import com.oguzhanozgokce.carassistantai.ui.chat.utils.app.instagram.InstagramUtils
import com.oguzhanozgokce.carassistantai.ui.chat.utils.mail.MailUtils
import com.oguzhanozgokce.carassistantai.ui.chat.utils.app.map.MapUtils
import com.oguzhanozgokce.carassistantai.ui.chat.utils.app.photo.PhotoUtils
import com.oguzhanozgokce.carassistantai.ui.chat.utils.app.spotify.SpotifyUtils
import com.oguzhanozgokce.carassistantai.ui.chat.utils.app.whatsapp.WhatsAppUtils
import com.oguzhanozgokce.carassistantai.ui.chat.utils.app.youtube.YouTubeUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

    val requestContactPermissionsLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            // Handle the case where all permissions are granted
        } else {
            sendBotMessage(getString(R.string.permissions_not_granted))
        }
    }

    val cameraActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            // Handle the camera result here
        } else {
            sendBotMessage(getString(R.string.camera_app_not_found))
        }
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

        setupUI()
        observeViewModel()

        speechRecognizerHelper = SpeechRecognizerHelper(this) { userMessage ->
            sendMessage(Message(userMessage, false))
            showLoadingAnimation()
            sendToGeminiAndProcessCommand(userMessage) { jsonResponse ->
                hideLoadingAnimation()
                commandProcessor.processCommand(jsonResponse)
            }
        }


        if (savedInstanceState == null) {
            viewModel.setChatMode(false)
            sendMessage(Message("Welcome!", true))
            sendMessage(Message(getString(R.string.app_intro),true))
        }
    }

    private fun setupUI() {
        setupRecyclerView()
        setupButtonSend()
        setupMicButton()
    }

    private fun observeViewModel() {
        viewModel.messages.observe(viewLifecycleOwner) { messages ->
            adapter.setMessages(messages)
            binding.recyclerView.scrollToPosition(adapter.itemCount - 1)
        }

        viewModel.isChatMode.observe(viewLifecycleOwner) { isChatMode ->
            if (isChatMode) {
                switchToChatMode()
            } else {
                showCardViews()
            }
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
                showLoadingAnimation()
                sendToGeminiAndProcessCommand(message) { response ->
                    hideLoadingAnimation()
                    commandProcessor.processCommand(response)
                }
            }
        }
    }

    private fun sendMessage(message: Message) {
        viewModel.addMessage(message)
        binding.recyclerView.scrollToPosition(adapter.itemCount - 1)
    }

    fun sendBotMessage(message: String) {
        val botMessage = Message(message, true)
        viewModel.addMessage(botMessage)
        binding.recyclerView.scrollToPosition(adapter.itemCount - 1)
    }

    private fun switchToChatMode() {
        binding.recyclerView.visible()
    }

    private fun showCardViews() {
        binding.recyclerView.gone()
    }

    private fun showLoadingAnimation() {
        val loadingMessage = Message("", isBotMessage = true, isLoading = true)
        viewModel.addMessage(loadingMessage)
        binding.recyclerView.scrollToPosition(adapter.itemCount - 1)
    }

    fun hideLoadingAnimation() {
        viewModel.removeLoadingMessage()
    }

    fun sendToGeminiAndProcessCommand(prompt: String, callback: (String) -> Unit) {
        val generativeModel = GenerativeModel(
            modelName = GEMINI_MODEL_NAME,
            apiKey = BuildConfig.GEMINI_API_KEY,
            generationConfig = generationConfig {
                temperature = 1f
                topK = 64
                topP = 0.95f
                maxOutputTokens = 8192
                responseMimeType = "text/plain"
            },
            systemInstruction = content {
                text("Uygulamam sesli veya text ile çalışan asisstant uygulamasıdır. Kullanıcının kotmurlarını dinleyip analiz edip doğru işlemleri yaptırman gerekir. İki bölümden oluşuyor birincidi telefonun kendi içerisindeki komutlar mesela kamera açtırma, youtubedan şarkı açtırma, spotfy açtırma veya beni şuraya götür dediğinde haritayı açtırması gibi işlemlerden oluşuyor böyle komutları anladığında bana json türünde veri göndermeni istiyorum. Nasıl json komutları göndermen gerekitiğini yazacağım  \n\n\nJSON Formatı:\n\n```json\n{\n  \"type\": \"open\",\n  \"target\": \"YouTube\",\n  \"action\": \"search\",\n  \"parameters\": {\n    \"query\": \"Sezen Aksu Git\"\n  }\n}\n```\n\nKamera aç komutu gelirse veya açmak istediğini anlarsan bir şekilde bu json türünü gönder\n\n{\n\n\"type\": \"open\",\n\n\"target\": \"Camera\",\n\n\"action\": \"open\",\n\n\"parameters\": {}\n\n}\n\nfotoğrafları aç komutu gelirse veya açmak istediğini anlarsan bir şekilde bu json türünü gönder\n\n{\n\n\"type\": \"open\",\n\n\"target\": \"Gallery\",\n\n\"action\": \"open\",\n\n\"parameters\": {}\n\n}  \n  \nSpotfy aç komutu gelirse veya açmak istediğini anlarsan bir şekilde bu json türünü gönder\n\n{\n\n\"type\": \"open\",\n\n\"target\": \"Spotify\",\n\n\"action\": \"open\",\n\n\"parameters\": {}\n\n}\n\nSaat aç komutu gelirse veya açmak istediğini anlarsan bir şekilde bu json türünü gönder\n\nsaat uygulamasını aç\n\n{\n\n\"type\": \"open\",\n\n\"target\": \"Clock\",\n\n\"action\": \"open\",\n\n\"parameters\": {}\n\n}\n\n  \nEğerki kendisi arama yapmak isterse search varsa sen bu json türünü bana gönder\n\n{\n\n\"type\": \"search\",\n\n\"target\": \"Google\",\n\n\"action\": \"search\",\n\n\"parameters\": {  \n\"query\": \"Matematik\"\n\n}\n}  \n\nBirisini arama komutu gelirse veya rehberden birisine ara derse bunu analiz edip istediğini anlarsan  bu json türünü gönder\n{\n  \"type\": \"call\",\n  \"target\": \"Contacts\",\n  \"action\": \"call\",\n  \"parameters\": {\n    \"contactName\": \"Buse\"\n  }\n}\n\nBirisini mesaj gönder komutu gelirse veya rehberden birisine mesaj gönder derse bunu analiz edip istediğini anlarsan  bu json türünü gönder\n\n  {\n  \"type\": \"message\",\n  \"target\": \"Contacts\",\n  \"action\": \"send\",\n  \"parameters\": {\n    \"contactName\": \"Buse\",\n    \"message\": \"merhaba\"\n  }\n}\n\nBir yere gitmek isterse veya yol tarifi derse veya beni götür derse bana bu json türünde veriyi gönder\n{\n  \"type\": \"navigate\",\n  \"target\": \"GoogleMaps\",\n  \"action\": \"route\",\n  \"parameters\": {\n    \"destination\": \"Beşiktaş Meydanı\"\n  }\n}\n\nMesela kullanıcı bana en iyi hastaneleri veya pizzacıları veya yakınlarındaki eczaneleri  göster derse bana bu json türünde veriyi döndür\n\n{\n  \"type\": \"show\",\n  \"target\": \"GoogleMaps\",\n  \"action\": \"search\",\n  \"parameters\": {\n    \"placeType\": \"hospitals\"\n  }\n}\n\nMesela kullanıcı alarm kurdurmak isterse sana saat verirse alarm kur derse bunu anlayıp bana bu json türünü döndür\n{\n    \"type\": \"alarm\",\n    \"target\": \"Clock\",\n    \"action\": \"set\",\n    \"parameters\": {\n        \"time\": \"08:30\",\n        \"message\": \"Time to wake up\"\n    }\n}\n\nMail aç komutu gelirse veya açmak istediğini anlarsan bir şekilde bu json türünü gönder\n\n{\n\n\"type\": \"open\",\n\n\"target\": \"Mail\",\n\n\"action\": \"open\",\n\n\"parameters\": {}\n\n}\n\nWhatsApp aç komutu gelirse veya açmak istediğini anlarsan bir şekilde bu json türünü gönder\n\n{\n\n\"type\": \"open\",\n\n\"target\": \"whatsapp\",\n\n\"action\": \"open\",\n\n\"parameters\": {}\n\n}\n\n\nbunlar dışında kalanları sen anlayıp kullancının sorunlarını nazik tatlı bir dille cevapla yapamadığın işlemler için özür dile. Bilgi sorularını vs cevaplayabilirsin\n")
            }
        )

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    generativeModel.generateContent(prompt)
                }
                val jsonResponse = response.candidates.first().content.parts.first().asTextOrNull() ?: ""
                Log.d(getString(R.string.gemini_response), jsonResponse)
                callback(jsonResponse)
            } catch (e: Exception) {
                hideLoadingAnimation()
                sendBotMessage("${getString(R.string.an_error_occurred)}: ${e.message}")
            }
        }
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
    fun showPlacesOnGoogleMaps(placeType: String) {
        MapUtils.showPlacesOnGoogleMaps(this, placeType)
    }
    fun findContactAndCall(contactName: String) {
        ContactUtils.findContactAndCall(this, contactName)
    }
    fun findContactAndSendMessage(contactName: String, message: String) {
        ContactUtils.findContactAndSendMessage(this, contactName, message)
    }
    fun openGooglePhotos() {
        PhotoUtils.openGooglePhotos(requireContext()) { errorMessage ->
            sendBotMessage(errorMessage)
        }
    }
    fun openSpotify() {
        SpotifyUtils.openSpotify(requireContext()) { errorMessage ->
            sendBotMessage(errorMessage)
        }
    }
    fun openInstagram() {
        InstagramUtils.openInstagram(requireContext()) { errorMessage ->
            sendBotMessage(errorMessage)
        }
    }
    fun openCamera() {
        CameraUtils.openCamera(this)
    }
    fun setAlarm(hour: Int, minute: Int, message: String) {
        AlarmUtils.setAlarm(requireContext(), hour, minute, message) { errorMessage ->
            sendBotMessage(errorMessage)
        }
    }
    fun openClockApp() {
        AlarmUtils.showAlarms(requireContext())
    }
    fun openMailApp() {
        MailUtils.openMailApp(requireContext()) { message ->
            sendBotMessage(message)
        }
    }
    fun openWhatsApp() {
       WhatsAppUtils.openWhatsAppChatWithMyNumber(requireContext()) { message ->
            sendBotMessage(message)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}