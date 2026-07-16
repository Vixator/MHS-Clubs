package com.precon.mhsclubs.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.precon.mhsclubs.auth.AuthService
import com.precon.mhsclubs.auth.AuthState
import com.precon.mhsclubs.model.UserRole
import org.jetbrains.compose.resources.painterResource

/**
 * Login screen for the MHS Clubs app.
 *
 * Displays a welcome message and a Google Sign-In button.
 * Handles the authentication flow and navigates to the appropriate screen
 * based on the user's role.
 *
 * @param authService The authentication service
 * @param onSignedIn Callback when a user successfully signs in
 * @param onError Callback when an error occurs during sign-in
 */
@Composable
fun LoginScreen(
    authService: AuthService,
    onSignedIn: (UserRole) -> Unit,
    onError: (String) -> Unit
) {
    val authState by authService.authState.collectAsState()

    // Handle auth state changes
    when (val state = authState) {
        is AuthState.SignedIn -> {
            LaunchedEffect(state) {
                onSignedIn(state.user.role)
            }
        }
        is AuthState.Error -> {
            LaunchedEffect(state) {
                onError(state.message)
            }
        }
        else -> {
            // Show login screen
        }
    }

    LoginContent(
        isLoading = authState is AuthState.Loading,
        onSignInClick = { 
            // On Android, this would launch the Google Sign-In activity
            // On other platforms, it would call authService.signInWithGoogle()
        }
    )
}

/**
 * Content of the login screen.
 */
@Composable
fun LoginContent(
    isLoading: Boolean,
    onSignInClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App logo
        Image(
            imageVector = Icons.Default.School,
            contentDescription = "MHS Clubs",
            modifier = Modifier.size(120.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Welcome text
        Text(
            text = "MHS Clubs",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Sign in to join clubs, RSVP to events, and track your attendance",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Sign in button
        if (isLoading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = onSignInClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("Sign in with Google")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Domain restriction notice
        Text(
            text = "MCPASD email required",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview
@Composable
fun LoginScreenPreview() {
    MaterialTheme {
        LoginContent(
            isLoading = false,
            onSignInClick = {}
        )
    }
}
