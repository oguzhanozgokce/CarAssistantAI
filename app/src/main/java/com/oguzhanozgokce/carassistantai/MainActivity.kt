package com.oguzhanozgokce.carassistantai

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.oguzhanozgokce.carassistantai.databinding.ActivityMainBinding
import com.oguzhanozgokce.carassistantai.ui.chat.ChatBotFragment.Companion.TAG
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import com.oguzhanozgokce.carassistantai.ui.chat.FloatingIconService


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    private val requestOverlayPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (Settings.canDrawOverlays(this)) {
            startFloatingIconService()
        } else {
            Log.d(TAG, "SYSTEM_ALERT_WINDOW izni verilmedi.")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        listInstalledPackages()

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        val navView: BottomNavigationView = binding.bottomNavView
        navView.setupWithNavController(navController)

        checkOverlayPermissionAndStartService()
    }

    private fun listInstalledPackages() {
        val pm: PackageManager = packageManager
        val packages = pm.getInstalledPackages(0)
        for (packageInfo in packages) {
            Log.d(TAG, "Installed package: ${packageInfo.packageName}")
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    private fun checkOverlayPermissionAndStartService() {
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            requestOverlayPermissionLauncher.launch(intent)
        } else {
            startFloatingIconService()
        }
    }

    private fun startFloatingIconService() {
        val intent = Intent(this, FloatingIconService::class.java)
        startService(intent)
    }
}

// Client ID = 149427396719-mlus9cqi2ena532dm5g2soo6rkofc4b4.apps.googleusercontent.com
// gemini api key = AIzaSyBMwsij534VYo0Zi6YCvwUVdsMIh3ziRt0
// keytool -keystore path-to-debug-or-production-keystore -list -v

//AIzaSyCmDkEwxItRh7m-mSucXSVc8Cxt3Saq4CA