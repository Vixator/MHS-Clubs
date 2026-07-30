package com.precon.mhsclubs.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.precon.mhsclubs.auth.AuthService
import com.precon.mhsclubs.auth.AuthState
import com.precon.mhsclubs.model.User
import com.precon.mhsclubs.ui.FigmaActionButton
import com.precon.mhsclubs.ui.FigmaCard
import com.precon.mhsclubs.ui.FigmaDarkText
import com.precon.mhsclubs.ui.FigmaPage
import com.precon.mhsclubs.ui.FigmaRed
import com.precon.mhsclubs.ui.FigmaScreen
import com.precon.mhsclubs.ui.FigmaTan
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
        Box(Modifier.height(42.dp))
        FigmaTitle("Your account", compact = true)
        Box(Modifier.height(20.dp))
        Row(Modifier.fillMaxWidth().height(104.dp).clip(androidx.compose.foundation.shape.RoundedCornerShape(52.dp)).background(FigmaTan).padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
            user?.avatarUrl?.takeIf { it.isNotBlank() }?.let { avatar ->
                RemoteProfileAvatar(avatar, user.displayName, Modifier.size(64.dp).clip(CircleShape))
            } ?: Box(Modifier.size(64.dp).clip(CircleShape).background(FigmaPage), contentAlignment = Alignment.Center) { Text(user?.displayName?.initials() ?: "?", color = FigmaRed, fontWeight = FontWeight.ExtraBold, fontSize = 19.sp) }
            Column(Modifier.padding(start = 16.dp)) {
                Text(user?.displayName ?: "Unknown User", color = FigmaDarkText, fontSize = 19.sp, fontWeight = FontWeight.ExtraBold)
                Text("${memberType ?: "Student"} member", color = FigmaDarkText, fontSize = 13.sp)
            }
        }
        Box(Modifier.height(20.dp))
        FigmaCard(Modifier.fillMaxWidth().height(59.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Notifications", color = FigmaDarkText, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("Allow Permission", color = FigmaDarkText, fontSize = 13.sp)
                }
                Switch(
                    checked = notificationsEnabled,
                    onCheckedChange = onNotificationsChange,
                    modifier = Modifier.scale(.72f),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF34C759),
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color(0xFFE5E5EA),
                        uncheckedBorderColor = Color.Transparent
                    )
                )
            }
        }
        Box(Modifier.height(11.dp))
        FigmaCard(Modifier.fillMaxWidth().height(59.dp)) { Text("Attendance", color = FigmaDarkText, fontSize = 18.sp, fontWeight = FontWeight.Bold); Text("See all past attendance records", color = FigmaDarkText, fontSize = 13.sp) }
        Box(Modifier.height(20.dp))
        FigmaActionButton(if (isSigningOut) "Signing out…" else "Sign Out", Modifier.fillMaxWidth(), Icons.Default.Logout, background = Color(0xFF441D1E), contentColor = Color(0xFFFF424C), onClick = onSignOut)
        signOutError?.let { Text(it, color = Color(0xFFFF424C), fontSize = 12.sp, modifier = Modifier.padding(top = 10.dp)) }
    }
}

private fun String.initials(): String = split(" ").mapNotNull { it.firstOrNull()?.uppercase() }.take(2).joinToString("")
