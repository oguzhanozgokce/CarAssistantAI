package com.oguzhanozgokce.carassistantai

import android.content.pm.PackageManager
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
import com.oguzhanozgokce.carassistantai.ui.chat.view.ChatBotFragment.Companion.TAG


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
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        listInstalledPackages()

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        val navView: BottomNavigationView = binding.bottomNavView
        navView.setupWithNavController(navController)


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

}

// Client ID = 149427396719-mlus9cqi2ena532dm5g2soo6rkofc4b4.apps.googleusercontent.com
// gemini api key = AIzaSyBMwsij534VYo0Zi6YCvwUVdsMIh3ziRt0
// keytool -keystore path-to-debug-or-production-keystore -list -v

//AIzaSyCmDkEwxItRh7m-mSucXSVc8Cxt3Saq4CA