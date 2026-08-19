package com.precon.mhsclubs.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.precon.mhsclubs.auth.AuthService
import com.precon.mhsclubs.auth.AuthState
import com.precon.mhsclubs.auth.SignInPendingException
import com.precon.mhsclubs.model.UserRole
import com.precon.mhsclubs.ui.FigmaStatusPanel
import kotlinx.coroutines.launch
import mhsclubs.app.shared.generated.resources.Res
import mhsclubs.app.shared.generated.resources.figma_sign_in_asset_1
import mhsclubs.app.shared.generated.resources.figma_sign_in_asset_2
import org.jetbrains.compose.resources.painterResource

@Composable
fun LoginScreen(authService: AuthService, onSignedIn: (UserRole) -> Unit, onError: (String) -> Unit) {
    val authState by authService.authState.collectAsState(initial = AuthState.SignedOut)
    val scope = rememberCoroutineScope()
    when (val state = authState) {
        is AuthState.SignedIn -> LaunchedEffect(state) { onSignedIn(state.user.role) }
        is AuthState.Error -> LaunchedEffect(state) { onError(state.message) }
        else -> Unit
    }
    LoginContent(
        isLoading = authState is AuthState.Loading,
        errorMessage = (authState as? AuthState.Error)?.message,
        onSignInClick = {
            scope.launch {
                authService.signInWithGoogle().exceptionOrNull()
                    ?.takeUnless { it is SignInPendingException }
                    ?.let { onError(it.message ?: "Unable to start Google sign-in") }
            }
        }
    )
}

@Composable
fun LoginContent(isLoading: Boolean, errorMessage: String? = null, onSignInClick: () -> Unit) {
    BoxWithConstraints(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.TopCenter
    ) {
        val horizontalPadding = if (maxWidth >= 402.dp) 56.dp else 28.dp
        Column(
            modifier = Modifier.widthIn(max = 402.dp).fillMaxWidth().padding(top = 88.dp).padding(horizontal = horizontalPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(Res.drawable.figma_sign_in_asset_2),
                contentDescription = null,
                modifier = Modifier.size(width = 190.dp, height = 183.dp)
            )
            Spacer(Modifier.height(22.dp))
            Column(
                modifier = Modifier.width(190.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "MHS",
                    style = MaterialTheme.typography.displayLarge,
                    fontSize = 62.sp,
                    lineHeight = 62.sp,
                    color = MaterialTheme.colorScheme.tertiary
                )
                Text(
                    text = "CLUBS",
                    style = MaterialTheme.typography.titleLarge,
                    fontSize = 23.sp,
                    lineHeight = 25.sp,
                    letterSpacing = 0.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            Spacer(Modifier.height(38.dp))
            if (isLoading) {
                FigmaStatusPanel(
                    title = "Signing you in",
                    message = "Waiting for Google to finish securely.",
                    isLoading = true
                )
            } else {
                val shape = RoundedCornerShape(100.dp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp)
                        .clip(shape)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape)
                        .clickable(onClick = onSignInClick)
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(Res.drawable.figma_sign_in_asset_1),
                        contentDescription = null,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.size(12.dp))
                    Text(
                        text = "Continue with Google",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
            Text(
                text = "A MCPASD School email is required",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            errorMessage?.let {
                Spacer(Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.small)
                        .background(MaterialTheme.colorScheme.errorContainer)
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
