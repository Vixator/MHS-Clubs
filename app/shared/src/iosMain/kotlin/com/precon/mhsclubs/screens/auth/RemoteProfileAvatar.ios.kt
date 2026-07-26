package com.precon.mhsclubs.screens.auth

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
actual fun RemoteProfileAvatar(avatarUrl: String, displayName: String, modifier: Modifier) {
    Icon(Icons.Default.AccountCircle, contentDescription = "$displayName profile picture", modifier = modifier)
}
