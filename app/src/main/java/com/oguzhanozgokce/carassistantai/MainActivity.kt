package com.oguzhanozgokce.carassistantai

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.oguzhanozgokce.carassistantai.databinding.ActivityMainBinding
import com.oguzhanozgokce.carassistantai.ui.chat.helper.SpeechRecognitionService
import com.oguzhanozgokce.carassistantai.ui.chat.view.ChatBotFragment
import com.oguzhanozgokce.carassistantai.ui.chat.view.RecordAudioPermissionDialogFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )
            insets
        }
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        binding.root.setBackgroundColor(resources.getColor(R.color.background_color, null))

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        if (!checkMicrophonePermission()) {
            requestMicrophonePermission()
        }

    }

    override fun onResume() {
        super.onResume()
        stopBackgroundService() // Stop the background service when the app is in the foreground
    }

    override fun onPause() {
        super.onPause()
        startBackgroundService() // Start the background service when the app goes to the background
    }

    private fun startBackgroundService() {
        if (checkMicrophonePermission()) {
            val serviceIntent = Intent(this, SpeechRecognitionService::class.java)
            Log.d("MainActivity", "Starting background service")
            startService(serviceIntent)
        } else {
            Log.e("MainActivity", "Microphone permission not granted")
        }
    }

    private fun stopBackgroundService() {
        val serviceIntent = Intent(this, SpeechRecognitionService::class.java)
        stopService(serviceIntent)
    }

    private fun checkMicrophonePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestMicrophonePermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.RECORD_AUDIO),
            REQUEST_MICROPHONE_PERMISSION
        )
    }

    companion object {
        private const val REQUEST_MICROPHONE_PERMISSION = 1
    }



    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_MICROPHONE_PERMISSION) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // Permission granted
                startBackgroundService()
            } else {
                // Permission denied
                Log.e("MainActivity", "Microphone permission denied")
            }
        }
    }

}