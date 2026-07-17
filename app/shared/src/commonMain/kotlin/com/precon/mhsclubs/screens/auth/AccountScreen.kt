package com.precon.mhsclubs.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.precon.mhsclubs.auth.AuthService
import com.precon.mhsclubs.auth.AuthState
import com.precon.mhsclubs.model.User
import com.precon.mhsclubs.model.UserRole

/**
 * Account screen displaying user profile information.
 *
 * Shows the user's name, email, role, and provides a sign-out button.
 *
 * @param authService The authentication service
 * @param onSignedOut Callback when the user signs out
 */
@Composable
fun AccountScreen(
    authService: AuthService,
    onSignedOut: () -> Unit
) {
    val authState by authService.authState.collectAsState(initial = AuthState.SignedOut)

    // Handle sign out
    LaunchedEffect(authState) {
        if (authState is AuthState.SignedOut) {
            onSignedOut()
        }
    }

    AccountContent(
        user = authService.currentUser,
        onSignOut = { }
    )
}

/**
 * Content of the account screen.
 */
@Composable
fun AccountContent(
    user: User?,
    onSignOut: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Profile card
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Profile icon
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Profile",
                    modifier = Modifier.size(80.dp)
                )

                // User name
                Text(
                    text = user?.displayName ?: "Unknown User",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                // User email
                Text(
                    text = user?.email ?: "No email",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // User role badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    val roleText = when (user?.role) {
                        UserRole.Student -> "Student"
                        UserRole.Teacher -> "Teacher"
                        null -> "Unknown"
                    }
                    
                    Text(
                        text = roleText,
                        style = MaterialTheme.typography.labelLarge,
                        color = when (user?.role) {
                            UserRole.Teacher -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                    )
                }
            }
        }

        // App info card
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = "MHS Clubs",
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "MHS Clubs",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Manage your club memberships and events",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Sign out button
        Button(
            onClick = onSignOut,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Logout,
                contentDescription = "Sign Out"
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text("Sign Out")
        }
    }
}

@Preview
@Composable
fun AccountScreenPreview() {
    MaterialTheme {
        AccountContent(
            user = User(
                id = "1",
                firebaseUid = "abc123",
                email = "student@students.mcpasd.k12.wi.us",
                displayName = "John Doe",
                role = UserRole.Student
            ),
            onSignOut = {}
        )
    }
}
