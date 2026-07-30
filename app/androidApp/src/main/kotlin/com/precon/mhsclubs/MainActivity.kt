package com.precon.mhsclubs

import android.os.Bundle
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
        val clubContentApi = AndroidClubContentApi(getString(R.string.mhs_clubs_api_base_url))

        setContent {
            val preferences = remember { getSharedPreferences("app_preferences", MODE_PRIVATE) }
            var notificationsEnabled by remember {
                mutableStateOf(
                    preferences.getBoolean(
                        "notifications_enabled",
                        android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.TIRAMISU ||
                            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
                    )
                )
            }
            val notificationPermissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { granted ->
                notificationsEnabled = granted
                preferences.edit().putBoolean("notifications_enabled", granted).apply()
            }
            App(
                authService,
                clubContentApi,
                notificationsEnabled = notificationsEnabled,
                onNotificationsChange = { enabled ->
                    if (enabled && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU &&
                        ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
                    ) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        notificationsEnabled = enabled
                        preferences.edit().putBoolean("notifications_enabled", enabled).apply()
                    }
                }
            )
        }
    }

    @Deprecated("Deprecated in Android; retained for the legacy Google Sign-In activity result API")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: android.content.Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        lifecycleScope.launch { authService.handleSignInResult(requestCode, resultCode, data) }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
