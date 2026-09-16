package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.data.SocietyRepository
import com.example.ui.screens.MainScreen
import com.example.ui.theme.GullakSocietyTheme
import com.example.util.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var repository: SocietyRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        NotificationHelper.createNotificationChannel(applicationContext)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }

        repository = SocietyRepository(applicationContext)

        // User Request: App khulte hi live sync automatically ho jana chahiye
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                if (repository.isLiveSyncActive.value) {
                    repository.syncWithGoogleSheet()
                }
            } catch (_: Exception) {}
        }

        setContent {
            GullakSocietyTheme {
                MainScreen(repository = repository)
            }
        }
    }
}
