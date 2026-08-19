package com.precon.mhsclubs

import android.os.Bundle
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.edit
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import com.precon.mhsclubs.auth.AndroidAuthService
import com.precon.mhsclubs.data.AndroidClubContentApi
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var authService: AndroidAuthService

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        authService = AndroidAuthService(this)
        
        // Register Google Sign-In launcher
        val googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            lifecycleScope.launch {
                authService.handleSignInResult(result.resultCode, result.data)
            }
        }
        authService.googleSignInLauncher = googleSignInLauncher

        val clubContentApi = AndroidClubContentApi(getString(R.string.mhs_clubs_api_base_url))

        setContent {
            val preferences = remember { getSharedPreferences("app_preferences", MODE_PRIVATE) }
            var notificationsEnabled by remember {
                mutableStateOf(
                    preferences.getBoolean(
                        "notifications_enabled",
                        (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.TIRAMISU) ||
                            (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED),
                    )
                )
            }
            val notificationPermissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission(),
            ) { granted ->
                notificationsEnabled = granted
                preferences.edit { putBoolean("notifications_enabled", granted) }
            }
            App(
                authService,
                clubContentApi,
                notificationsEnabled = notificationsEnabled,
                onNotificationsChange = { enabled ->
                    if (enabled && (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) &&
                        (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
                    ) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        notificationsEnabled = enabled
                        preferences.edit { putBoolean("notifications_enabled", enabled) }
                    }
                },
            )
        }
    }

}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
