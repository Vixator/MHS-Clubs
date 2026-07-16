package com.precon.mhsclubs

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.precon.mhsclubs.auth.AuthService
import com.precon.mhsclubs.auth.AuthState
import com.precon.mhsclubs.auth.createAuthService
import com.precon.mhsclubs.model.UserRole
import com.precon.mhsclubs.screens.auth.AccountScreen
import com.precon.mhsclubs.screens.auth.LoginScreen
import com.precon.mhsclubs.screens.clubs.ClubListScreen
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Main app component that handles authentication and navigation.
 */
@Composable
fun App() {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            AppContent()
        }
    }
}

/**
 * Main app content with authentication flow.
 */
@Composable
fun AppContent() {
    // Create auth service
    val authService: AuthService = remember { createAuthService() }
    
    // Collect auth state
    val authState by authService.authState.collectAsState()
    
    // Track the current screen
    var currentScreen by remember { mutableStateOf<AppScreen>(AppScreen.Login) }
    
    // Handle auth state changes
    when (val state = authState) {
        is AuthState.SignedIn -> {
            LaunchedEffect(state) {
                // Navigate based on user role
                currentScreen = when (state.user.role) {
                    UserRole.Teacher -> AppScreen.ClubList
                    UserRole.Student -> AppScreen.ClubList
                }
            }
        }
        is AuthState.SignedOut -> {
            LaunchedEffect(state) {
                currentScreen = AppScreen.Login
            }
        }
        is AuthState.Error -> {
            // Show error and go to login
            LaunchedEffect(state) {
                currentScreen = AppScreen.Login
            }
        }
        else -> {
            // Loading state - show login
        }
    }
    
    // Render the current screen
    when (currentScreen) {
        AppScreen.Login -> {
            LoginScreen(
                authService = authService,
                onSignedIn = { role ->
                    currentScreen = when (role) {
                        UserRole.Teacher -> AppScreen.ClubList
                        UserRole.Student -> AppScreen.ClubList
                    }
                },
                onError = { error ->
                    // Show error
                }
            )
        }
        AppScreen.ClubList -> {
            ClubListScreen(
                onAccountClick = { currentScreen = AppScreen.Account },
                onClubClick = { clubId ->
                    // Navigate to club detail
                }
            )
        }
        AppScreen.Account -> {
            AccountScreen(
                authService = authService,
                onSignedOut = { currentScreen = AppScreen.Login }
            )
        }
    }
}

/**
 * App screen navigation destinations.
 */
sealed class AppScreen {
    object Login : AppScreen()
    object ClubList : AppScreen()
    object ClubDetail : AppScreen()
    object Account : AppScreen()
    object EventList : AppScreen()
    object EventDetail : AppScreen()
    object AdminDashboard : AppScreen()
}

@Preview
@Composable
fun AppPreview() {
    App()
}
