package com.precon.mhsclubs

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
            App(authService, clubContentApi)
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
