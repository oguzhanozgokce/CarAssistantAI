package com.oguzhanozgokce.carassistantai.ui.chat.view

import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import com.oguzhanozgokce.carassistantai.common.Constant.GEMINI_API_KEY
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
import com.oguzhanozgokce.carassistantai.ui.chat.utils.google.GoogleUtils
import com.oguzhanozgokce.carassistantai.ui.chat.utils.instagram.InstagramUtils
import com.oguzhanozgokce.carassistantai.ui.chat.utils.mail.MailUtils
import com.oguzhanozgokce.carassistantai.ui.chat.utils.map.MapUtils
import com.oguzhanozgokce.carassistantai.ui.chat.utils.photo.PhotoUtils
import com.oguzhanozgokce.carassistantai.ui.chat.utils.spotify.SpotifyUtils
import com.oguzhanozgokce.carassistantai.ui.chat.utils.youtube.YouTubeUtils
import kotlinx.coroutines.runBlocking

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

        setupUI()
        observeViewModel()

        speechRecognizerHelper = SpeechRecognizerHelper(this) { userMessage ->
            sendMessage(Message(userMessage, false))
            commandProcessor.processCommand(userMessage)
            //sendGeminiResponse(userMessage)
        }

        if (savedInstanceState == null) {
            viewModel.setChatMode(false)
            sendMessage(Message("Welcome!", true))
            sendMessage(Message("How can I help you?", true))
        }
        binding.root.viewTreeObserver.addOnGlobalLayoutListener {
            val rect = Rect()
            binding.root.getWindowVisibleDisplayFrame(rect)
            val screenHeight = binding.root.height
            val keypadHeight = screenHeight - rect.bottom

            if (keypadHeight > screenHeight * 0.15) { // Klavye açıldı
                binding.recyclerView.scrollToPosition(adapter.itemCount - 1)
            } else {

            }
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
                commandProcessor.processCommand(message)
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

     fun showLoadingAnimation() {
        val loadingMessage = Message("", isBotMessage = true, isLoading = true)
        viewModel.addMessage(loadingMessage)
        binding.recyclerView.scrollToPosition(adapter.itemCount - 1)
    }

     fun hideLoadingAnimation() {
        viewModel.removeLoadingMessage()
    }

     fun sendGeminiResponse(prompt: String) {
        val generativeModel = GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = GEMINI_API_KEY,
            generationConfig = generationConfig {
                temperature = 1f
                topK = 64
                topP = 0.95f
                maxOutputTokens = 8192
                responseMimeType = "text/plain"
            },
            // safetySettings = Adjust safety settings
            // See https://ai.google.dev/gemini-api/docs/safety-settings
            systemInstruction = content { text("Uygulamamız bir sesli veya text olarak asisstan uygulamasıdır Kullanıcının komutlarını dinlersin onlara göre işlemler yapabilirsin Mevcut uygulamama belli başlı komutlar var bunlar mesela;\nYoutube dan şarkı açtırma \nKamerayı açma\nFotoğrafları açma\nSpotfy açma\nRehberden birini arama\nMesaj gönderme\nSaat Uygulamasını açma\nAlarm kurdurma\nInstagrama girme\nGoogldan search etme \nVe de kullanıcının spesifik bir araştırma veya merak ettiği bir şey varsa onları araştırma yanıt vermen. Şimdi hepsi uygulamada json dosyalarında tutluyor ve uygun format gerekiyor çalışmaları için\nMesela Youtube da şarkı açtırma formatı. şu şekilde işler;\nBaşta youtube yazıcak sonrasında istenilen video ismi sonrasında aç \" Youtube sezen aksu git aç\"  senin burada yapman gereken ise mesela kullanıcı \"Sezen aksu git şarkısını aç\" dediğinde sen yukarıda ki formata çevirmen gerekiyor veya\" Youtube sezen aksu git şarkısı\" dediğinde de uygun formata çevirmen gerekiyor ki ben uygulamada çalıştırabilim\nMesela bir diğeri rehberden birini arama \nNormalde çalışması için başta rehberden sonra dediğim isim gelir sonra ara gelir bu şekilde çalışma işler örnek \"rehberden buse ara\" ama ben burada \"Buseyi ara\" dediğimde de sen doğru formata çevirmen gerekiyor uygun kelimeleri getirip veya isimdeki ekleri atıp uygun formata çevirmelisin. Veya başka türlü diyebilir sen onun birini araması gerektiğini anlayıp uygun json formatında bana geri vermen gerekiyor.\nGeri kalanı da sana anlatim kullanıcının istekleri analiz doğru formatta oluşturman lazım\n\nKullanıcıdan gelen komutları analiz et ve uygun JSON formatına çevir. Aşağıdaki komutları uygun JSON formatına çevir:\n\n1. \"YouTube'dan Sezen Aksu Git şarkısını aç\"\n2. \"Kamerayı aç\"\n3. \"Fotoğrafları aç\"\n4. \"Spotify'ı aç\"\n5. \"Buse'yi ara\"\n6. \"Buse'ye merhaba demek istiyorum\"\n7. \"Saat uygulamasını aç\"\n8. \"23:55 alarm kur\"\n9. \"Instagram'da username profiline bak\"\n10. \"Matematik search\"\n11. \"Beni Beşiktaş Meydanı'na götür\"\n\nÖrnek çıktılar:\n\n1. Kullanıcı Komutu: \"YouTube'dan Sezen Aksu Git şarkısını aç\"\n   JSON Formatı:\n   ```json\n   {\n     \"type\": \"open\",\n     \"target\": \"YouTube\",\n     \"action\": \"search\",\n     \"parameters\": {\n       \"query\": \"Sezen Aksu Git\"\n     }\n   }\n\nKullanıcı Komutu: \"Kamerayı aç\"\n{\n  \"type\": \"open\",\n  \"target\": \"Camera\",\n  \"action\": \"open\",\n  \"parameters\": {}\n}\n\nburada aç demese bile sen anlayıp açman lazım\n\nfotoğrafları aç dediğinde \n{\n  \"type\": \"open\",\n  \"target\": \"Gallery\",\n  \"action\": \"open\",\n  \"parameters\": {}\n}\n\n{\n  \"type\": \"open\",\n  \"target\": \"Spotify\",\n  \"action\": \"open\",\n  \"parameters\": {}\n}\n\n{\n  \"type\": \"call\",\n  \"target\": \"Contacts\",\n  \"action\": \"call\",\n  \"parameters\": {\n    \"contactName\": \"Buse\"\n  }\n}\n\nmesaj gönderme\n\n{\n  \"type\": \"message\",\n  \"target\": \"Contacts\",\n  \"action\": \"send\",\n  \"parameters\": {\n    \"contactName\": \"Buse\",\n    \"message\": \"merhaba\"\n  }\n}\n\nsaat uygulamasını aç \n{\n  \"type\": \"open\",\n  \"target\": \"Clock\",\n  \"action\": \"open\",\n  \"parameters\": {}\n}\n\nKullanıcı Komutu: \"23:55 alarm kur\"\n{\n  \"type\": \"alarm\",\n  \"target\": \"Clock\",\n  \"action\": \"set\",\n  \"parameters\": {\n    \"time\": \"23:55\"\n  }\n}\nburada 23.55 de yazabilir ama sen onu doğru formata çevir\n\n{\n  \"type\": \"search\",\n  \"target\": \"Google\",\n  \"action\": \"search\",\n  \"parameters\": {\n    \"query\": \"Matematik\"\n  }\n}\n\nburada  search işlemi derse ona göre döndür\n\n{\n  \"type\": \"navigate\",\n  \"target\": \"GoogleMaps\",\n  \"action\": \"route\",\n  \"parameters\": {\n    \"destination\": \"Beşiktaş Meydanı\"\n  }\n}\nburada götür diyebilir navigasyondan yol tarifi diyebilir vs üretebilirsin ama sen kullanıcının yol tarifi uygulamasını açmak istediğini anladığında bana uygun formatta döndür\n\nbunlar dışında bir şey araştırmak istiyorsa da kullanıcıya cevap verebilirsin doğru cevabı") },
        )

         runBlocking {
             try {
                 val response = generativeModel.generateContent(prompt)
                 hideLoadingAnimation()
                 sendBotMessage(response.text.toString())
             } catch (e: Exception) {
                 hideLoadingAnimation()
                 sendBotMessage("An error occurred: ${e.message}")
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


    fun setAlarm(hour: Int, minute: Int, message: String) {
        AlarmUtils.setAlarm(requireContext(), hour, minute, message)

    }

    fun openClockApp() {
        AlarmUtils.showAlarms(requireContext())
    }

    fun openMailApp() {
        MailUtils.openMailApp(requireContext()) { message ->
            sendBotMessage(message)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}