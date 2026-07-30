package com.precon.mhsclubs.screens.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.precon.mhsclubs.auth.AuthService
import com.precon.mhsclubs.auth.AuthState
import com.precon.mhsclubs.model.User
import com.precon.mhsclubs.ui.FigmaActionButton
import com.precon.mhsclubs.ui.FigmaCard
import com.precon.mhsclubs.ui.FigmaMonogram
import com.precon.mhsclubs.ui.FigmaScreen
import com.precon.mhsclubs.ui.FigmaTitle
import kotlinx.coroutines.launch

@Composable
fun AccountScreen(
    authService: AuthService,
    memberType: String? = null,
    notificationsEnabled: Boolean = false,
    onNotificationsChange: (Boolean) -> Unit = {},
    onSignedOut: () -> Unit
) {
    val state by authService.authState.collectAsState(initial = AuthState.SignedOut)
    val scope = rememberCoroutineScope()
    LaunchedEffect(state) { if (state is AuthState.SignedOut) onSignedOut() }
    AccountContent(
        authService.currentUser,
        memberType,
        notificationsEnabled = notificationsEnabled,
        onNotificationsChange = onNotificationsChange,
        onSignOut = { scope.launch { authService.signOut() } }
    )
}

@Composable
fun AccountContent(
    user: User?,
    memberType: String? = null,
    notificationsEnabled: Boolean = false,
    onNotificationsChange: (Boolean) -> Unit = {},
    isSigningOut: Boolean = false,
    signOutError: String? = null,
    onSignOut: () -> Unit
) {
    FigmaScreen(Modifier.fillMaxSize()) {
        Spacer(Modifier.height(48.dp))
        FigmaTitle("Account", compact = true)
        Spacer(Modifier.height(24.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            val avatar = user?.avatarUrl?.takeIf { it.isNotBlank() }
            if (avatar != null) {
                RemoteProfileAvatar(avatar, user.displayName, Modifier.size(56.dp).clip(CircleShape))
            } else {
                FigmaMonogram(user?.displayName, Modifier.size(56.dp))
            }
            Column(Modifier.weight(1f).padding(start = 16.dp)) {
                Text(
                    text = user?.displayName ?: "Unknown User",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = user?.email ?: "${memberType ?: "Student"} member",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Spacer(Modifier.height(28.dp))
        FigmaCard(Modifier.fillMaxWidth()) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Notifications", style = MaterialTheme.typography.titleSmall)
                    Text(
                        text = "Club events and announcements",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = notificationsEnabled,
                    onCheckedChange = onNotificationsChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                        checkedBorderColor = Color.Transparent,
                        uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        uncheckedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )
            }
        }
        Spacer(Modifier.height(28.dp))
        FigmaActionButton(
            text = if (isSigningOut) "Signing out…" else "Sign out",
            modifier = Modifier.fillMaxWidth(),
            icon = Icons.Default.Logout,
            background = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
            onClick = onSignOut
        )
        signOutError?.let {
            Spacer(Modifier.height(10.dp))
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}
