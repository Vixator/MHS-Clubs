package com.precon.mhsclubs.screens.auth

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** Renders a profile photo when the active platform can load the provider URL. */
@Composable
expect fun RemoteProfileAvatar(
    avatarUrl: String,
    displayName: String,
    modifier: Modifier
)
